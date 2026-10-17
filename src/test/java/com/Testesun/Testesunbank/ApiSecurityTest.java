package com.Testesun.Testesunbank;
import com.Testesun.Testesunbank.dao.BankDao;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@SpringBootTest(properties="app.database.initialize=false") @AutoConfigureMockMvc
class ApiSecurityTest {
    @Autowired MockMvc mvc;
    @MockitoBean BankDao dao;
    @Test void guestsCannotAccessPrivateData() throws Exception { mvc.perform(get("/api/likes")).andExpect(status().isUnauthorized()); }
    @Test void mutationRequiresCsrfEvenWhenAuthenticated() throws Exception {
        mvc.perform(post("/api/likes").with(user("alice")).contentType("application/json")
            .content("{\"productNo\":1,\"purchaseQuantity\":2}")).andExpect(status().isForbidden());
        verifyNoInteractions(dao);
    }
    @Test void quantityValidationRejectsZero() throws Exception {
        mvc.perform(post("/api/likes").with(user("alice")).with(csrf()).contentType("application/json")
            .content("{\"productNo\":1,\"purchaseQuantity\":0}")).andExpect(status().isBadRequest());
        verifyNoInteractions(dao);
    }
    @Test void ignoresForgedUserIdAndUsesPrincipal() throws Exception {
        mvc.perform(get("/api/likes?userId=bob").with(user("alice"))).andExpect(status().isOk());
        verify(dao).likes("alice"); verify(dao, never()).likes("bob");
    }
    @Test void registrationRequiresValidFields() throws Exception {
        mvc.perform(post("/api/auth/register").with(csrf()).contentType("application/json").content("{}"))
            .andExpect(status().isBadRequest()); verifyNoInteractions(dao);
    }
}
