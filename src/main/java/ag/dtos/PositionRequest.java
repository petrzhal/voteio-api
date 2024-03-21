package ag.dtos;

import lombok.Data;
import lombok.Getter;

@Data
@Getter
public class PositionRequest {
    private String description;
    private Integer voting_id;
}
