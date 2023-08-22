package ru.spandco.binarystoragemodel;

import java.io.ByteArrayOutputStream;
import java.util.Date;

public class BinaryModel {
    public String Id;
    public int Version;
    public String Name;
    public String MimeType;
    public Long Size;
    public Date TimeToDelete;
    public ByteArrayOutputStream Content;
}
