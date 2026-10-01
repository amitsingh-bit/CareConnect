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

import com.example.CareConnect_hcl.model.entity.LabReport;
import com.example.CareConnect_hcl.services.LabReportService;

@RestController
@RequestMapping("/api/lab-reports")
@CrossOrigin
public class LabReportController {
    private final LabReportService service;
    public LabReportController(LabReportService service) { this.service = service; }
    @GetMapping public List<LabReport> getAll() { return service.getAll(); }
    @GetMapping("/{id}") public LabReport getById(@PathVariable Long id) { return service.getById(id); }
    @PostMapping public LabReport create(@RequestBody LabReport report) { return service.create(report); }
    @PutMapping("/{id}") public LabReport update(@PathVariable Long id, @RequestBody LabReport report) { return service.update(id, report); }
    @DeleteMapping("/{id}") public void delete(@PathVariable Long id) { service.delete(id); }
}
