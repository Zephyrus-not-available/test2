package com.KTU.KTUVotingapp.dto;

import com.KTU.KTUVotingapp.model.Category;

public class SubmittedVoteDTO {
    private Category category;
    private Integer candidateNumber;
    private String candidateName;

    public SubmittedVoteDTO() {
    }

    public SubmittedVoteDTO(Category category, Integer candidateNumber, String candidateName) {
        this.category = category;
        this.candidateNumber = candidateNumber;
        this.candidateName = candidateName;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public Integer getCandidateNumber() {
        return candidateNumber;
    }

    public void setCandidateNumber(Integer candidateNumber) {
        this.candidateNumber = candidateNumber;
    }

    public String getCandidateName() {
        return candidateName;
    }

    public void setCandidateName(String candidateName) {
        this.candidateName = candidateName;
    }
}

