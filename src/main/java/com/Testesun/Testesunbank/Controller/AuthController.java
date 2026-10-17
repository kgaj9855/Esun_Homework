package com.Testesun.Testesunbank.Controller;
import com.Testesun.Testesunbank.DTO.*;
import com.Testesun.Testesunbank.Model.User;
import com.Testesun.Testesunbank.Service.AuthService;
import com.Testesun.Testesunbank.dao.BankDao;
import com.Testesun.Testesunbank.common.ApiException;
import jakarta.servlet.http.*;
import jakarta.validation.Valid;
import java.util.*;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService service;
    private final BankDao dao;
    private final HttpSessionSecurityContextRepository contexts = new HttpSessionSecurityContextRepository();
    public AuthController(AuthService service, BankDao dao) {
        this.service = service;
        this.dao = dao;
    }
    @GetMapping("/csrf")
    public Map<String,String> csrf(CsrfToken token) {
        return Map.of("token", token.getToken(), "headerName", token.getHeaderName());
    }
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public User register(@Valid @RequestBody RegisterRequest request) {
        return service.register(request);
    }
    @PostMapping("/login")
    public User login(@Valid @RequestBody LoginRequest loginRequest, HttpServletRequest request, HttpServletResponse response) {
        User user = service.login(loginRequest);
        // 登入後更換 Session ID，避免沿用登入前的識別碼。
        request.getSession();
        request.changeSessionId();
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(UsernamePasswordAuthenticationToken.authenticated(user.getUserId(), null,
            List.of(new SimpleGrantedAuthority("ROLE_USER"))));
        SecurityContextHolder.setContext(context);
        contexts.saveContext(context, request, response);
        new org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository().saveToken(null, request, response);
        request.getSession().setAttribute("email", user.getEmail());
        return user;
    }
    @GetMapping("/me")
    public User me(Authentication authentication, HttpServletRequest request) {
        String email = (String) request.getSession().getAttribute("email");
        User user = dao.userByEmail(email);
        if (user == null) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "請重新登入");
        }
        if (!user.getUserId().equals(authentication.getName())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "請重新登入");
        }
        return user;
    }
}
