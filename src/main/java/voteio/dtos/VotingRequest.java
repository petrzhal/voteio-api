package voteio.dtos;

import lombok.Data;
import lombok.Getter;

import java.time.LocalDateTime;

@Data
@Getter
public class VotingRequest {
    private String title;
    private String description;
    private String category;
    private String type;
    private LocalDateTime begin_date;
    private LocalDateTime end_date;
}
