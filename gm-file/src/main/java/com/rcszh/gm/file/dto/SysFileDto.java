package com.rcszh.gm.file.dto;

import com.rcszh.gm.file.entity.SysFile;

import java.time.LocalDateTime;

public record SysFileDto(
        Long id,
        String biz,
        String originalName,
        String storedName,
        String ext,
        String contentType,
        Long sizeBytes,
        String storagePath,
        String url,
        Long createdByUserId,
        LocalDateTime createdAt
) {
    public static SysFileDto from(SysFile e) {
        return new SysFileDto(
                e.getId(),
                e.getBiz(),
                e.getOriginalName(),
                e.getStoredName(),
                e.getExt(),
                e.getContentType(),
                e.getSizeBytes(),
                e.getStoragePath(),
                e.getUrl(),
                e.getCreatedByUserId(),
                e.getCreatedAt()
        );
    }
}

