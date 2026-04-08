package com.pubgclan.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "exemptions")
public class Exemption {

    @Id
    private String id;

    @Indexed
    private String userId;

    private String username;

    private int year;

    private int week;

    private String reason; // Leave ID 또는 이유

    private LocalDateTime createdAt;

    private String createdBy; // Admin userId
}
