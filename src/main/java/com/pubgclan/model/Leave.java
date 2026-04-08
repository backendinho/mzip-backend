package com.pubgclan.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "leaves")
public class Leave {

    @Id
    private String id;

    @Indexed
    private String userId;

    private String username;

    private String reason;

    private LocalDate startDate;

    private LocalDate endDate;

    @Builder.Default
    private String status = "PENDING"; // PENDING, APPROVED, REJECTED

    private LocalDateTime createdAt;

    private LocalDateTime approvedAt;

    private String approvedBy; // Admin userId
}
