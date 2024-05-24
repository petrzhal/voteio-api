package voteio.models;

import lombok.*;

@Getter
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User {
    private Integer id;
    private String login;
    private String password;
    private Role role = Role.USER;
}


