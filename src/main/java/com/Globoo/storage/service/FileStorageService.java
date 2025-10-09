package com.Globoo.storage.service;


import java.io.InputStream;

public interface FileStorageService {
    String store(InputStream in, String filename);
    void delete(String url);
}
