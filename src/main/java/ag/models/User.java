package ag.models;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonTypeId;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.*;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Getter
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {
    private Integer id;
    private String login;
    private String password;
    private Role role = Role.USER;
}


