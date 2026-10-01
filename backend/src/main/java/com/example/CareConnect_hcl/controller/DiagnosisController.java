package com.example.CareConnect_hcl.controller;

import java.util.List;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.CareConnect_hcl.model.entity.Diagnosis;
import com.example.CareConnect_hcl.services.DiagnosisService;

@RestController
@RequestMapping("/api/diagnoses")
@CrossOrigin
public class DiagnosisController {
    private final DiagnosisService service;
    public DiagnosisController(DiagnosisService service) { this.service = service; }
    @GetMapping public List<Diagnosis> getAll() { return service.getAll(); }
    @GetMapping("/{id}") public Diagnosis getById(@PathVariable Long id) { return service.getById(id); }
    @PostMapping public Diagnosis create(@RequestBody Diagnosis diagnosis) { return service.create(diagnosis); }
    @PutMapping("/{id}") public Diagnosis update(@PathVariable Long id, @RequestBody Diagnosis diagnosis) { return service.update(id, diagnosis); }
    @DeleteMapping("/{id}") public void delete(@PathVariable Long id) { service.delete(id); }
}
