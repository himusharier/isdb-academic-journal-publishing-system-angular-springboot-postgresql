package com.himusharier.ajps_backend.dto.submission;

import jakarta.validation.constraints.NotBlank;

public record ManuscriptDetailsRequest(
        @NotBlank(message = "User Id is not found in request")
        Long userId,

        @NotBlank(message = "Journal name should not left blank.")
//        String journalName,
        Long journalId,

        @NotBlank(message = "Manuscript title should not left blank.")
        String manuscriptTitle,

        @NotBlank(message = "Manuscript category should not left blank.")
        String manuscriptCategory,

        @NotBlank(message = "Abstract should not left blank.")
        String abstractContent,

        @NotBlank(message = "Keywords should not left blank.")
        String manuscriptKeywords,

        String completedSteps,

        Long submissionId

) {
}
