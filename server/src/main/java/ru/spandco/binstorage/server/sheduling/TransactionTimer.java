package ru.spandco.binstorage.server.sheduling;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.spandco.binstorage.server.controller.BinController;
import ru.spandco.binstorage.server.hub.BinaryDataHub;
import ru.spandco.binstorage.server.hub.BinaryMetadataEventConfirmArgs;
import ru.spandco.binstorage.server.hub.BinaryMetadataEventSaveArgs;
import ru.spandco.logic.storage.entity.BinaryMetadata;
import ru.spandco.logic.storage.service.BinaryMetadataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class TransactionTimer {

    private Object queueLock = new Object();
    private final Logger logger =
            LoggerFactory.getLogger(BinController.class);

    @Autowired
    private BinaryMetadataService binaryMetadataService;

    private List<BinaryMetadata> timeoutQueue;

    private ScheduledExecutorService taskExecutor = Executors.newSingleThreadScheduledExecutor();

    private Runnable OnTimeoutTask = OnTimeout();

    private Boolean enabled = false;

    public TransactionTimer() {
        timeoutQueue = new ArrayList<>();
    }



    @Scheduled(fixedDelay = 2000)
    public void TransactionCheckedStart() throws InterruptedException {
        String[] timeoutBinaryDataIds = binaryMetadataService
                .getUnconfirmedTimeoutIds();


        if (timeoutBinaryDataIds.length > 0) {
            ExecutorService taskExecutor = Executors
                    .newFixedThreadPool(timeoutBinaryDataIds.length);

            for (String binaryId : timeoutBinaryDataIds) {
                Runnable deleteBinaryData = new Runnable() {
                    public void run() {
                        try {
                            binaryMetadataService.deleteBinaryMetadata(binaryId);
                        } catch (Exception ex) {
                            throw new RuntimeException(ex);
                        }
                    }
                };

                taskExecutor.execute(deleteBinaryData);
            }

            taskExecutor.shutdown();

            BinaryMetadata[] unconfirmedBinaries = binaryMetadataService
                    .getUnconfirmedBinaryMetadataArray().toArray(new BinaryMetadata[0]);

            boolean hasAnyUnconfirmedBinaries = unconfirmedBinaries != null
                    && unconfirmedBinaries.length > 0;

            if (!hasAnyUnconfirmedBinaries) return;

            synchronized (queueLock) {
                for (BinaryMetadata bin : unconfirmedBinaries) {
                    timeoutQueue.add(bin);
                }
            }
        }
    }

    private Runnable OnTimeout() {
        return () -> {
            boolean hasNextTimeout = false;
            do {
                 BinaryMetadata binaryMetadata;

                synchronized (queueLock) {
                    if (timeoutQueue.isEmpty()) return;

                    binaryMetadata = timeoutQueue.stream()
                            .findFirst().get();

                    if (binaryMetadata.getTimeToDelete().compareTo(new Date()) > 0) {
                        UpdateTimer();
                        return;
                    }
                }

                boolean isDataConfirmed = binaryMetadataService
                        .IsConfirmed(binaryMetadata.getId());

                if (!isDataConfirmed) {
                    binaryMetadataService.deleteBinaryMetadata(binaryMetadata.getId());
                }

                synchronized (queueLock) {
                    timeoutQueue.remove(binaryMetadata);

                    hasNextTimeout = !timeoutQueue.isEmpty()
                            && timeoutQueue.stream().findFirst().get().getTimeToDelete()
                            .compareTo(new Date()) < 0;
                }

            } while (hasNextTimeout);

            UpdateTimer();
        };
    }

    private void ChangeTimer(Integer awaitSecond) {
        if (taskExecutor.isShutdown()) {
            taskExecutor.shutdown();
        }

        if (awaitSecond != null)
            taskExecutor.schedule(OnTimeout(), awaitSecond, TimeUnit.SECONDS);
        else
            taskExecutor.schedule(OnTimeout(), 0, TimeUnit.SECONDS);
    }

    private void UpdateTimer() {
        synchronized (queueLock) {
            if (timeoutQueue.isEmpty()) {
                ChangeTimer(null);
            } else {
                BinaryMetadata nextMeta = timeoutQueue.stream()
                        .findFirst().get();

                Date nextTime = nextMeta.getTimeToDelete();
                if (nextTime == null) {
                        nextTime = new Date();
                }

                int awaitSecond = (int)Math.max(0, (nextTime.getTime() - new Date().getTime()) / 1000) + 1;
                awaitSecond *= 1000;
                ChangeTimer(awaitSecond);
            }
        }
    }

    @EventListener(BinaryMetadataEventSaveArgs.class)
    public void BinaryDataHub_BinaryMetadataSaved(BinaryMetadataEventSaveArgs e) {
        if (e.binaryMetadata.getTimeToDelete() == null) return;

        synchronized (queueLock) {
            timeoutQueue.add(e.binaryMetadata);
            if(timeoutQueue.stream().count() == 1) UpdateTimer();
        }
    }


    @EventListener(BinaryMetadataEventConfirmArgs.class)
    public void BinaryDataHub_BinaryMetadataConfirmed(BinaryMetadataEventConfirmArgs e) {
        if (e.binaryMetadata == null || e.binaryMetadata.getId().isEmpty()) return;

        BinaryMetadata confirmMeta = e.binaryMetadata;

        synchronized (queueLock) {
            BinaryMetadata meta = timeoutQueue.stream().filter(z -> z.getId() == confirmMeta.getId())
                    .findFirst().get();

            if(meta == null) return;

            int index = timeoutQueue.indexOf(meta);
            if (index > -1) {
                timeoutQueue.remove(index);
                if (index == 0) UpdateTimer();
            }
        }
    }


}
