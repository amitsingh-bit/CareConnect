package com.example.CareConnect_hcl.controller;

import com.example.CareConnect_hcl.model.entity.Patient;
import com.example.CareConnect_hcl.services.PatientService;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/patients")
@CrossOrigin
public class PatientController {

    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }
    @PostMapping
    public Patient savePatient(@RequestBody Patient patient) {
        return patientService.savePatient(patient);
    }
    @GetMapping
    public List<Patient> getAllPatients() {
        return patientService.getAllPatients();
    }
    @GetMapping("/{id}")
    public Patient getPatientById(@PathVariable Long id) {
        return patientService.getPatientById(id);
    }
    @PutMapping("/{id}")
    public Patient updatePatient(
            @PathVariable Long id,
            @RequestBody Patient patient) {

        return patientService.updatePatient(id, patient);
    }
    @DeleteMapping("/{id}")
    public String deletePatient(@PathVariable Long id) {

        patientService.deletePatient(id);

        return "Patient deleted successfully";
    }

    @PutMapping("/{id}/nurses/{nurseId}")
    public Patient assignNurse(@PathVariable Long id, @PathVariable Long nurseId) {
        return patientService.assignNurse(id, nurseId);
    }

    @GetMapping("/{id}/nurses")
    public List<com.example.CareConnect_hcl.model.entity.Nurse> getAssignedNurses(@PathVariable Long id) {
        return patientService.getAssignedNurses(id);
    }

    @DeleteMapping("/{id}/nurses/{nurseId}")
    public Patient unassignNurse(@PathVariable Long id, @PathVariable Long nurseId) {
        return patientService.unassignNurse(id, nurseId);
    }
}
