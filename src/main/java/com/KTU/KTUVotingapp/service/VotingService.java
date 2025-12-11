package com.KTU.KTUVotingapp.service;

import com.KTU.KTUVotingapp.dto.BulkVoteRequest;
import com.KTU.KTUVotingapp.dto.VoteRequest;
import com.KTU.KTUVotingapp.model.Candidate;
import com.KTU.KTUVotingapp.model.Category;
import com.KTU.KTUVotingapp.model.Vote;
import com.KTU.KTUVotingapp.model.Voter;
import com.KTU.KTUVotingapp.repository.CandidateRepository;
import com.KTU.KTUVotingapp.repository.VoteRepository;
import com.KTU.KTUVotingapp.repository.VoterRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class VotingService {

    private final VoterRepository voterRepository;
    private final VoteRepository voteRepository;
    private final CandidateRepository candidateRepository;

    public VotingService(VoterRepository voterRepository, VoteRepository voteRepository,
                        CandidateRepository candidateRepository) {
        this.voterRepository = voterRepository;
        this.voteRepository = voteRepository;
        this.candidateRepository = candidateRepository;
    }

    /**
     * Submit a single vote with pessimistic locking and transaction management.
     * Uses READ_COMMITTED isolation level for optimal performance with consistency.
     * Supports shared PINs - multiple devices can use the same PIN.
     *
     * Surgical change: remove explicit pessimistic locking calls to reduce long-lived DB locks
     * under high concurrency. Rely on DB unique constraints and DataIntegrityViolationException
     * to detect duplicates. This reduces transaction contention and moves locking responsibility
     * back to the DB (single responsibility) instead of the service.
     */
    @Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    @CacheEvict(value = {"results", "candidates"}, allEntries = true)
    public void submitVote(VoteRequest request) {
        // NOTE: removed pessimistic locking reads to shorten transaction duration and avoid
        // heavy lock contention. Use non-locking checks and let DB constraints guard uniqueness.

        // Step 1: Check device ID existence (non-locking quick check)
        Optional<Voter> existingDeviceVoter = voterRepository.findByDeviceId(request.getDeviceId());
        if (existingDeviceVoter.isPresent()) {
            Voter existingVoter = existingDeviceVoter.get();
            if (existingVoter.isHasVoted() || voteRepository.existsByVoter(existingVoter)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "This device has already submitted a vote");
            }
        }

        // Step 2: Get or create voter by device ID (PIN can be shared)
        Voter voter = getOrCreateVoter(request.getPin(), request.getDeviceId());

        if (voter.isHasVoted() || voteRepository.existsByVoter(voter)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                "This device has already submitted a vote");
        }

        // Step 3: Check if voter already voted in this category (non-locking existence check)
        if (voteRepository.existsByVoterAndCategory(voter, request.getCategory())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                "You have already voted in this category");
        }

        // Step 4: Get candidate
        Candidate candidate = candidateRepository.findByCategoryAndCandidateNumber(
                request.getCategory(), request.getCandidateNumber())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Candidate not found for category " + request.getCategory() +
                    " and number " + request.getCandidateNumber()));

        // Step 5: Create and save vote (database constraint will prevent duplicates)
        try {
            Vote vote = new Vote(voter, candidate, request.getCategory());
            voteRepository.save(vote);

            // Atomically increment candidate vote count in DB to avoid lost updates
            candidateRepository.incrementVoteCount(candidate.getId());

            // Update voter status
            if (!voter.isHasVoted()) {
                voter.setHasVoted(true);
                voter.setVotedAt(LocalDateTime.now());
                voterRepository.save(voter);
            }
        } catch (DataIntegrityViolationException e) {
            // Database constraint violation - handle gracefully
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                "Duplicate vote detected. You may have already voted in this category.");
        }
    }

    /**
     * Submit multiple votes in a single transaction (bulk voting).
     * All votes are processed atomically - either all succeed or all fail.
     * Supports shared PINs - multiple devices can use the same PIN.
     *
     * Surgical change: same approach as above, avoid pessimistic locking reads.
     */
    @Transactional(isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    @CacheEvict(value = {"results", "candidates"}, allEntries = true)
    public void submitBulkVotes(BulkVoteRequest request) {
        // Step 1: Check device ID existence (non-locking)
        Optional<Voter> existingDeviceVoter = voterRepository.findByDeviceId(request.getDeviceId());
        if (existingDeviceVoter.isPresent()) {
            Voter existingVoter = existingDeviceVoter.get();
            if (existingVoter.isHasVoted() || voteRepository.existsByVoter(existingVoter)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "This device has already submitted votes");
            }
        }

        // Step 2: Get or create voter by device ID (PIN can be shared)
        Voter voter = getOrCreateVoter(request.getPin(), request.getDeviceId());

        if (voter.isHasVoted() || voteRepository.existsByVoter(voter)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                "This device has already submitted votes");
        }

        // Step 3: Validate all votes before processing
        for (BulkVoteRequest.VoteItem voteItem : request.getVotes()) {
            // Check if already voted in this category
            if (voteRepository.existsByVoterAndCategory(voter, voteItem.getCategory())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "You have already voted in category: " + voteItem.getCategory());
            }

            // Validate candidate exists
            candidateRepository.findByCategoryAndCandidateNumber(
                    voteItem.getCategory(), voteItem.getCandidateNumber())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Candidate not found for category " + voteItem.getCategory() +
                        " and number " + voteItem.getCandidateNumber()));
        }

        // Step 4: Process all votes
        try {
            for (BulkVoteRequest.VoteItem voteItem : request.getVotes()) {
                Candidate candidate = candidateRepository.findByCategoryAndCandidateNumber(
                        voteItem.getCategory(), voteItem.getCandidateNumber())
                        .orElseThrow();

                Vote vote = new Vote(voter, candidate, voteItem.getCategory());
                voteRepository.save(vote);

                // Atomically increment candidate vote count in DB
                candidateRepository.incrementVoteCount(candidate.getId());
            }

            // Update voter status
            if (!voter.isHasVoted()) {
                voter.setHasVoted(true);
                voter.setVotedAt(LocalDateTime.now());
                voterRepository.save(voter);
            }
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                "Duplicate vote detected. Transaction rolled back.");
        }
    }

    /**
     * Get or create voter with pessimistic locking to prevent race conditions.
     * Supports shared PINs - multiple devices can use the same PIN.
     * Device ID is unique, PIN can be shared across multiple devices.
     *
     * Surgical change: avoid acquiring pessimistic locks here. Try to insert and on
     * unique constraint violation, read the existing record. This reduces lock time and
     * reliance on application-side locking.
     */
    private Voter getOrCreateVoter(String pin, String deviceId) {
        // Find by device ID first (non-locking quick check)
        Optional<Voter> voterOpt = voterRepository.findByDeviceId(deviceId);

        if (voterOpt.isPresent()) {
            // Device already exists - return it
            return voterOpt.get();
        }

        // Device doesn't exist - create new voter with this PIN and device ID
        // Multiple devices can share the same PIN
        try {
            Voter newVoter = new Voter(pin, deviceId);
            return voterRepository.save(newVoter);
        } catch (DataIntegrityViolationException e) {
            // Race condition - another thread created the voter with same device ID
            // Retry by finding it (non-locking)
            return voterRepository.findByDeviceId(deviceId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT,
                        "Failed to create voter. Please try again."));
        }
    }

    public boolean hasVoted(String pin, Category category) {
        Optional<Voter> voterOpt = voterRepository.findByPin(pin);
        if (voterOpt.isEmpty()) {
            return false;
        }
        return voteRepository.existsByVoterAndCategory(voterOpt.get(), category);
    }

    public boolean deviceHasVoted(String deviceId) {
        Optional<Voter> voterOpt = voterRepository.findByDeviceId(deviceId);
        return voterOpt.isPresent() && voterOpt.get().isHasVoted();
    }
}
