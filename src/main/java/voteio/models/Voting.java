package voteio.models;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Voting {
    private Integer id;
    private String title;
    private String description;
    private String category;
    private VotingType type;
    private Integer creator_id;
    private LocalDateTime begin_date;
    private LocalDateTime end_date;
}
