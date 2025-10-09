package com.Globoo.storage.infra;


import com.Globoo.storage.service.FileStorageService;
import org.springframework.stereotype.Component;
import java.io.InputStream;

@Component
public class S3Storage implements FileStorageService {
    @Override public String store(InputStream in, String filename){ return "s3://bucket/" + filename; }
    @Override public void delete(String url){ }
}
