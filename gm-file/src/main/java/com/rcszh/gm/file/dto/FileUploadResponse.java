package com.rcszh.gm.file.dto;

public record FileUploadResponse(
        Long id,
        String key,
        String url,
        String originalName,
        Long size,
        String contentType
) {
}

