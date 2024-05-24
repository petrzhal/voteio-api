package voteio.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Comment {
    private Integer id;
    private String text;
    private String creator;
    private Integer voting_id;
    private Date publication_date;
}
