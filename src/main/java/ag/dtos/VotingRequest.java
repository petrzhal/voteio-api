package ag.dtos;

import lombok.Data;
import lombok.Getter;

@Data
@Getter
public class VotingRequest {
    private String title;
    private String description;
}
