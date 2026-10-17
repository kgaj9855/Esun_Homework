package com.Testesun.Testesunbank.Service;
import com.Testesun.Testesunbank.Model.Product;
import com.Testesun.Testesunbank.dao.BankDao;
import java.util.List;
import org.springframework.stereotype.Service;
@Service
public class ProductService {
    private final BankDao dao;
    public ProductService(BankDao dao) {
        this.dao = dao;
    }
    public List<Product> list() {
        return dao.products();
    }
}
