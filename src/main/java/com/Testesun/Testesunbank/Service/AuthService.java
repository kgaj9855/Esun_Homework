package com.Testesun.Testesunbank.Service;

import com.Testesun.Testesunbank.DTO.*;
import com.Testesun.Testesunbank.Model.User;
import com.Testesun.Testesunbank.common.ApiException;
import com.Testesun.Testesunbank.dao.BankDao;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private final BankDao dao;
    private final PasswordEncoder encoder;
    private final String dummyHash;

    public AuthService(BankDao dao, PasswordEncoder encoder) {
        this.dao = dao;
        this.encoder = encoder;
        this.dummyHash = encoder.encode("unknown-user-timing-placeholder");
    }

    @Transactional
    public User register(RegisterRequest request) {
        // BCrypt 最多接受 72 bytes，中文字必須以 UTF-8 實際長度計算。
        if (request.getPassword().getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "密碼 UTF-8 長度不得超過 72 bytes");
        }
        User user = new User(null, request.getUserName().trim(), normalize(request.getEmail()), request.getAccount(),
                encoder.encode(request.getPassword()));
        return dao.register(user);
    }

    public User login(LoginRequest request) {
        User user = dao.userByEmail(normalize(request.getEmail()));
        // 查無會員時也比對一次密碼，避免用回應時間猜測會員是否存在。
        String passwordHash = dummyHash;
        if (user != null) {
            passwordHash = user.getPasswordHash();
        }
        boolean matched = encoder.matches(request.getPassword(), passwordHash);
        if (user == null || !matched) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Email 或密碼不正確");
        }
        return user;
    }

    private String normalize(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
