package com.himusharier.ajps_backend.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.himusharier.ajps_backend.constants.FileUploadOrigin;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "AJPS_FILES")
public class FileUpload {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FileUploadOrigin fileOrigin;

    @Column(nullable = false)
    private String storedName;

    @Column(nullable = false)
    private String originalName;

    @Column(nullable = false)
    private long size;

    @Column(nullable = false)
    private String type;

    @Column(nullable = false)
    private String fileUrl;

    @ManyToOne
    @JoinColumn(name = "submission_id", nullable = false)
    @JsonBackReference
    private Submission submission;
}
