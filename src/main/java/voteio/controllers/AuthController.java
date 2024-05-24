package voteio.controllers;

import voteio.dtos.JwtRequest;
import voteio.dtos.JwtResponse;
import voteio.exceptions.ApplicationError;
import voteio.exceptions.UserAlreadyExistsException;
import voteio.models.Role;
import voteio.models.User;
import voteio.service.UserService;
import voteio.token.JwtUtil;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping
public class AuthController {
    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    @CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
    @PostMapping("api/register")
    public ResponseEntity<?> Register(@RequestBody JwtRequest registerRequest) {
        try {
            userService.Register(
                    User.builder()
                            .login(registerRequest.getLogin())
                            .password(registerRequest.getPassword())
                            .role(Role.USER)
                            .build()
            );
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            registerRequest.getLogin(),
                            registerRequest.getPassword()
                    )
            );
        } catch (UserAlreadyExistsException e) {
            return new ResponseEntity<>(
                    new ApplicationError(
                            HttpStatus.UNAUTHORIZED.value(),
                            "user already exists"
                    ),
                    HttpStatus.UNAUTHORIZED
            );
        } catch (AuthenticationException e) {
            return new ResponseEntity<>(
                    new ApplicationError(
                            HttpStatus.UNAUTHORIZED.value(),
                            "failed to authenticate"
                    ),
                    HttpStatus.UNAUTHORIZED
            );
        }
        UserDetails userDetails = userService.loadUserByUsername((registerRequest.getLogin()));
        var user_id = userService.findByLogin(userDetails.getUsername()).get().getId();
        String access_token = jwtUtil.generateAccessToken(userDetails);
        String refresh_token = jwtUtil.generateRefreshToken(userDetails);
        jwtUtil.saveToken(refresh_token, user_id);
        return ResponseEntity.ok(new JwtResponse(access_token, refresh_token, user_id));
    }

    @CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
    @PostMapping("api/login")
    public ResponseEntity<?> Login(@RequestBody JwtRequest authRequest) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            authRequest.getLogin(),
                            authRequest.getPassword()
                    )
            );
        } catch (BadCredentialsException e) {
            return new ResponseEntity<>(
                    new ApplicationError(
                            HttpStatus.UNAUTHORIZED.value(),
                            "incorrect login or password"
                    ),
                    HttpStatus.UNAUTHORIZED
            );
        }
        UserDetails userDetails = userService.loadUserByUsername((authRequest.getLogin()));
        var user_id = userService.findByLogin(authRequest.getLogin()).get().getId();
        String access_token = jwtUtil.generateAccessToken(userDetails);
        String refresh_token = jwtUtil.generateRefreshToken(userDetails);
        jwtUtil.saveToken(refresh_token, user_id);
        return ResponseEntity.ok(new JwtResponse(access_token, refresh_token, user_id));
    }

    @CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
    @PostMapping("api/refresh")
    public ResponseEntity<?> refresh(@RequestHeader("Authorization") String refreshToken) {
        try {
            jwtUtil.checkExpiration(refreshToken);
            var login = jwtUtil.getLogin(refreshToken);
            return ResponseEntity.ok(
                    new JwtResponse(
                            jwtUtil.generateAccessToken(userService.loadUserByUsername(login)),
                            refreshToken,
                            userService.findByLogin(login).get().getId()
                    )
            );
        } catch (TokenExpiredException e) {
            return new ResponseEntity<>(
                    new ApplicationError(
                            HttpStatus.UNAUTHORIZED.value(),
                            "token expired"
                    ),
                    HttpStatus.UNAUTHORIZED
            );
        } catch (JWTVerificationException e) {
            System.out.println(e.getMessage());
            return new ResponseEntity<>(
                    new ApplicationError(
                            HttpStatus.UNAUTHORIZED.value(),
                            "invalid token"
                    ),
                    HttpStatus.UNAUTHORIZED
            );
        }
    }
    @CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
    @GetMapping("api/user/get")
    public ResponseEntity<?> getUser(@RequestHeader("Authorization") String accessToken) {
        try {
            jwtUtil.checkExpiration(accessToken);
            var login = jwtUtil.getLogin(accessToken);
            var user = userService.findByLogin(login).get();
            user.setPassword(null);
            return ResponseEntity.ok(user);
        } catch (TokenExpiredException e) {
            return new ResponseEntity<>(
                    new ApplicationError(
                            HttpStatus.UNAUTHORIZED.value(),
                            "token expired"
                    ),
                    HttpStatus.UNAUTHORIZED
            );
        } catch (JWTVerificationException e) {
            System.out.println(e.getMessage());
            return new ResponseEntity<>(
                    new ApplicationError(
                            HttpStatus.UNAUTHORIZED.value(),
                            "invalid token"
                    ),
                    HttpStatus.UNAUTHORIZED
            );
        }
    }
}
