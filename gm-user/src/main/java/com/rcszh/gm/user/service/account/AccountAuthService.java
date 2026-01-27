package com.rcszh.gm.user.service.account;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.rcszh.gm.common.exception.UnauthorizedException;
import com.rcszh.gm.common.security.LoginIdUtils;
import com.rcszh.gm.common.web.RequestUtils;
import com.rcszh.gm.user.dto.account.AccountLoginRequest;
import com.rcszh.gm.user.dto.account.AccountLoginResponse;
import com.rcszh.gm.user.dto.account.AccountMeDto;
import com.rcszh.gm.user.dto.account.AccountRegisterRequest;
import com.rcszh.gm.user.dto.account.AccountUpdateRequest;
import com.rcszh.gm.user.entity.AppAccount;
import com.rcszh.gm.user.entity.AppAccountLoginLog;
import com.rcszh.gm.user.mapper.AppAccountLoginLogMapper;
import com.rcszh.gm.user.mapper.AppAccountMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AccountAuthService {

    private final AppAccountMapper accountMapper;
    private final AppAccountLoginLogMapper loginLogMapper;
    private final PasswordEncoder passwordEncoder;

    public AccountAuthService(AppAccountMapper accountMapper,
                              AppAccountLoginLogMapper loginLogMapper,
                              PasswordEncoder passwordEncoder) {
        this.accountMapper = accountMapper;
        this.loginLogMapper = loginLogMapper;
        this.passwordEncoder = passwordEncoder;
    }

    public AccountLoginResponse login(AccountLoginRequest req) {
        String username = req.username().trim();

        AppAccount a = accountMapper.selectOne(new LambdaQueryWrapper<AppAccount>()
                .eq(AppAccount::getUsername, username)
                .last("LIMIT 1"));

        if (a == null) {
            writeLoginLog(username, false, "ACCOUNT_NOT_FOUND");
            throw new IllegalArgumentException("Invalid username or password");
        }
        if (Boolean.FALSE.equals(a.getEnabled())) {
            writeLoginLog(username, false, "ACCOUNT_DISABLED");
            throw new IllegalArgumentException("Invalid username or password");
        }
        if (!passwordEncoder.matches(req.password(), a.getPasswordHash())) {
            writeLoginLog(username, false, "BAD_PASSWORD");
            throw new IllegalArgumentException("Invalid username or password");
        }

        StpUtil.login(LoginIdUtils.accountLoginId(a.getId()));
        a.setLastLoginAt(LocalDateTime.now());
        accountMapper.updateById(a);
        writeLoginLog(username, true, null);

        return new AccountLoginResponse(StpUtil.getTokenValue());
    }

    @Transactional
    public AccountLoginResponse register(AccountRegisterRequest req) {
        String username = req.username().trim();

        // Username must be unique.
        AppAccount exists = accountMapper.selectOne(new LambdaQueryWrapper<AppAccount>()
                .eq(AppAccount::getUsername, username)
                .last("LIMIT 1"));
        if (exists != null) {
            throw new IllegalStateException("Username already exists");
        }

        String displayName = req.displayName() == null ? "" : req.displayName().trim();
        if (displayName.isBlank()) {
            displayName = username;
        }

        var a = new AppAccount();
        a.setUsername(username);
        a.setDisplayName(displayName);
        a.setEnabled(true);
        a.setPasswordHash(passwordEncoder.encode(req.password()));
        a.setLastLoginAt(LocalDateTime.now());
        accountMapper.insert(a);

        // Auto-login after register.
        StpUtil.login(LoginIdUtils.accountLoginId(a.getId()));
        writeLoginLog(username, true, "REGISTER_AUTO_LOGIN");
        return new AccountLoginResponse(StpUtil.getTokenValue());
    }

    public AccountMeDto me() {
        Long accountId = LoginIdUtils.parseAccountId(StpUtil.getLoginId());
        if (accountId == null) {
            throw new UnauthorizedException("Not logged in");
        }
        AppAccount a = accountMapper.selectById(accountId);
        if (a == null || Boolean.FALSE.equals(a.getEnabled())) {
            StpUtil.logout();
            throw new UnauthorizedException("Not logged in");
        }
        return AccountMeDto.from(a);
    }

    @Transactional
    public AccountMeDto updateMe(AccountUpdateRequest req) {
        Long accountId = LoginIdUtils.parseAccountId(StpUtil.getLoginId());
        if (accountId == null) {
            throw new UnauthorizedException("Not logged in");
        }
        AppAccount a = accountMapper.selectById(accountId);
        if (a == null || Boolean.FALSE.equals(a.getEnabled())) {
            StpUtil.logout();
            throw new UnauthorizedException("Not logged in");
        }
        a.setDisplayName(req.displayName().trim());
        accountMapper.updateById(a);
        return AccountMeDto.from(a);
    }

    public void logout() {
        Long accountId = LoginIdUtils.parseAccountId(StpUtil.getLoginId());
        if (accountId == null) {
            throw new UnauthorizedException("Not logged in");
        }
        StpUtil.logout();
    }

    private void writeLoginLog(String username, boolean success, String reason) {
        var log = new AppAccountLoginLog();
        log.setUsername(username);
        log.setSuccess(success);
        log.setReason(reason);
        log.setIp(RequestUtils.clientIp());
        log.setUa(RequestUtils.userAgent());
        loginLogMapper.insert(log);
    }
}
