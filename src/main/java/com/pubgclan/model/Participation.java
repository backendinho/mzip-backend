package com.pubgclan.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "participations")
@CompoundIndexes({
    @CompoundIndex(name = "user_year_week_idx", def = "{'userId': 1, 'year': 1, 'week': 1}"),
    @CompoundIndex(name = "message_id_idx", def = "{'messageId': 1}")
})
public class Participation {

    @Id
    private String id;

    private String userId;

    private String username;

    private String messageId;

    private String date;  // "2026-04-08" 형식 (String으로 저장, 시간대 이슈 없음)

    private int week;

    private int year;
}
