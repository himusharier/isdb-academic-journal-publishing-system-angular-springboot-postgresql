// src/main/java/com/himusharier/ajps_backend/service/SubmissionService.java
package com.himusharier.ajps_backend.service;

import com.himusharier.ajps_backend.constants.FileUploadOrigin;
import com.himusharier.ajps_backend.constants.SubmissionStatus;
import com.himusharier.ajps_backend.dto.response.submission.*; // Import new DTOs
import com.himusharier.ajps_backend.dto.submission.*;
import com.himusharier.ajps_backend.exception.SubmissionRequestException;
import com.himusharier.ajps_backend.model.*;
import com.himusharier.ajps_backend.repository.*;
import com.himusharier.ajps_backend.util.BdtZoneTimeUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SubmissionService {

    @Value("${app.base.url}")
    private String baseUrl;

    @Value("${file.upload-dir}")
    private String uploadDirectory;

    private final SubmissionRepository submissionRepository;
    private final AuthorRepository authorRepository;
    private final FileUploadRepository fileUploadRepository;
    private final SubmissionReviewerRepository submissionReviewerRepository;
    private final JournalRepository journalRepository;

    public SubmissionService(
            SubmissionRepository submissionRepository,
            AuthorRepository authorRepository,
            FileUploadRepository fileUploadRepository,
            SubmissionReviewerRepository submissionReviewerRepository,
            JournalRepository journalRepository
    ) {
        this.submissionRepository = submissionRepository;
        this.authorRepository = authorRepository;
        this.fileUploadRepository = fileUploadRepository;
        this.submissionReviewerRepository = submissionReviewerRepository;
        this.journalRepository = journalRepository;
    }

    // Existing method to return raw Submission entity
    public Submission returnSubmissionDetails(Profile profile, Long submissionId) {
        // Using findByIdAndProfile ensures the submission belongs to the user
        Optional<Submission> optionalSubmission = submissionRepository.findByIdAndProfile(submissionId, profile);

        // Crucial for lazy loading: explicitly initialize collections if you return the entity directly
        // If using DTOs as suggested, the mapping process will implicitly initialize them.
        optionalSubmission.ifPresent(submission -> {
            submission.getAuthors().size(); // Forces initialization
            submission.getFiles().size();
            submission.getSubmissionReviewers().size();
            // submission.getJournal().getJournalName(); // Also accesses related entity
            // Access journal to ensure it's loaded if needed for the response
            if (submission.getJournal() != null) {
                submission.getJournal().getId();
            }
        });

        return optionalSubmission.orElseThrow(() ->
                new SubmissionRequestException("Submission not found for given user and submission ID."));
    }

    // New method to return SubmissionDetailsResponse DTO
    @Transactional(readOnly = true)
    public SubmissionDetailsResponse getSubmissionDetailsAsDto(Profile profile, Long submissionId) {
        Submission submission = returnSubmissionDetails(profile, submissionId);

        return SubmissionDetailsResponse.builder()
                .id(submission.getId())
                .submissionNumber(submission.getSubmissionNumber())
                .journal(submission.getJournal() != null ?
                        JournalResponse.builder()
                                .id(submission.getJournal().getId())
                                .journalName(submission.getJournal().getJournalName())
                                .build() : null)
                // Add owner details if needed for the DTO
                .manuscriptTitle(submission.getManuscriptTitle())
                .manuscriptCategory(submission.getManuscriptCategory())
                .abstractContent(submission.getAbstractContent())
                .manuscriptKeywords(submission.getManuscriptKeywords())
                .comments(submission.getComments())
                .submissionConfirmation(submission.isSubmissionConfirmation())
                .submissionStatus(submission.getSubmissionStatus())
                .createdAt(submission.getCreatedAt())
                .submittedAt(submission.getSubmittedAt())
                .updatedAt(submission.getUpdatedAt())
                .isPaymentDue(submission.isPaymentDue())
                .completedSteps(submission.getCompletedSteps())
                .isEditable(submission.isEditable())
                .authors(submission.getAuthors() != null ? submission.getAuthors().stream()
                        .map(author -> AuthorResponse.builder()
                                .id(author.getId())
                                .name(author.getName())
                                .email(author.getEmail())
                                .institution(author.getInstitution())
                                .corresponding(author.isCorresponding())
                                .build())
                        .collect(Collectors.toList()) : List.of())
                .files(submission.getFiles() != null ? submission.getFiles().stream()
                        .map(file -> FileUploadResponse.builder()
                                .id(file.getId())
                                .fileOrigin(file.getFileOrigin().name())
                                .storedName(file.getStoredName())
                                .originalName(file.getOriginalName())
                                .size(file.getSize())
                                .type(file.getType())
                                .fileUrl(file.getFileUrl())
                                .isReviewFile(file.isReviewFile()) // --- INCLUDE NEW FIELD ---
                                .isCopyEditingFile(file.isCopyEditingFile())
                                .isProductionFile(file.isProductionFile()) //
                                .build())
                        .collect(Collectors.toList()) : List.of())
                .submissionReviewers(submission.getSubmissionReviewers() != null ? submission.getSubmissionReviewers().stream()
                        .map(reviewer -> SubmissionReviewerResponse.builder()
                                .id(reviewer.getId())
                                .name(reviewer.getName())
                                .email(reviewer.getEmail())
                                .institution(reviewer.getInstitution())
                                .build())
                        .collect(Collectors.toList()) : List.of())
                .build();
    }

    public Submission returnSubmission(Long submissionId) {
        Optional<Submission> optionalSubmission = submissionRepository.findById(submissionId);

        if (optionalSubmission.isPresent()) {
            return optionalSubmission.get();

        } else {
            throw new SubmissionRequestException("Submission not found for given submission ID.");
        }
    }

    @Transactional
    public Long saveManuscriptDetails(ManuscriptDetailsRequest request, Profile profile) {
        Journal journal = journalRepository.findById(request.journalId())
                .orElseThrow(() -> new SubmissionRequestException("Journal not found with ID: " + request.journalId()));

        Submission submission = Submission.builder()
                .profile(profile)
                .journal(journal) // Set the Journal entity
                .manuscriptTitle(request.manuscriptTitle())
                .manuscriptCategory(request.manuscriptCategory())
                .abstractContent(request.abstractContent())
                .manuscriptKeywords(request.manuscriptKeywords())
                .comments("") // Default empty comments
                .completedSteps(request.completedSteps())
                .submissionConfirmation(false) // Default value
                .submissionStatus(SubmissionStatus.SAVED) // Default status
                .isPaymentDue(false) // Default value
                .isEditable(true) // Default value
                .build();
        submissionRepository.save(submission);
        return submission.getId();
    }

    @Transactional
    public Submission updateManuscriptDetails(ManuscriptDetailsRequest request) {
        Submission submission = returnSubmission(request.submissionId());
        Journal journal = journalRepository.findById(request.journalId())
                .orElseThrow(() -> new SubmissionRequestException("Journal not found with ID: " + request.journalId()));

        submission.setJournal(journal); // Update the Journal entity
        submission.setManuscriptTitle(request.manuscriptTitle());
        submission.setManuscriptCategory(request.manuscriptCategory());
        submission.setAbstractContent(request.abstractContent());
        submission.setManuscriptKeywords(request.manuscriptKeywords());
        submission.setCompletedSteps(request.completedSteps());
        return submissionRepository.save(submission);
    }

    @Transactional
    public void updateCompletedSteps(Long submissionId, List<String> completedSteps) {
        Submission existingSubmission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new RuntimeException("Submission not found with ID: " + submissionId));

        // Use LinkedHashSet to preserve insertion order and avoid duplicates
        Set<String> steps = new LinkedHashSet<>();

        // Load existing steps (split by comma)
        if (existingSubmission.getCompletedSteps() != null && !existingSubmission.getCompletedSteps().isEmpty()) {
            Arrays.stream(existingSubmission.getCompletedSteps().split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .forEach(steps::add);
        }

        // Add new steps, trim and filter empty strings
        completedSteps.stream()
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .forEach(steps::add);

        // Set updated comma-separated steps back to entity
        existingSubmission.setCompletedSteps(String.join(",", steps));
        submissionRepository.save(existingSubmission);
    }

    @Transactional
    public List<Author> saveAuthors(List<AuthorDTO> authors, Submission submission) {
        List<Author> savedAuthors = new ArrayList<>();

        for (AuthorDTO authorInfo : authors) {
            Author author = new Author();
            author.setName(authorInfo.name());
            author.setEmail(authorInfo.email());
            author.setInstitution(authorInfo.institution());
            author.setCorresponding(authorInfo.corresponding());
            author.setSubmission(submission);

            savedAuthors.add(authorRepository.save(author));
        }

        return savedAuthors;
    }

    @Transactional
    public void removeAuthor(Long submissionId, Long authorId) {
        authorRepository.deleteBySubmissionIdAndId(submissionId, authorId);
    }

    public FileUpload saveFile(Long submissionId, String fileOrigin, MultipartFile file) throws IOException {
        if (file.isEmpty() || file.getOriginalFilename() == null) {
            throw new IOException("Invalid file: file is empty or has no original name");
        }
        String originalName = file.getOriginalFilename();
        String fileExtension = originalName.substring(originalName.lastIndexOf('.') + 1).toLowerCase();
        String storedName = UUID.randomUUID() + "." + fileExtension;

        Path uploadPath = Paths.get(uploadDirectory);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        Path filePath = uploadPath.resolve(storedName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        String fullFileUrl = baseUrl + "/" + uploadDirectory + "/" + storedName;

        Submission existingSubmission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new RuntimeException("Submission not found with ID: " + submissionId));

        FileUpload fileUpload = FileUpload.builder()
                .submission(existingSubmission)
                .fileOrigin(FileUploadOrigin.valueOf(fileOrigin))
                .originalName(originalName)
                .storedName(storedName)
                .type(file.getContentType())
                .size(file.getSize())
                .fileUrl(fullFileUrl)
                .isReviewFile(false)
                .isCopyEditingFile(false)
                .build();

        return fileUploadRepository.save(fileUpload);
    }

    public void deleteFile(Long submissionId, Long fileId) throws IOException {
        // Fetch the file to get its stored name for physical deletion
        FileUpload fileToDelete = fileUploadRepository.findById(fileId)
                .orElseThrow(() -> new SubmissionRequestException("File not found with ID: " + fileId));

        // Ensure the file belongs to the correct submission before deleting
        if (!fileToDelete.getSubmission().getId().equals(submissionId)) {
            throw new SubmissionRequestException("File does not belong to the specified submission.");
        }

        Path uploadPath = Paths.get(uploadDirectory);
        Path filePath = uploadPath.resolve(fileToDelete.getStoredName());
        Files.deleteIfExists(filePath); // Physically delete the file

        fileUploadRepository.delete(fileToDelete); // Delete from database
    }


    public List<SubmissionReviewer> saveReviewers(ReviewerSubmissionRequest request) {
        Submission submission = submissionRepository.findById(request.submissionId())
                .orElseThrow(() -> new IllegalArgumentException("Submission not found"));

        List<SubmissionReviewer> savedSubmissionReviewers = new ArrayList<>();

        for (ReviewerDTO dto : request.reviewers()) {
            SubmissionReviewer submissionReviewer = new SubmissionReviewer(
                    dto.name(),
                    dto.email(),
                    dto.institution(),
                    submission
            );
            SubmissionReviewer savedSubmissionReviewer = submissionReviewerRepository.save(submissionReviewer);
            savedSubmissionReviewers.add(savedSubmissionReviewer);
        }
        return savedSubmissionReviewers;
    }

    public List<ReviewerDTO> getReviewersBySubmissionId(Long submissionId) {
        return submissionReviewerRepository.findBySubmissionId(submissionId)
                .stream()
                .map(r -> new ReviewerDTO(
                        r.getId(), // Assuming ReviewerDTO has an ID field now
                        r.getName(),
                        r.getEmail(),
                        r.getInstitution()
                ))
                .collect(Collectors.toList());
    }

    @Transactional
    public void removeReviewer(Long submissionId, Long reviewerId) {
        submissionReviewerRepository.deleteByIdAndSubmissionId(reviewerId, submissionId);
    }

    @Transactional
    public Submission saveAdditionalInformation(AdditionalInformationRequest request) {
        Submission submission = submissionRepository.findById(request.submissionId())
                .orElseThrow(() -> new SubmissionRequestException("Submission not found with ID: " + request.submissionId()));

        submission.setComments(request.comments());
        submission.setSubmissionConfirmation(request.confirmed());
        if (request.completedSteps() != null && !request.completedSteps().isEmpty()) {
            updateCompletedSteps(request.submissionId(), request.completedSteps());
        }

        return submissionRepository.save(submission);
    }

    @Transactional
    public Submission submitManuscript(SubmitManuscriptRequest request) {
        Submission submission = submissionRepository.findById(request.submissionId())
                .orElseThrow(() -> new SubmissionRequestException("Submission not found with ID: " + request.submissionId()));

        try {
            submission.setSubmissionStatus(SubmissionStatus.valueOf(request.submissionStatus()));
            if (request.completedSteps() != null && !request.completedSteps().isEmpty()) {
                updateCompletedSteps(request.submissionId(), request.completedSteps());
            }

        } catch (IllegalArgumentException e) {
            throw new SubmissionRequestException("Invalid submission status: " + request.submissionStatus());
        }

        submission.setSubmissionDateTime(); // Sets submittedAt using BdtZoneTimeUtil
//        submission.setEditable(false); // Lock submission after final submission

        return submissionRepository.save(submission);
    }

    @Transactional
    public void deleteSubmission(Long submissionId) {
        // First delete associated files physically from storage
        List<FileUpload> files = fileUploadRepository.findBySubmissionId(submissionId);
        for (FileUpload file : files) {
            try {
                Path uploadPath = Paths.get(uploadDirectory);
                Path filePath = uploadPath.resolve(file.getStoredName());
                Files.deleteIfExists(filePath);
            } catch (IOException e) {
                // Log the exception, but don't prevent DB deletion if file system is the only issue
                System.err.println("Failed to delete physical file: " + file.getOriginalName() + " - " + e.getMessage());
            }
        }

        // The cascade will handle authors, files, and reviewers deletion from DB
        submissionRepository.deleteById(submissionId);
    }

    public void updateSubmissionStatus(Long submissionId, String newStatusString)
            throws Exception {

        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new SubmissionRequestException("Submission not found with ID: " + submissionId));

        // --- Business Logic for Status Transitions ---
        // This is crucial. You need to define valid state transitions.
        // For simplicity, I'm using a direct string comparison, but an enum or
        // a more complex state machine pattern would be better for real-world.

//        String currentStatus = submission.getSubmissionStatus().toString(); // Assuming your entity has this field

        // Example business rules:
        /*switch (newStatusString) {
            case "SENT_TO_REVIEW":
                // Only allow sending to review from "Submitted" or "Under Review" (if re-submitting)
                if (!"SUBMITTED".equals(currentStatus) && !"UNDER_REVIEW".equals(currentStatus)) {
                    throw new SubmissionRequestException("Cannot send to review from status: " + currentStatus);
                }
                break;
            case "ACCEPTED":
                // Only allow acceptance from "Under Review" or "Submitted" (if skipping review)
                if (!"UNDER_REVIEW".equals(currentStatus) && !"SUBMITTED".equals(currentStatus)) {
                    throw new SubmissionRequestException("Cannot accept from status: " + currentStatus);
                }
                break;
            case "REJECTED":
                // Can be rejected from various stages
                if ("ACCEPTED".equals(currentStatus) || "PUBLISHED".equals(currentStatus)) {
                    throw new SubmissionRequestException("Cannot reject an accepted or published submission.");
                }
                break;
            case "COPYEDITING": // New status for after 'Accept and Skip Review'
                if (!"ACCEPTED".equals(currentStatus) && !"IN_PRODUCTION".equals(currentStatus)) {
                    throw new SubmissionRequestException("Cannot move to copyediting from status: " + currentStatus);
                }
                break;
            // Add more cases for other statuses (e.g., IN_PRODUCTION, PUBLISHED)
            default:
                throw new SubmissionRequestException("Invalid or unsupported new status: " + newStatusString);
        }*/

        // Update the status in the entity
        submission.setSubmissionStatus(SubmissionStatus.valueOf(newStatusString)); // Assuming a setter exists
        // You might also want to set a timestamp for status change, e.g., submission.setUpdatedAt(new Date());

        submissionRepository.save(submission); // Save the updated submission
    }

    // --- NEW METHOD FOR SENDING TO REVIEW ---
    @Transactional // Ensures both status update and file marking succeed or fail together
    public Submission sendSubmissionToReview(Long submissionId, Long fileIdForReview) {
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new SubmissionRequestException("Submission not found with ID: " + submissionId));

        boolean fileFoundAndMarked = false;
        if (submission.getFiles() != null) {
            for (FileUpload file : submission.getFiles()) {
                if (file.getId().equals(fileIdForReview)) {
                    file.setReviewFile(true); // Mark this file for review
                    fileFoundAndMarked = true;
//                    fileUploadRepository.save(file);
                } else {
                    file.setReviewFile(false); // Unmark any other files
                }
                // Due to CascadeType.ALL on `files` list in Submission,
                // changes to FileUpload objects within the collection
                // will be persisted when the parent Submission is saved.
                // fileUploadRepository.save(file); // No need to explicitly save each file
            }
        }

        if (!fileFoundAndMarked) {
            throw new SubmissionRequestException("File with ID: " + fileIdForReview + " not found within submission ID: " + submissionId);
        }

        // Set submission status to UNDER_REVIEW
        submission.setSubmissionStatus(SubmissionStatus.UNDER_REVIEW);
        submission.setUpdatedAt(BdtZoneTimeUtil.timeInBDT());

        // Save the submission, which will cascade changes to its associated FileUploads
        return submissionRepository.save(submission);
    }

    // --- NEW METHOD: ACCEPT AND SKIP REVIEW ---
    @Transactional
    public Submission acceptAndSkipReview(Long submissionId) {
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new SubmissionRequestException("Submission not found with ID: " + submissionId));

        // Optional business logic: Ensure current status allows skipping review
        /*if (submission.getSubmissionStatus() != SubmissionStatus.SUBMITTED &&
                submission.getSubmissionStatus() != SubmissionStatus.SAVED &&
                submission.getSubmissionStatus() != SubmissionStatus.REVISION_REQUIRED) {
            throw new SubmissionRequestException("Cannot skip review for a submission with status: " + submission.getSubmissionStatus());
        }*/

//        submission.setSubmissionStatus(SubmissionStatus.ACCEPTED); // First set to accepted
        submission.setSubmissionStatus(SubmissionStatus.COPY_EDITING); // Then immediately set to copy editing

        submission.setUpdatedAt(BdtZoneTimeUtil.timeInBDT());
//        submission.setEditable(false); // Lock submission for editing after this stage

        // Unmark any review files and copy-editing files if they were marked before skipping review
        if (submission.getFiles() != null) {
            for (FileUpload file : submission.getFiles()) {
                file.setReviewFile(false);
//                file.setCopyEditingFile(false); // <--- ADDED THIS LINE FOR CONSISTENCY
            }
        }

        return submissionRepository.save(submission);
    }

    // --- NEW METHOD: SELECT FILE FOR COPY EDITING ---
    @Transactional
    public Submission selectFileForCopyEditing(Long submissionId, Long fileIdForCopyEditing) {
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new SubmissionRequestException("Submission not found with ID: " + submissionId));

        // Business logic: Ensure the submission is in a copy-editing phase or equivalent
        /*if (submission.getSubmissionStatus() != SubmissionStatus.COPY_EDITING) {
            throw new SubmissionRequestException("Cannot select file for copy-editing as the submission is not in 'COPY_EDITING' status. Current status: " + submission.getSubmissionStatus());
        }*/

        boolean fileFoundAndMarked = false;
        if (submission.getFiles() != null) {
            for (FileUpload file : submission.getFiles()) {
                if (file.getId().equals(fileIdForCopyEditing)) {
                    file.setCopyEditingFile(true); // Mark this file for copy-editing
                    fileFoundAndMarked = true;
                } else {
                    file.setCopyEditingFile(false); // Unmark any other files
                }
                // Ensure review file flag is false for all files here, as we are in copy-editing phase
//                file.setReviewFile(false);
            }
        }

        if (!fileFoundAndMarked) {
            throw new SubmissionRequestException("File with ID: " + fileIdForCopyEditing + " not found within submission ID: " + submissionId);
        }

        submission.setUpdatedAt(BdtZoneTimeUtil.timeInBDT());
        return submissionRepository.save(submission);
    }

    // --- NEW METHOD: Upload Revision File ---
    @Transactional
    public FileUpload uploadRevisionFile(Long submissionId, MultipartFile file) throws IOException {
        Submission existingSubmission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new SubmissionRequestException("Submission not found with ID: " + submissionId));

        // You might want to add logic here to unmark previous revisions as 'latest' if you implement that concept.
        // For now, it simply adds the new revision file.

        return saveFile(submissionId, FileUploadOrigin.REVISION.name(), file);
    }

    // --- NEW METHOD: Upload Copyedited File ---
    @Transactional
    public FileUpload uploadCopyeditedFile(Long submissionId, MultipartFile file) throws IOException {
        Submission existingSubmission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new SubmissionRequestException("Submission not found with ID: " + submissionId));

        // Call the general saveFile method, specifying the origin as COPY_EDIT
        return saveFile(submissionId, FileUploadOrigin.COPY_EDIT.name(), file);
    }

    // Add this to SubmissionService.java
    @Transactional
    public Submission selectFileForProduction(Long submissionId, Long fileIdForProduction) {
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new SubmissionRequestException("Submission not found with ID: " + submissionId));

        // Business logic: Ensure the submission is in a production phase or equivalent
        /*if (submission.getSubmissionStatus() != SubmissionStatus.COPY_EDITING) {
            throw new SubmissionRequestException("Cannot select file for production as the submission is not in 'COPY_EDITING' status. Current status: " + submission.getSubmissionStatus());
        }*/

        boolean fileFoundAndMarked = false;
        if (submission.getFiles() != null) {
            for (FileUpload file : submission.getFiles()) {
                if (file.getId().equals(fileIdForProduction)) {
                    file.setProductionFile(true); // Mark this file for production
                    fileFoundAndMarked = true;
                } else {
                    file.setProductionFile(false); // Unmark any other files
                }
                // Ensure copy-editing file flag is false for all files here, as we are moving to production
//                file.setCopyEditingFile(false);
            }
        }

        if (!fileFoundAndMarked) {
            throw new SubmissionRequestException("File with ID: " + fileIdForProduction + " not found within submission ID: " + submissionId);
        }

        // Update status to PRODUCTION
        submission.setSubmissionStatus(SubmissionStatus.PRODUCTION);
        submission.setUpdatedAt(BdtZoneTimeUtil.timeInBDT());

        return submissionRepository.save(submission);
    }

    // Add this to SubmissionService.java
    @Transactional
    public FileUpload uploadProductionFile(Long submissionId, MultipartFile file) throws IOException {
        Submission existingSubmission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new SubmissionRequestException("Submission not found with ID: " + submissionId));

        // Business logic: Ensure the submission is in production phase
//        if (existingSubmission.getSubmissionStatus() != SubmissionStatus.PRODUCTION) {
//            throw new SubmissionRequestException("Cannot upload production file as the submission is not in 'PRODUCTION' status");
//        }

        return saveFile(submissionId, FileUploadOrigin.PRODUCTION.name(), file);
    }

    // Add this to SubmissionService.java
    @Transactional
    public Submission selectFileForPublication(Long submissionId, Long fileIdForPublication) {
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new SubmissionRequestException("Submission not found with ID: " + submissionId));

        // Business logic: Ensure the submission is in production phase
//        if (submission.getSubmissionStatus() != SubmissionStatus.PRODUCTION) {
//            throw new SubmissionRequestException("Cannot select file for publication as the submission is not in 'PRODUCTION' status");
//        }

        boolean fileFoundAndMarked = false;
        if (submission.getFiles() != null) {
            for (FileUpload file : submission.getFiles()) {
                if (file.getId().equals(fileIdForPublication)) {
                    file.setPublicationFile(true); // Mark this file for publication
                    fileFoundAndMarked = true;
                } else {
                    file.setPublicationFile(false); // Unmark any other files
                }
            }
        }

        if (!fileFoundAndMarked) {
            throw new SubmissionRequestException("File with ID: " + fileIdForPublication + " not found within submission ID: " + submissionId);
        }

        // Update status to PUBLISHED
        submission.setSubmissionStatus(SubmissionStatus.PUBLICATION);
        submission.setUpdatedAt(BdtZoneTimeUtil.timeInBDT());
        submission.setEditable(false);

        return submissionRepository.save(submission);
    }

    // NEW METHOD: Get All Submissions
    @Transactional(readOnly = true)
    public List<Submission> getAllSubmissions() {
        return submissionRepository.findAll();
    }

}