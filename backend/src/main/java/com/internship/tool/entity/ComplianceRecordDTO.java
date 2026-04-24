package com.internship.tool.entity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComplianceRecordDTO {

    private Long id;

    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;

    private String description;

    @Size(max = 50, message = "Section number must not exceed 50 characters")
    private String sectionNumber;

    @NotBlank(message = "Compliance type is required")
    @Size(max = 100, message = "Compliance type must not exceed 100 characters")
    private String complianceType;

    @NotNull(message = "Status is required")
    private ComplianceRecord.ComplianceStatus status;

    @NotNull(message = "Priority is required")
    private ComplianceRecord.Priority priority;

    private LocalDate dueDate;

    @Size(max = 100, message = "Assigned to must not exceed 100 characters")
    private String assignedTo;

    @NotBlank(message = "Company name is required")
    @Size(max = 200, message = "Company name must not exceed 200 characters")
    private String companyName;

    private String filingFrequency;
    private Double penaltyAmount;
    private String remarks;
    private String aiDescription;
    private String aiRecommendations;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Convert DTO to Entity
    public ComplianceRecord toEntity() {
        return ComplianceRecord.builder()
                .title(this.title)
                .description(this.description)
                .sectionNumber(this.sectionNumber)
                .complianceType(this.complianceType)
                .status(this.status)
                .priority(this.priority)
                .dueDate(this.dueDate)
                .assignedTo(this.assignedTo)
                .companyName(this.companyName)
                .filingFrequency(this.filingFrequency)
                .penaltyAmount(this.penaltyAmount)
                .remarks(this.remarks)
                .build();
    }

    // Convert Entity to DTO
    public static ComplianceRecordDTO fromEntity(ComplianceRecord record) {
        return ComplianceRecordDTO.builder()
                .id(record.getId())
                .title(record.getTitle())
                .description(record.getDescription())
                .sectionNumber(record.getSectionNumber())
                .complianceType(record.getComplianceType())
                .status(record.getStatus())
                .priority(record.getPriority())
                .dueDate(record.getDueDate())
                .assignedTo(record.getAssignedTo())
                .companyName(record.getCompanyName())
                .filingFrequency(record.getFilingFrequency())
                .penaltyAmount(record.getPenaltyAmount())
                .remarks(record.getRemarks())
                .aiDescription(record.getAiDescription())
                .aiRecommendations(record.getAiRecommendations())
                .createdAt(record.getCreatedAt())
                .updatedAt(record.getUpdatedAt())
                .build();
    }
}