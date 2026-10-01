package com.example.CareConnect_hcl.controller;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;
import com.example.CareConnect_hcl.model.entity.Vital;
import com.example.CareConnect_hcl.services.VitalService;

@RestController
@RequestMapping("/api/vitals")
@CrossOrigin
public class VitalController {
    private final VitalService service;
    public VitalController(VitalService service) { this.service = service; }
    @GetMapping public List<Vital> getAll() { return service.getAll(); }
    @GetMapping("/{id}") public Vital getById(@PathVariable Long id) { return service.getById(id); }
    @PostMapping public Vital create(@RequestBody Vital vital) { return service.create(vital); }
}
