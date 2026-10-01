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

import com.example.CareConnect_hcl.model.entity.Nurse;
import com.example.CareConnect_hcl.services.NurseService;

@RestController
@RequestMapping("/api/nurses")
@CrossOrigin
public class NurseController {
    private final NurseService service;
    public NurseController(NurseService service) { this.service = service; }
    @GetMapping public List<Nurse> getAll() { return service.getAll(); }
    @GetMapping("/{id}") public Nurse getById(@PathVariable Long id) { return service.getById(id); }
    @PostMapping public Nurse create(@RequestBody Nurse nurse) { return service.create(nurse); }
    @PutMapping("/{id}") public Nurse update(@PathVariable Long id, @RequestBody Nurse nurse) { return service.update(id, nurse); }
    @DeleteMapping("/{id}") public void delete(@PathVariable Long id) { service.delete(id); }
}
