package com.Globoo.storage.infra;


import com.Globoo.storage.service.FileStorageService;
import org.springframework.stereotype.Component;
import java.io.InputStream;

@Component
public class LocalStorage implements FileStorageService {
    @Override public String store(InputStream in, String filename){ return "/local/" + filename; }
    @Override public void delete(String url){ }
}
