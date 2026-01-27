package com.rcszh.gm.api.file;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.stp.StpUtil;
import com.rcszh.gm.common.api.ApiResponse;
import com.rcszh.gm.common.model.PageResult;
import com.rcszh.gm.common.security.LoginIdUtils;
import com.rcszh.gm.file.dto.FileUploadResponse;
import com.rcszh.gm.file.dto.SysFileDto;
import com.rcszh.gm.file.service.FileService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @SaCheckPermission("file:upload")
    @PostMapping("/upload")
    public ApiResponse<FileUploadResponse> upload(
            @RequestParam("biz") @NotBlank String biz,
            @RequestParam(value = "type", required = false) String type,
            @RequestPart("file") MultipartFile file
    ) {
        Long userId = StpUtil.isLogin() ? LoginIdUtils.parseAdminId(StpUtil.getLoginId()) : null;
        return ApiResponse.ok(fileService.upload(userId, biz, type, file));
    }

    @SaCheckPermission("file:list")
    @GetMapping
    public ApiResponse<PageResult<SysFileDto>> list(
            @RequestParam(defaultValue = "1") @Min(1) long page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(200) long size,
            @RequestParam(required = false) String biz,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String startAt,
            @RequestParam(required = false) String endAt
    ) {
        return ApiResponse.ok(fileService.list(page, size, biz, keyword, startAt, endAt));
    }

    @SaCheckPermission("file:delete")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable("id") long id) {
        Long userId = StpUtil.isLogin() ? LoginIdUtils.parseAdminId(StpUtil.getLoginId()) : null;
        fileService.delete(userId, id);
        return ApiResponse.ok(null);
    }
}

