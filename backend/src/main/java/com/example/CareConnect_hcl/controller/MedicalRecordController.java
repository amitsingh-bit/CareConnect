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

import com.example.CareConnect_hcl.model.entity.MedicalRecord;
import com.example.CareConnect_hcl.services.MedicalRecordService;

@RestController
@RequestMapping("/api/medical-records")
@CrossOrigin
public class MedicalRecordController {
    private final MedicalRecordService service;
    public MedicalRecordController(MedicalRecordService service) { this.service = service; }
    @GetMapping public List<MedicalRecord> getAll() { return service.getAll(); }
    @GetMapping("/{id}") public MedicalRecord getById(@PathVariable Long id) { return service.getById(id); }
    @PostMapping public MedicalRecord create(@RequestBody MedicalRecord record) { return service.create(record); }
    @PutMapping("/{id}") public MedicalRecord update(@PathVariable Long id, @RequestBody MedicalRecord record) { return service.update(id, record); }
    @DeleteMapping("/{id}") public void delete(@PathVariable Long id) { service.delete(id); }
}
