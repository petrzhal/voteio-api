package voteio.models;

import lombok.*;

@AllArgsConstructor
@Getter
@Setter
@Builder
public class RateUser {
    private Integer id;
    private String login;
    private Integer votes_num;
}
