package voteio.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class JwtResponse {
    private String access_token;
    private String refresh_token;
    private Integer user_id;
}
