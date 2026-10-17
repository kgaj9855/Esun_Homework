package com.Testesun.Testesunbank.Controller;
import com.Testesun.Testesunbank.DTO.CreateLikeListRequest;
import com.Testesun.Testesunbank.Model.LikeList;
import com.Testesun.Testesunbank.Service.LikeListService;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/likes")
public class LikeListController {
    private final LikeListService service;
    public LikeListController(LikeListService service) {
        this.service = service;
    }
    @GetMapping
    public List<LikeList> list(Principal principal) {
        return service.list(principal.getName());
    }
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LikeList create(Principal principal, @Valid @RequestBody CreateLikeListRequest request) {
        return service.create(principal.getName(), request);
    }
    @PutMapping("/{sn}")
    public LikeList update(Principal principal, @PathVariable long sn, @Valid @RequestBody CreateLikeListRequest request) {
        return service.update(principal.getName(), sn, request);
    }
    @DeleteMapping("/{sn}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(Principal principal, @PathVariable long sn) {
        service.delete(principal.getName(), sn);
    }
}
