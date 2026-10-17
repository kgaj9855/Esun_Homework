package com.Testesun.Testesunbank.Controller;

import com.Testesun.Testesunbank.Model.Product;
import com.Testesun.Testesunbank.Service.ProductService;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
public class ProductController {
    private final ProductService service;

    public ProductController(ProductService service) {
        this.service = service;
    }

    @GetMapping("/api/products")
    public List<Product> list() {
        return service.list();
    }
}
