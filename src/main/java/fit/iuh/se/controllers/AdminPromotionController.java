package fit.iuh.se.controllers;

import fit.iuh.se.entities.Promotion;
import fit.iuh.se.services.PromotionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/promotions")
public class AdminPromotionController {

    @Autowired
    private PromotionService service;

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Promotion p){
        return service.create(p);
    }

    @GetMapping
    public ResponseEntity<?> all(){
        return service.getAll();
    }

    @PutMapping("/{id}/disable")
    public ResponseEntity<?> disable(@PathVariable Long id){
        return service.disable(id);
    }
}
