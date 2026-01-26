package com.rcszh.gm.common.web;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import com.rcszh.gm.common.api.ApiErrorCodes;
import com.rcszh.gm.common.api.ApiResponse;
import com.rcszh.gm.common.exception.UnauthorizedException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        var msg = e.getBindingResult().getAllErrors().isEmpty()
                ? "Invalid request"
                : e.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(ApiErrorCodes.BAD_REQUEST, msg));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolation(ConstraintViolationException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(ApiErrorCodes.BAD_REQUEST, e.getMessage()));
    }

    @ExceptionHandler(NotLoginException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotLogin(NotLoginException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error(ApiErrorCodes.UNAUTHORIZED, "Not logged in"));
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnauthorized(UnauthorizedException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error(ApiErrorCodes.UNAUTHORIZED, e.getMessage()));
    }

    @ExceptionHandler(NotPermissionException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotPermission(NotPermissionException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error(ApiErrorCodes.FORBIDDEN, "Forbidden"));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(ApiErrorCodes.BAD_REQUEST, e.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalState(IllegalStateException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.error(ApiErrorCodes.CONFLICT, e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleOther(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(ApiErrorCodes.INTERNAL_ERROR, "Internal error"));
    }
}
