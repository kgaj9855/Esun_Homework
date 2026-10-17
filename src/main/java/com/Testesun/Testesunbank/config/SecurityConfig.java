package com.Testesun.Testesunbank.config;
import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
@Configuration
public class SecurityConfig {
    // 註冊儲存密碼雜湊，登入時以同一種演算法比對。
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    // Spring Security 使用此設定決定哪些網址需要登入，以及如何處理登出。
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // 註冊與登入開放存取，其餘 API 需要會員身分。
        http.authorizeHttpRequests(authorize -> authorize
            .requestMatchers("/api/auth/csrf", "/api/auth/register", "/api/auth/login").permitAll()
            .requestMatchers("/api/**").authenticated().anyRequest().permitAll());
        // 用 JSON 回應錯誤，讓前端可以顯示訊息。
        http.exceptionHandling(errors -> errors
                .authenticationEntryPoint((request, response, exception) -> {
                    response.setStatus(401);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"message\":\"請先登入\"}");
                })
                .accessDeniedHandler((request, response, exception) -> {
                    response.setStatus(403);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"message\":\"安全驗證已過期，請重新整理頁面\"}");
                }));
        http.logout(logout -> logout.logoutUrl("/api/auth/logout").invalidateHttpSession(true)
                .deleteCookies("JSESSIONID").logoutSuccessHandler((request, response, authentication) -> response.setStatus(204)));
        return http.build();
    }
}
