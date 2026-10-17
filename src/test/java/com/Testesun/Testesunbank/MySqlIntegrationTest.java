package com.Testesun.Testesunbank;
import com.Testesun.Testesunbank.DTO.*;
import com.Testesun.Testesunbank.Model.LikeList;
import com.Testesun.Testesunbank.Service.*;
import com.Testesun.Testesunbank.dao.BankDao;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
@EnabledIfEnvironmentVariable(named="RUN_DB_TESTS", matches="true")
class MySqlIntegrationTest {
    @Autowired AuthService auth;
    @Autowired LikeListService likes;
    @MockitoSpyBean BankDao dao;
    @Autowired JdbcTemplate jdbc;
    @Autowired PlatformTransactionManager manager;
    @Autowired org.springframework.boot.autoconfigure.jdbc.DataSourceProperties dataSourceProperties;
    @Test void startupInitializationCanRepeatWithoutLosingData() throws Exception {
        int members = jdbc.queryForObject("SELECT COUNT(*) FROM users", Integer.class);
        int favorites = jdbc.queryForObject("SELECT COUNT(*) FROM like_list", Integer.class);
        var config = new com.Testesun.Testesunbank.config.DatabaseConfig();
        try (var repeated = config.dataSource(dataSourceProperties, true)) {
            var check = new JdbcTemplate(repeated);
            assertEquals(members, check.queryForObject("SELECT COUNT(*) FROM users", Integer.class));
            assertEquals(favorites, check.queryForObject("SELECT COUNT(*) FROM like_list", Integer.class));
            assertTrue(check.queryForObject("SELECT COUNT(*) FROM products", Integer.class) >= 6);
            assertFalse(check.query("CALL sp_get_products()", (r, n) -> r.getLong("no")).isEmpty());
        }
    }
    @Test void failedAuditRollsBackFavoriteAndRegistration() {
        String id = "t" + UUID.randomUUID().toString().replace("-", "").substring(0,15);
        String email = id + "@example.test";
        TransactionTemplate tx = new TransactionTemplate(manager);
        try {
            doThrow(new IllegalStateException("audit failure")).when(dao).audit(anyString(), anyLong(), eq("CREATE"));
            assertThrows(org.springframework.transaction.UnexpectedRollbackException.class, () -> tx.executeWithoutResult(status -> {
                var registered = auth.register(new RegisterRequest("Rollback",email,"123456789012","integration-password"));
                var product = dao.products().getFirst();
                assertThrows(IllegalStateException.class, () -> likes.create(registered.getUserId(), new CreateLikeListRequest(product.getNo(),1)));
                assertTrue(status.isRollbackOnly());
            }));
            assertNull(dao.userByEmail(email));
            assertTrue(dao.likes(id).isEmpty());
        } finally { reset(dao); }
    }
    @Test void proceduresCrudIsolationMoneyAndRollback() {
        String id = "t" + UUID.randomUUID().toString().replace("-", "").substring(0, 15);
        String email = id + "@example.test";
        TransactionTemplate tx = new TransactionTemplate(manager);
        tx.executeWithoutResult(status -> {
            String memberId = auth.register(new RegisterRequest("測試會員", email, "123456789012", "integration-password")).getUserId();
            assertTrue(memberId.matches("[0-9]+"));
            assertEquals(memberId, auth.login(new LoginRequest(email, "integration-password")).getUserId());
            var p = dao.products().getFirst();
            LikeList row = likes.create(memberId, new CreateLikeListRequest(p.getNo(), 3));
            BigDecimal expectedFee = p.getPrice().multiply(BigDecimal.valueOf(3)).multiply(p.getFeeRate()).setScale(2, java.math.RoundingMode.HALF_UP);
            assertEquals(0, expectedFee.compareTo(row.getTotalFee()));
            assertEquals(0, p.getPrice().multiply(BigDecimal.valueOf(3)).add(expectedFee).compareTo(row.getTotalAmount()));
            assertEquals("123456789012", row.getAccount());
            assertTrue(likes.list("other-user").stream().noneMatch(l -> l.getSn() == row.getSn()));
            assertThrows(DataAccessException.class, () -> dao.update("other-user", row.getSn(), p.getNo(), 2));
            assertThrows(DataAccessException.class, () -> dao.delete("other-user", row.getSn()));
            // Same values are still a valid update (not a false 404).
            assertEquals(3, likes.update(memberId, row.getSn(), new CreateLikeListRequest(p.getNo(), 3)).getPurchaseQuantity());
            assertEquals(5, likes.update(memberId, row.getSn(), new CreateLikeListRequest(p.getNo(), 5)).getPurchaseQuantity());
            likes.delete(memberId, row.getSn());
            assertTrue(likes.list(memberId).isEmpty());
            assertEquals(4, jdbc.queryForObject("SELECT COUNT(*) FROM like_events WHERE user_id=?", Integer.class, memberId));
            status.setRollbackOnly();
        });
        assertNull(dao.userByEmail(email));
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM like_events WHERE user_id=?", Integer.class, id));
    }
}
