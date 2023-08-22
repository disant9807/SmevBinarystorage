package ru.spandco.logic.storage.dao;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.spandco.logic.storage.entity.BinaryMetadata;

import javax.validation.constraints.NotNull;
import java.util.List;

@Repository
public interface BinaryMetadataDao extends CrudRepository<BinaryMetadata, String> {
    @Query(
            value = "select * from binaryMetadata " +
                    "where Id = :binaryId",
            nativeQuery = true
    )
    BinaryMetadata getBinaryMetadataById(
            @NotNull @Param("binaryId") String binaryId);

    @Query(
            value = "select * from binaryMetadata " +
                    "where Id in (:binaryIds)",
            nativeQuery = true
    )
    List<BinaryMetadata> getBinaryMetadataByIds(
            @NotNull @Param("binaryIds") String[] binaryIds);

    @Query(
            value = "select * from binaryMetadata " +
                    "where datetime(TIME_TO_DELETE, 'unixepoch') <= CURRENT_DATE",
            nativeQuery = true
    )
    List<String> getUnconfirmedTimedoutIds();

    @Query(
            value = "select * from binaryMetadata " +
                    "where TIME_TO_DELETE IS NOT NULL " +
                    "and datetime(TIME_TO_DELETE, 'unixepoch') > CURRENT_DATE " +
                    "order by TIME_TO_DELETE",
            nativeQuery = true
    )
    List<BinaryMetadata> getUnconfirmedBinaryMetadataArray();

}
