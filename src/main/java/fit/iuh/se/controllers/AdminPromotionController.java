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


    @GetMapping("/{id}")
    public ResponseEntity<?> getOne(@PathVariable Long id){
        return service.getById(id);
    }

    @PutMapping("/{id}/disable")
    public ResponseEntity<?> disable(@PathVariable Long id){
        return service.disable(id);
    }

    @PutMapping("/{id}/enable")
    public ResponseEntity<?> enable(@PathVariable Long id){
        return service.enable(id);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id,
                                    @RequestBody Promotion p){
        return service.update(id, p);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id){
        return service.delete(id);
    }

}
