package com.rcszh.gm.file.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rcszh.gm.common.file.FileStorageProperties;
import com.rcszh.gm.common.model.PageResult;
import com.rcszh.gm.file.dto.FileUploadResponse;
import com.rcszh.gm.file.dto.SysFileDto;
import com.rcszh.gm.file.entity.SysFile;
import com.rcszh.gm.file.mapper.SysFileMapper;
import com.rcszh.gm.user.service.audit.AuditLogService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class FileService {

    private static final Pattern BIZ_PATTERN = Pattern.compile("^[a-zA-Z0-9/_-]{1,64}$");

    private final SysFileMapper fileMapper;
    private final FileStorageProperties props;
    private final AuditLogService auditLogService;

    public FileService(SysFileMapper fileMapper, FileStorageProperties props, AuditLogService auditLogService) {
        this.fileMapper = fileMapper;
        this.props = props;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public FileUploadResponse upload(Long uploaderUserId, String biz, String type, MultipartFile file) {
        String safeBiz = normalizeBiz(biz);
        validateFile(type, file);

        String originalName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "file";
        String ext = extFromName(originalName);

        String uuid = UUID.randomUUID().toString().replace("-", "");
        String storedName = ext != null ? (uuid + "." + ext) : uuid;

        LocalDate today = LocalDate.now();
        String subPath = safeBiz + "/" + today.getYear() + "/" + two(today.getMonthValue()) + "/" + two(today.getDayOfMonth()) + "/" + storedName;
        String url = "/files/" + subPath;

        Path root = Path.of(props.getStorageDir()).toAbsolutePath().normalize();
        Path target = root.resolve(subPath).normalize();
        if (!target.startsWith(root)) {
            throw new IllegalArgumentException("Invalid storage path");
        }

        try {
            Files.createDirectories(target.getParent());
            file.transferTo(target);
        } catch (IOException e) {
            auditLogService.fail("FILE_UPLOAD", "FILE", null, e.getMessage());
            throw new IllegalStateException("File save failed");
        }

        var rec = new SysFile();
        rec.setBiz(safeBiz);
        rec.setOriginalName(originalName);
        rec.setStoredName(storedName);
        rec.setExt(ext);
        rec.setContentType(file.getContentType());
        rec.setSizeBytes(file.getSize());
        rec.setStoragePath(subPath);
        rec.setUrl(url);
        rec.setDeleted(false);
        rec.setDeletedAt(null);
        rec.setCreatedByUserId(uploaderUserId);
        fileMapper.insert(rec);

        auditLogService.success("FILE_UPLOAD", "FILE", String.valueOf(rec.getId()), rec.getUrl());

        return new FileUploadResponse(rec.getId(), rec.getStoragePath(), rec.getUrl(), rec.getOriginalName(), rec.getSizeBytes(), rec.getContentType());
    }

    public PageResult<SysFileDto> list(long page, long size, String biz, String keyword, String startAt, String endAt) {
        var wrapper = new LambdaQueryWrapper<SysFile>();
        wrapper.eq(SysFile::getDeleted, false);

        if (biz != null && !biz.isBlank()) {
            wrapper.eq(SysFile::getBiz, normalizeBiz(biz));
        }
        if (keyword != null && !keyword.isBlank()) {
            String kw = keyword.trim();
            wrapper.and(w -> w.like(SysFile::getOriginalName, kw).or().like(SysFile::getUrl, kw));
        }

        LocalDateTime start = parseTime(startAt);
        LocalDateTime end = parseTime(endAt);
        if (start != null) {
            wrapper.ge(SysFile::getCreatedAt, start);
        }
        if (end != null) {
            wrapper.le(SysFile::getCreatedAt, end);
        }

        wrapper.orderByDesc(SysFile::getId);

        Page<SysFile> p = fileMapper.selectPage(new Page<>(page, size), wrapper);
        var records = p.getRecords().stream().map(SysFileDto::from).toList();
        return new PageResult<>(records, p.getTotal(), page, size);
    }

    @Transactional
    public void delete(Long operatorUserId, long id) {
        SysFile f = fileMapper.selectById(id);
        if (f == null || Boolean.TRUE.equals(f.getDeleted())) {
            return;
        }

        f.setDeleted(true);
        f.setDeletedAt(LocalDateTime.now());
        fileMapper.updateById(f);

        Path root = Path.of(props.getStorageDir()).toAbsolutePath().normalize();
        Path target = root.resolve(f.getStoragePath()).normalize();
        boolean diskDeleted = false;
        try {
            if (target.startsWith(root)) {
                diskDeleted = Files.deleteIfExists(target);
            }
        } catch (Exception ignored) {
        }

        auditLogService.success("FILE_DELETE", "FILE", String.valueOf(id), "diskDeleted=" + diskDeleted);
    }

    private static void validateFile(String type, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }
        if ("image".equalsIgnoreCase(type)) {
            String ct = file.getContentType();
            if (ct == null || !ct.toLowerCase().startsWith("image/")) {
                throw new IllegalArgumentException("Only image is allowed");
            }
        }
    }

    private static String normalizeBiz(String biz) {
        if (biz == null || biz.isBlank()) {
            throw new IllegalArgumentException("biz is required");
        }
        String b = biz.trim();
        if (b.startsWith("/")) b = b.substring(1);
        if (b.endsWith("/")) b = b.substring(0, b.length() - 1);
        if (!BIZ_PATTERN.matcher(b).matches() || b.contains("..")) {
            throw new IllegalArgumentException("Invalid biz");
        }
        return b;
    }

    private static String extFromName(String name) {
        if (name == null) return null;
        String n = name.trim();
        int idx = n.lastIndexOf('.');
        if (idx <= 0 || idx == n.length() - 1) return null;
        String ext = n.substring(idx + 1).toLowerCase();
        // Keep it simple to avoid weird suffixes.
        if (!ext.matches("^[a-z0-9]{1,10}$")) return null;
        return ext;
    }

    private static String two(int n) {
        return (n < 10 ? "0" : "") + n;
    }

    private static LocalDateTime parseTime(String s) {
        if (s == null || s.isBlank()) return null;
        String t = s.trim();
        try {
            // Accept ISO-8601 with offset (preferred).
            return OffsetDateTime.parse(t).toLocalDateTime();
        } catch (DateTimeParseException ignored) {
        }
        try {
            // Also accept LocalDateTime format without offset.
            return LocalDateTime.parse(t);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid datetime: " + s);
        }
    }
}

