package ag.controllers;

import ag.dtos.*;
import ag.exceptions.ApplicationError;
import ag.exceptions.UserAlreadyVotedException;
import ag.models.*;
import ag.service.UserService;
import ag.service.VotingService;
import ag.token.JwtUtil;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;
import java.util.logging.Logger;


@RestController
@RequiredArgsConstructor
@RequestMapping
public class VotingController {
    private final JwtUtil jwtUtil;
    private final VotingService votingService;
    private final UserService userService;
    private static final Logger logger = Logger.getLogger(VotingController.class.getName());

    @PostMapping("api/voting/add")
    public ResponseEntity<?> addVoting(
            @RequestHeader("Authorization") String accessToken,
            @RequestBody VotingRequest votingRequest
    ) {
        Integer id;
        try {
            jwtUtil.checkExpiration(accessToken);
            var login = jwtUtil.getLogin(accessToken);
            id = votingService.addVoting(
                    Voting.builder()
                            .title(votingRequest.getTitle())
                            .description(votingRequest.getDescription())
                            .category(votingRequest.getCategory())
                            .type(VotingType.valueOf(votingRequest.getType().toUpperCase()))
                            .creator_id(
                                    userService.findByLogin(login).orElseThrow(() -> new UsernameNotFoundException(
                                                    String.format("User '%s' not found", login)
                                            )
                                    ).getId())
                            .build()
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
            logger.severe(e.getMessage());
            return new ResponseEntity<>(
                    new ApplicationError(
                            HttpStatus.UNAUTHORIZED.value(),
                            "invalid token"
                    ),
                    HttpStatus.UNAUTHORIZED
            );
        } catch (Throwable e) {
            logger.severe(e.getMessage());
            return new ResponseEntity<>(
                    new ApplicationError(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "failed to add voting"
                    ),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return ResponseEntity.ok(new VotingResponse(id));
    }

    @PostMapping("api/voting/{voting_id}/addPosition")
    public ResponseEntity<?> addPosition(
            @RequestHeader("Authorization") String accessToken,
            @RequestBody PositionRequest positionRequest,
            @PathVariable Integer voting_id
    ) {
        Integer id;
        try {
            jwtUtil.checkExpiration(accessToken);
            id = votingService.addPosition(
                    new Position(
                            null,
                            positionRequest.getDescription(),
                            voting_id
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
            logger.severe(e.getMessage());
            return new ResponseEntity<>(
                    new ApplicationError(
                            HttpStatus.UNAUTHORIZED.value(),
                            "invalid token"
                    ),
                    HttpStatus.UNAUTHORIZED
            );
        } catch (Throwable e) {
            logger.severe(e.getMessage());
            return new ResponseEntity<>(
                    new ApplicationError(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "failed to add position"
                    ),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
        return ResponseEntity.ok(new PositionResponse(id));
    }

    @PostMapping("api/voting/{voting_id}/position/{position_id}/vote")
    public ResponseEntity<?> vote(
            @RequestHeader("Authorization") String accessToken,
            @PathVariable Integer position_id,
            @PathVariable Integer voting_id
    ) {
        Integer id;
        try {
            jwtUtil.checkExpiration(accessToken);
            var login = jwtUtil.getClaimsFromToken(accessToken).getSubject();
            var user_id = userService.findByLogin(login).orElseThrow(() -> new UsernameNotFoundException(
                    String.format("User '%s' not found", login)
            )).getId();
            id = votingService.addVote(new Vote(null, position_id, user_id), voting_id);
        } catch (TokenExpiredException e) {
            return new ResponseEntity<>(
                    new ApplicationError(
                            HttpStatus.UNAUTHORIZED.value(),
                            "token expired"
                    ),
                    HttpStatus.UNAUTHORIZED
            );
        } catch (JWTVerificationException e) {
            logger.severe(e.getMessage());
            return new ResponseEntity<>(
                    new ApplicationError(
                            HttpStatus.UNAUTHORIZED.value(),
                            "invalid token"
                    ),
                    HttpStatus.UNAUTHORIZED
            );
        } catch (UserAlreadyVotedException e) {
            logger.severe(e.getMessage());
            return new ResponseEntity<>(
                    new ApplicationError(
                            HttpStatus.FORBIDDEN.value(),
                            "user already voted"
                    ),
                    HttpStatus.FORBIDDEN
            );
        } catch (Throwable e) {
            logger.severe(e.getMessage());
            return new ResponseEntity<>(
                    new ApplicationError(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "failed to add vote"
                    ),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
        return ResponseEntity.ok(new VoteResponse(id));
    }

    @DeleteMapping("api/voting/{voting_id}/delete")
    public ResponseEntity<?> deleteVoting(
            @RequestHeader("Authorization") String accessToken,
            @PathVariable Integer voting_id
    ) {
        try {
            jwtUtil.checkExpiration(accessToken);
            var login = jwtUtil.getLogin(accessToken);
            if (userService.getRole(login) != Role.ADMIN || !Objects.equals(votingService.getCreator(voting_id), login)) {
                return new ResponseEntity<>(
                        new ApplicationError(
                                HttpStatus.FORBIDDEN.value(),
                                "not enough privileges"
                        ),
                        HttpStatus.FORBIDDEN
                );
            }
            votingService.deleteVoting(voting_id);
        } catch (TokenExpiredException e) {
            return new ResponseEntity<>(
                    new ApplicationError(
                            HttpStatus.UNAUTHORIZED.value(),
                            "token expired"
                    ),
                    HttpStatus.UNAUTHORIZED
            );
        } catch (JWTVerificationException e) {
            logger.severe(e.getMessage());
            return new ResponseEntity<>(
                    new ApplicationError(
                            HttpStatus.UNAUTHORIZED.value(),
                            "invalid token"
                    ),
                    HttpStatus.UNAUTHORIZED
            );
        } catch (Throwable e) {
            logger.severe(e.getMessage());
            return new ResponseEntity<>(
                    new ApplicationError(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "failed to delete voting"
                    ),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
        return ResponseEntity.ok("voting deleted");
    }

    @GetMapping("api/voting/category/{category}")
    public ResponseEntity<?> getVotingFromCategory(
            @RequestHeader("Authorization") String accessToken,
            @PathVariable String category
    ) {
        try {
            jwtUtil.checkExpiration(accessToken);
            return ResponseEntity.ok(votingService.getVotingFromCategory(category));
        } catch (TokenExpiredException e) {
            return new ResponseEntity<>(
                    new ApplicationError(
                            HttpStatus.UNAUTHORIZED.value(),
                            "token expired"
                    ),
                    HttpStatus.UNAUTHORIZED
            );
        } catch (JWTVerificationException e) {
            logger.severe(e.getMessage());
            return new ResponseEntity<>(
                    new ApplicationError(
                            HttpStatus.UNAUTHORIZED.value(),
                            "invalid token"
                    ),
                    HttpStatus.UNAUTHORIZED
            );
        }
    }

    @GetMapping("api/voting/by/{user_id}")
    public ResponseEntity<?> getCreatedVoting(
            @RequestHeader("Authorization") String accessToken,
            @PathVariable Integer user_id
    ) {
        try {
            jwtUtil.checkExpiration(accessToken);
            return ResponseEntity.ok(votingService.getCreatedVoting(user_id));
        } catch (TokenExpiredException e) {
            return new ResponseEntity<>(
                    new ApplicationError(
                            HttpStatus.UNAUTHORIZED.value(),
                            "token expired"
                    ),
                    HttpStatus.UNAUTHORIZED
            );
        } catch (JWTVerificationException e) {
            logger.severe(e.getMessage());
            return new ResponseEntity<>(
                    new ApplicationError(
                            HttpStatus.UNAUTHORIZED.value(),
                            "invalid token"
                    ),
                    HttpStatus.UNAUTHORIZED
            );
        }
    }

    @GetMapping("api/voting/participated/{user_id}")
    public ResponseEntity<?> getTakenPartVoting(
            @RequestHeader("Authorization") String accessToken,
            @PathVariable Integer user_id
    ) {
        try {
            jwtUtil.checkExpiration(accessToken);
            return ResponseEntity.ok(votingService.getTakenPartVoting(user_id));
        } catch (TokenExpiredException e) {
            return new ResponseEntity<>(
                    new ApplicationError(
                            HttpStatus.UNAUTHORIZED.value(),
                            "token expired"
                    ),
                    HttpStatus.UNAUTHORIZED
            );
        } catch (JWTVerificationException e) {
            logger.severe(e.getMessage());
            return new ResponseEntity<>(
                    new ApplicationError(
                            HttpStatus.UNAUTHORIZED.value(),
                            "invalid token"
                    ),
                    HttpStatus.UNAUTHORIZED
            );
        }
    }

    @GetMapping("api/voting/rating")
    public ResponseEntity<?> getVotingRating() {
        return ResponseEntity.ok(votingService.getVotingRating());
    }

    @GetMapping("api/voting/{voting_id}/getPositions")
    public ResponseEntity<?> getPositionsForVoting(@PathVariable Integer voting_id) {
        return ResponseEntity.ok(votingService.getPositionsForVoting(voting_id));
    }

    @GetMapping("api/users/rating")
    public ResponseEntity<?> getUserRating() {
        return ResponseEntity.ok(userService.getUserRating());
    }
}
