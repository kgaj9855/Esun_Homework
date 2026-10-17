package com.Testesun.Testesunbank.dao;

import com.Testesun.Testesunbank.Model.User;
import com.Testesun.Testesunbank.Model.Product;
import com.Testesun.Testesunbank.Model.LikeList;
import java.util.List;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

// DAO 只負責呼叫資料庫程序，商業邏輯放在 Service。
@Repository
public class BankDao {
    private final JdbcTemplate jdbc;

    public BankDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // 查到會員就回傳 User，查不到就回傳 null。
    public User userByEmail(String email) {
        // RowMapper 會透過 setter，把 user_name 等欄位填入 userName。
        String sql = "CALL sp_get_user_by_email(?)";
        List<User> users = jdbc.query(sql, new BeanPropertyRowMapper<>(User.class), email);
        if (users.isEmpty()) {
            return null;
        }
        return users.get(0);
    }

    public User register(User user) {
        String sql = "CALL sp_register_user(?,?,?,?)";
        List<User> users = jdbc.query(sql, new BeanPropertyRowMapper<>(User.class),
                user.getUserName(), user.getEmail(), user.getAccount(), user.getPasswordHash());
        return users.get(0);
    }

    public List<Product> products() {
        String sql = "CALL sp_get_products()";
        return jdbc.query(sql, new BeanPropertyRowMapper<>(Product.class));
    }

    public List<LikeList> likes(String userId) {
        String sql = "CALL sp_get_like_list_by_user(?)";
        return jdbc.query(sql, new BeanPropertyRowMapper<>(LikeList.class), userId);
    }

    public LikeList create(String userId, long productNo, int quantity) {
        String sql = "CALL sp_create_like_list(?,?,?)";
        List<LikeList> likes = jdbc.query(sql, new BeanPropertyRowMapper<>(LikeList.class),
                userId, productNo, quantity);
        return likes.get(0);
    }

    public LikeList update(String userId, long sn, long productNo, int quantity) {
        String sql = "CALL sp_update_like_list(?,?,?,?)";
        List<LikeList> likes = jdbc.query(sql, new BeanPropertyRowMapper<>(LikeList.class),
                userId, sn, productNo, quantity);
        return likes.get(0);
    }

    public void delete(String userId, long sn) {
        jdbc.update("CALL sp_delete_like_list(?,?)", userId, sn);
    }

    public void audit(String userId, long sn, String action) {
        jdbc.update("CALL sp_record_like_event(?,?,?)", userId, sn, action);
    }
}
