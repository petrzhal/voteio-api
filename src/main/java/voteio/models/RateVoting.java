package voteio.models;

import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@Getter
@Setter
@Builder
public class RateVoting {
    private Integer id;
    private String title;
    private String description;
    private String category;
    private VotingType type;
    private Integer creator_id;
    private Integer votes_num;
    private LocalDateTime begin_date;
    private LocalDateTime end_date;
}
