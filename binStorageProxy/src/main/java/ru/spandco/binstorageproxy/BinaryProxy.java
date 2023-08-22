package ru.spandco.binstorageproxy;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import ru.spandco.binarystoragemodel.ApiHeaders;
import ru.spandco.binarystoragemodel.BinaryModel;
import ru.spandco.binarystoragemodel.SaveMode;

@Service("BinaryProxy")
@RequiredArgsConstructor
public class BinaryProxy {
    private String serviceUrl = "http://localhost:8001/bin";
    private String saveUrl = "/";
    private String getUrl = "/data";
    private String getInfoUrl = "/info";
    private String deleteUrl = "/delete";
    private String confirmSaveUrl = "/confirm";


    @Autowired
    private RestTemplate rest;

    public String Save(byte[] file, SaveMode mode, String id,
                       String name, String mimeType,
                       Boolean isConfirmationRequered) throws FileNotFoundException {
        if (file.length == 0) {
            throw new IllegalArgumentException();
        }

        String encodeName = Base64
                .getEncoder()
                .encodeToString(name.getBytes());

        HttpHeaders headers = new HttpHeaders();
        headers.add(ApiHeaders.ContentName, encodeName);
        headers.add(ApiHeaders.ContentId, id);
        headers.add(ApiHeaders.ContentType, mimeType);
        headers.add(ApiHeaders.ContentSaveMode, mode.toString());
        headers.add(ApiHeaders.ContentConfirmation, isConfirmationRequered
                .toString());

        ByteArrayResource contentsAsResource = new ByteArrayResource(file) {
            @Override
            public String getFilename() {
                return name;
            }
        };
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<String, Object>();
        map.add("file", contentsAsResource);
        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(
                map, headers
        );

        ResponseEntity<String> response = rest
                .exchange(
                serviceUrl + saveUrl, HttpMethod.POST, request, String.class
                );

        if (!response.getStatusCode().is2xxSuccessful()) {
            if (response.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new FileNotFoundException();
            }
        }

        return response.getBody();
    }

    public BinaryModel Get(String id) throws IOException {
        if (id.isEmpty()) {
            throw new IllegalArgumentException();
        }

        HttpEntity request = new HttpEntity(new HttpHeaders());

        ResponseEntity<byte[]> response = rest
                .exchange(serviceUrl + getUrl + "?id=" + id, HttpMethod.GET, request, byte[].class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            if (response.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new FileNotFoundException();
            }
            throw new IllegalArgumentException();
        }
        //return response.getBody();
        try(ByteArrayOutputStream stream = new ByteArrayOutputStream(response.getBody().length);) {
            stream.write(response.getBody(), 0, response.getBody().length);

            HttpHeaders headers = response.getHeaders();
            String responseMimeType =  headers.getFirst("Content-Type");
            String responseName = headers.getFirst("Content-Name");
            String responseId = headers.getFirst("Content-Id");

            BinaryModel result = new BinaryModel();
            result.Content = stream;
            result.Id = responseId;
            result.Name = responseName;
            result.MimeType = responseMimeType;

            return result;
        }
    }

    public BinaryModel Getinfo(String id) throws FileNotFoundException {
        if (id.isEmpty()) {
            throw new IllegalArgumentException();
        }

        HttpEntity<String> request = new HttpEntity<>(id);

        ResponseEntity<BinaryModel> response = rest
                .exchange(serviceUrl + getInfoUrl, HttpMethod.GET, request, BinaryModel.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            if (response.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new FileNotFoundException();
            }
            throw new IllegalArgumentException();
        }
        return response.getBody();
    }

    public void Delete(String id) throws FileNotFoundException {
        HttpEntity<String> request = new HttpEntity<>(id);
        ResponseEntity<String> response = rest
                .exchange(serviceUrl + deleteUrl, HttpMethod.POST, request, String.class);

        ValidateSuccessStatusCode(response.getStatusCode());
    }

    public void ConfirmSave(String[] ids) throws FileNotFoundException {
        HttpEntity<String[]> request = new HttpEntity<>(ids);
        ResponseEntity<String> response = rest
                .exchange(serviceUrl + deleteUrl, HttpMethod.POST, request, String.class);

        ValidateSuccessStatusCode(response.getStatusCode());
    }


    private static void ValidateSuccessStatusCode(HttpStatus status) throws FileNotFoundException {
        if (!status.is2xxSuccessful()) {
            if (status == HttpStatus.NOT_FOUND) {
                throw new FileNotFoundException();
            }
            else if (status == HttpStatus.BAD_REQUEST) {
                throw new IllegalArgumentException();
            }
        }
    }
}
