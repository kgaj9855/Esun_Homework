package com.Testesun.Testesunbank.Service;

import com.Testesun.Testesunbank.DTO.CreateLikeListRequest;
import com.Testesun.Testesunbank.Model.LikeList;
import com.Testesun.Testesunbank.dao.BankDao;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LikeListService {
    private final BankDao dao;

    public LikeListService(BankDao dao) {
        this.dao = dao;
    }

    public List<LikeList> list(String userId) {
        return dao.likes(userId);
    }

    // 新增喜好商品，另外加入Transactional
    @Transactional
    public LikeList create(String userId, CreateLikeListRequest request) {
        LikeList row = dao.create(userId, request.getProductNo(), request.getPurchaseQuantity());
        dao.audit(userId, row.getSn(), "CREATE");
        return row;
    }

    // 更新
    @Transactional
    public LikeList update(String userId, long sn, CreateLikeListRequest request) {
        LikeList row = dao.update(userId, sn, request.getProductNo(), request.getPurchaseQuantity());
        dao.audit(userId, sn, "UPDATE");
        return row;
    }

    @Transactional
    public void delete(String userId, long sn) {
        dao.delete(userId, sn);
        dao.audit(userId, sn, "DELETE");
    }
}
