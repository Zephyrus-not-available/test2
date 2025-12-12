package com.KTU.KTUVotingapp.controller;

import com.KTU.KTUVotingapp.dto.ResultDTO;
import com.KTU.KTUVotingapp.model.Category;
import com.KTU.KTUVotingapp.service.ResultService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    private final ResultService resultService;

    private String adminPin;

    public AdminController(ResultService resultService) {
        this.resultService = resultService;
    }

    /**
     * Returns aggregated vote counts in the shape:
     * {
     *   "KING": {"1": 10, "2": 5, ...},
     *   "QUEEN": {"1": 3, ...},
     *   ...
     * }
     * Requires adminPin query parameter for a basic auth check.
     */
    @GetMapping("/results")
    public ResponseEntity<?> getResults(@RequestParam("adminPin") String pin) {
        if (pin == null || !pin.equals(adminPin)) {
            return ResponseEntity.status(403).body("Forbidden");
        }

        Map<String, Map<Integer, Long>> results = new LinkedHashMap<>();

        // For each category, collect counts for candidate numbers
        for (Category category : Category.values()) {
            ResultDTO categoryResults = resultService.getResultsByCategory(category);
            Map<Integer, Long> counts = new LinkedHashMap<>();
            
            for (ResultDTO.CandidateResultDTO candidate : categoryResults.getCandidates()) {
                counts.put(candidate.getCandidateNumber(), candidate.getVoteCount());
            }
            
            results.put(category.name(), counts);
        }

        return ResponseEntity.ok(results);
    }

    /**
     * Get detailed results for all categories (with percentages).
     * Requires adminPin query parameter for a basic auth check.
     */
    @GetMapping("/results/detailed")
    public ResponseEntity<?> getDetailedResults(@RequestParam("adminPin") String pin) {
        if (pin == null || !pin.equals(adminPin)) {
            return ResponseEntity.status(403).body("Forbidden");
        }

        List<ResultDTO> results = resultService.getAllResults();
        return ResponseEntity.ok(results);
    }

    /**
     * Live admin flattened candidate results for dashboard polling.
     * GET /api/admin/results?pin=99999
     */
    @GetMapping(value = "/results", params = "pin")
    public ResponseEntity<java.util.List<ResultDTO.CandidateResultDTO>> getLiveAdminResults(@RequestParam("pin") String pin) {
        if (pin == null || !pin.equals(adminPin)) {
            return ResponseEntity.status(403).build();
        }

        java.util.List<ResultDTO> all = resultService.getAllResults();
        java.util.List<ResultDTO.CandidateResultDTO> candidates = all.stream()
                .flatMap(r -> r.getCandidates().stream())
                .sorted(java.util.Comparator.comparingLong(ResultDTO.CandidateResultDTO::getVoteCount).reversed())
                .collect(java.util.stream.Collectors.toList());

        return ResponseEntity.ok(candidates);
    }

    @PostMapping("/candidates")
    public org.springframework.http.ResponseEntity<?> createCandidate(@RequestParam("adminPin") String pin,
                                                                       @RequestBody com.KTU.KTUVotingapp.dto.CandidateDTO dto) {
        if (pin == null || !pin.equals(adminPin)) {
            return org.springframework.http.ResponseEntity.status(403).body("Forbidden");
        }

        org.springframework.web.context.WebApplicationContext ctx = org.springframework.web.context.support.WebApplicationContextUtils
                .getWebApplicationContext(((org.springframework.web.context.request.ServletRequestAttributes) org.springframework.web.context.request.RequestContextHolder.currentRequestAttributes()).getRequest().getServletContext());
        com.KTU.KTUVotingapp.repository.CandidateRepository repo = ctx.getBean(com.KTU.KTUVotingapp.repository.CandidateRepository.class);

        com.KTU.KTUVotingapp.model.Candidate candidate = new com.KTU.KTUVotingapp.model.Candidate();
        candidate.setCategory(dto.getCategory());
        candidate.setCandidateNumber(dto.getCandidateNumber());
        candidate.setName(dto.getName());
        candidate.setDepartment(dto.getDepartment());
        candidate.setImageUrl(dto.getImageUrl());
        candidate.setVoteCount(dto.getVoteCount() != null ? dto.getVoteCount() : 0L);

        com.KTU.KTUVotingapp.model.Candidate saved = repo.save(candidate);

        com.KTU.KTUVotingapp.dto.CandidateDTO response = new com.KTU.KTUVotingapp.dto.CandidateDTO(
                saved.getId(), saved.getCategory(), saved.getCandidateNumber(), saved.getName(), saved.getDepartment(), saved.getImageUrl(), saved.getVoteCount()
        );

        return org.springframework.http.ResponseEntity.ok(response);
    }

    @GetMapping("/candidates/{id}")
    public org.springframework.http.ResponseEntity<?> getCandidate(@RequestParam("adminPin") String pin, @PathVariable Long id) {
        if (pin == null || !pin.equals(adminPin)) {
            return org.springframework.http.ResponseEntity.status(403).body("Forbidden");
        }

        org.springframework.web.context.WebApplicationContext ctx = org.springframework.web.context.support.WebApplicationContextUtils
                .getWebApplicationContext(((org.springframework.web.context.request.ServletRequestAttributes) org.springframework.web.context.request.RequestContextHolder.currentRequestAttributes()).getRequest().getServletContext());
        com.KTU.KTUVotingapp.repository.CandidateRepository repo = ctx.getBean(com.KTU.KTUVotingapp.repository.CandidateRepository.class);

        java.util.Optional<com.KTU.KTUVotingapp.model.Candidate> found = repo.findById(id);
        if (found.isEmpty()) return org.springframework.http.ResponseEntity.notFound().build();

        com.KTU.KTUVotingapp.model.Candidate c = found.get();
        com.KTU.KTUVotingapp.dto.CandidateDTO response = new com.KTU.KTUVotingapp.dto.CandidateDTO(
                c.getId(), c.getCategory(), c.getCandidateNumber(), c.getName(), c.getDepartment(), c.getImageUrl(), c.getVoteCount()
        );
        return org.springframework.http.ResponseEntity.ok(response);
    }

    @PutMapping("/candidates/{id}")
    public org.springframework.http.ResponseEntity<?> updateCandidate(@RequestParam("adminPin") String pin, @PathVariable Long id,
                                                                       @RequestBody com.KTU.KTUVotingapp.dto.CandidateDTO dto) {
        if (pin == null || !pin.equals(adminPin)) {
            return org.springframework.http.ResponseEntity.status(403).body("Forbidden");
        }

        org.springframework.web.context.WebApplicationContext ctx = org.springframework.web.context.support.WebApplicationContextUtils
                .getWebApplicationContext(((org.springframework.web.context.request.ServletRequestAttributes) org.springframework.web.context.request.RequestContextHolder.currentRequestAttributes()).getRequest().getServletContext());
        com.KTU.KTUVotingapp.repository.CandidateRepository repo = ctx.getBean(com.KTU.KTUVotingapp.repository.CandidateRepository.class);

        com.KTU.KTUVotingapp.model.Candidate existing = repo.findById(id).orElse(null);
        if (existing == null) return org.springframework.http.ResponseEntity.notFound().build();

        if (dto.getCategory() != null) existing.setCategory(dto.getCategory());
        if (dto.getCandidateNumber() != null) existing.setCandidateNumber(dto.getCandidateNumber());
        if (dto.getName() != null) existing.setName(dto.getName());
        if (dto.getDepartment() != null) existing.setDepartment(dto.getDepartment());
        if (dto.getImageUrl() != null) existing.setImageUrl(dto.getImageUrl());
        if (dto.getVoteCount() != null) existing.setVoteCount(dto.getVoteCount());

        com.KTU.KTUVotingapp.model.Candidate saved = repo.save(existing);

        com.KTU.KTUVotingapp.dto.CandidateDTO response = new com.KTU.KTUVotingapp.dto.CandidateDTO(
                saved.getId(), saved.getCategory(), saved.getCandidateNumber(), saved.getName(), saved.getDepartment(), saved.getImageUrl(), saved.getVoteCount()
        );
        return org.springframework.http.ResponseEntity.ok(response);
    }

    @DeleteMapping("/candidates/{id}")
    public org.springframework.http.ResponseEntity<?> deleteCandidate(@RequestParam("adminPin") String pin, @PathVariable Long id) {
        if (pin == null || !pin.equals(adminPin) ){
            return org.springframework.http.ResponseEntity.status(403).body("Forbidden");
        }

        org.springframework.web.context.WebApplicationContext ctx = org.springframework.web.context.support.WebApplicationContextUtils
                .getWebApplicationContext(((org.springframework.web.context.request.ServletRequestAttributes) org.springframework.web.context.request.RequestContextHolder.currentRequestAttributes()).getRequest().getServletContext());
        com.KTU.KTUVotingapp.repository.CandidateRepository repo = ctx.getBean(com.KTU.KTUVotingapp.repository.CandidateRepository.class);

        if (!repo.existsById(id)) return org.springframework.http.ResponseEntity.notFound().build();
        repo.deleteById(id);
        return org.springframework.http.ResponseEntity.noContent().build();
    }
}
