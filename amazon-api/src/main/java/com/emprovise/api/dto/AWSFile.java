package com.emprovise.api.dto;

public class AWSFile {

    private String fileName;
    private byte[] fileBytes;

    public AWSFile(String fileName, byte[] fileBytes) {
        this.fileName = fileName;
        this.fileBytes = fileBytes;
    }

    public String getFileName() {
        return fileName;
    }

    public byte[] getFileBytes() {
        return fileBytes;
    }
}
