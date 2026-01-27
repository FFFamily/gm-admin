package com.rcszh.gm.common.file;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "gm.file")
public class FileStorageProperties {

    /**
     * Root directory for storing uploaded files.
     * Supports absolute path or relative path (relative to process working dir).
     */
    private String storageDir = "./upload";

    public String getStorageDir() {
        return storageDir;
    }

    public void setStorageDir(String storageDir) {
        this.storageDir = storageDir;
    }
}

