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

    @Value("${admin.pin:99999}")
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
}

