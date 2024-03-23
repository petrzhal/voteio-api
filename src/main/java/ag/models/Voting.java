package ag.models;

import lombok.*;

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
}
