package com.pubgclan.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "applications")
public class Application {

    @Id
    private String id;

    private String nickname;

    private String discordId;

    private String tier;

    private String playTime;

    private String intro;

    @Builder.Default
    private String status = "pending";

    private LocalDateTime createdAt;
}
