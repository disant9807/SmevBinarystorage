package ru.spandco.logic.storage.entity;

import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

import static javax.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "binaryMetadata")
public class BinaryMetadata implements Serializable{

    private String Id;
    private int Version;
    private String Name;
    private String MimeType;
    private Long Size;

    private Date TimeToDelete;

    public void setId(String id) {
        this.Id = id;
    }

    @Id
    @Column(name = "ID")
    public String getId() {
        return this.Id;
    }

    @Version
    @Column(name = "Version")
    public int getVersion() {
        return this.Version;
    }

    @Column(name = "Column")
    public String getName() {
        return this.Name;
    }

    @Column(name = "MimeType")
    public String getMimeType() {
        return this.MimeType;
    }

    @Column(name = "Size")
    public Long getSize() {
        return  this.Size;
    }

    @Temporal(TemporalType.DATE)
    @Column(name = "TIME_TO_DELETE")
    @DateTimeFormat(pattern = "dd/MM/yyyy")
    public Date getTimeToDelete() {
        return  this.TimeToDelete;
    }

    public void setVersion(int version) {
        this.Version = version;
    }

    public void setName(String name) {
        this.Name = name;
    }

    public void setMimeType(String mimeType) {
        this.MimeType = mimeType;
    }

    public void setSize(Long size) {
        this.Size = size;
    }

    @Temporal(TemporalType.DATE)
    @Column(name = "TIME_TO_DELETE")
    @DateTimeFormat(pattern = "dd/MM/yyyy")
    public void setTimeToDelete(Date timeToDelete) {
        this.TimeToDelete = timeToDelete;
    }
}
