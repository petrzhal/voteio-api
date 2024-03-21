package ag.repository;

import ag.exceptions.UserAlreadyExistsException;
import ag.models.Role;
import ag.models.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
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
        List<User> users = jdbcTemplate.query("SELECT * FROM account WHERE login=?", new Object[]{login}, (resultSet, rowNum) -> {
            User user = new User();
            user.setId(resultSet.getInt("id"));
            user.setLogin(resultSet.getString("login"));
            user.setPassword(resultSet.getString("password"));
            String roleString = resultSet.getString("role");
            Role role = Role.valueOf(roleString.toUpperCase());
            user.setRole(role);
            return user;
        });

        if (users.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(users.get(0));
    }
}
