package ag.repository;

import ag.exceptions.UserAlreadyExistsException;
import ag.models.Role;
import ag.models.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserRepository {
    private final JdbcTemplate jdbcTemplate;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Transactional
    public void addUser(User user) throws UserAlreadyExistsException {
        try {
            var rows = jdbcTemplate.update(
                    "INSERT INTO account (login, password, role) VALUES (?, ?, 'user')",
                    user.getLogin(),
                    passwordEncoder.encode(user.getPassword())
            );
            if (rows == 0) {
                throw new UserAlreadyExistsException();
            }
        } catch (org.springframework.dao.DuplicateKeyException e) {
            System.out.println(e.getMessage());
            throw new UserAlreadyExistsException();
        }
    }

    @Transactional
    public Optional<User> findByLogin(String login) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(
                    "SELECT * FROM account WHERE login=?",
                    new Object[]{login},
                    (resultSet, rowNum) ->
                            User.builder()
                                    .id(resultSet.getInt("id"))
                                    .login(resultSet.getString("login"))
                                    .password(resultSet.getString("password"))
                                    .role(Role.valueOf(resultSet.getString("role").toUpperCase()))
                                    .build()
                    )
            );
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Transactional
    public Role getRole(String login) {
        var role = jdbcTemplate.queryForObject("SELECT role FROM account WHERE login=?", String.class, login);
        assert role != null;
        return Role.valueOf(role.toUpperCase());
    }
}
