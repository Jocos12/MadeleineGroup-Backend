package rw.madeleinegroup.dto;

import jakarta.validation.constraints.Size;

public class ReviewRequest {

    @Size(max = 5000)
    private String reviewNote;

    public String getReviewNote() {
        return reviewNote;
    }

    public void setReviewNote(String reviewNote) {
        this.reviewNote = reviewNote;
    }
}
