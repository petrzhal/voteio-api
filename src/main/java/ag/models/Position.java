package ag.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

@Getter
@Data
@AllArgsConstructor
public class Position {
    private Integer id;
    private String description;
    private Integer votes_number;
    private Integer voting_id;
}
