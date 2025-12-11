package com.KTU.KTUVotingapp.repository;

import com.KTU.KTUVotingapp.model.Candidate;
import com.KTU.KTUVotingapp.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface CandidateRepository extends JpaRepository<Candidate, Long> {

    List<Candidate> findByCategory(Category category);

    Optional<Candidate> findByCategoryAndCandidateNumber(Category category, Integer candidateNumber);

    @Query("SELECT c FROM Candidate c WHERE c.category = :category ORDER BY c.candidateNumber")
    List<Candidate> findByCategoryOrderByCandidateNumber(@Param("category") Category category);

    // Atomic DB-side increment to avoid lost updates under concurrency.
    @Modifying
    @Transactional
    @Query("UPDATE Candidate c SET c.voteCount = c.voteCount + 1 WHERE c.id = :id")
    int incrementVoteCount(@Param("id") Long id);
}
