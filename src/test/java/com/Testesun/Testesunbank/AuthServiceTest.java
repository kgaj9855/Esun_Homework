package com.Testesun.Testesunbank;
import com.Testesun.Testesunbank.DTO.*;
import com.Testesun.Testesunbank.Model.User;
import com.Testesun.Testesunbank.Service.AuthService;
import com.Testesun.Testesunbank.common.ApiException;
import com.Testesun.Testesunbank.dao.BankDao;
import org.junit.jupiter.api.*;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class AuthServiceTest {
    BankDao dao = mock(BankDao.class);
    BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(4);
    AuthService service = new AuthService(dao, encoder);
    @Test void registrationHashesPasswordAndNormalizesEmail() {
        when(dao.register(any())).thenAnswer(invocation -> { User u = invocation.getArgument(0); return new User("1", u.getUserName(), u.getEmail(), u.getAccount(), u.getPasswordHash()); });
        User registered = service.register(new RegisterRequest(" 王小明 ", "Member@Example.com", "123456789012", "safe-password123"));
        var captor = ArgumentCaptor.forClass(User.class); verify(dao).register(captor.capture());
        User u = captor.getValue();
        assertNull(u.getUserId());
        assertEquals("1", registered.getUserId());
        assertEquals("member@example.com", u.getEmail()); assertEquals("王小明", u.getUserName());
        assertTrue(encoder.matches("safe-password123", u.getPasswordHash()));
        assertNotEquals("safe-password123", u.getPasswordHash());
    }
    @Test void userJsonKeepsFieldNamesAndNeverIncludesPasswordHash() throws Exception {
        User user = new User("1", "測試會員", "test@example.com", "123456789012", "secret-hash");
        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        com.fasterxml.jackson.databind.JsonNode json = mapper.readTree(mapper.writeValueAsString(user));
        assertEquals("1", json.get("userId").asText());
        assertEquals("測試會員", json.get("userName").asText());
        assertFalse(json.has("passwordHash"));
    }
    @Test void refusesOverlongUtf8Password() {
        assertThrows(ApiException.class, () -> service.register(new RegisterRequest("Name", "a@b.com", "123456789012", "密".repeat(25))));
        verifyNoInteractions(dao);
    }
    @Test void wrongOrUnknownCredentialsHaveSameError() {
        when(dao.userByEmail("a@b.com")).thenReturn(new User("alice", "Alice", "a@b.com", "123456789012", encoder.encode("correct-password")));
        when(dao.userByEmail("unknown@b.com")).thenReturn(null);
        var wrong = assertThrows(ApiException.class, () -> service.login(new LoginRequest("a@b.com", "wrong-password")));
        var unknown = assertThrows(ApiException.class, () -> service.login(new LoginRequest("unknown@b.com", "wrong-password")));
        assertEquals(wrong.getMessage(), unknown.getMessage());
    }
}
