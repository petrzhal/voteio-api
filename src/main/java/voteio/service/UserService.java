package voteio.service;

import voteio.exceptions.UserAlreadyExistsException;
import voteio.models.RateUser;
import voteio.models.Role;
import voteio.models.User;
import voteio.repository.UserRepository;
import voteio.token.JwtUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    public Optional<User> findByLogin(String login) {
        return userRepository.findByLogin(login);
    }

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        User user = findByLogin(login).orElseThrow(() -> new UsernameNotFoundException(
                String.format("User '%s' not found", login)
        ));
        return new org.springframework.security.core.userdetails.User(
                user.getLogin(),
                user.getPassword(),
                List.of(new SimpleGrantedAuthority(user.getRole().toString()))
        );
    }

    public void Register(User user) throws UserAlreadyExistsException {
        try {
            userRepository.addUser(user);
        } catch (UserAlreadyExistsException e) {
            throw new UserAlreadyExistsException();
        }
        jwtUtil.generateAccessToken(
                new org.springframework.security.core.userdetails.User(
                        user.getLogin(),
                        user.getPassword(),
                        List.of(new SimpleGrantedAuthority(user.getRole().toString()))
                )
        );
    }
    public Role getRole(String login) {
        return userRepository.getRole(login);
    }

    public List<RateUser> getUserRating() {
        return userRepository.getUserRating();
    }

    public User getUserById(Integer id) {
        return userRepository.getUserById(id);
    }
}
