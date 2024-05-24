package voteio.controllers;

import voteio.dtos.*;
import voteio.exceptions.ApplicationError;
import voteio.exceptions.UserAlreadyVotedException;
import voteio.models.*;
import voteio.service.UserService;
import voteio.service.VotingService;
import voteio.token.JwtUtil;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
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

    @CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
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
                            .begin_date(votingRequest.getBegin_date())
                            .end_date(votingRequest.getEnd_date())
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

    @CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
    @PostMapping("api/voting/{voting_id}/addPosition")
    public ResponseEntity<?> addPosition(
            @RequestHeader("Authorization") String accessToken,
            @RequestBody PositionRequest positionRequest,
            @PathVariable Integer voting_id
    ) {
        Integer id;
        try {
            jwtUtil.checkExpiration(accessToken);
            if (!votingService.isCreator(voting_id, userService.findByLogin(jwtUtil.getLogin(accessToken)).get().getId())) {
                return new ResponseEntity<>(
                        new ApplicationError(
                                HttpStatus.FORBIDDEN.value(),
                                "not creator"
                        ),
                        HttpStatus.FORBIDDEN
                );
            }
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

    @CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
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

    @CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
    @DeleteMapping("api/voting/{voting_id}/delete")
    public ResponseEntity<?> deleteVoting(
            @RequestHeader("Authorization") String accessToken,
            @PathVariable Integer voting_id
    ) {
        try {
            jwtUtil.checkExpiration(accessToken);
            var login = jwtUtil.getLogin(accessToken);
            if ((userService.getRole(login) != Role.ADMIN) && (!Objects.equals(votingService.getCreator(voting_id), login))) {
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

    @CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
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

    @CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
    @GetMapping("api/voting/created")
    public ResponseEntity<?> getCreatedVoting(@RequestHeader("Authorization") String accessToken) {
        try {
            jwtUtil.checkExpiration(accessToken);
            var user = userService.findByLogin(jwtUtil.getLogin(accessToken)).get();
            if (user.getRole() == Role.ADMIN) {
                return ResponseEntity.ok(votingService.getVotingRating());
            }
            return ResponseEntity.ok(votingService.getCreatedVoting(user.getId()));
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

    @CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
    @GetMapping("api/voting/participated")
    public ResponseEntity<?> getTakenPartVoting(
            @RequestHeader("Authorization") String accessToken
    ) {
        try {
            var user = userService.findByLogin(jwtUtil.getLogin(accessToken));
            jwtUtil.checkExpiration(accessToken);
            return ResponseEntity.ok(votingService.getTakenPartVoting(user.get().getId()));
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

    @CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
    @GetMapping("api/voting/rating")
    public ResponseEntity<?> getVotingRating() {
        return ResponseEntity.ok(votingService.getVotingRating());
    }

    @CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
    @GetMapping("api/voting/{voting_id}/getPositions")
    public ResponseEntity<?> getPositionsForVoting(@PathVariable Integer voting_id) {
        return ResponseEntity.ok(votingService.getPositionsForVoting(voting_id));
    }

    @CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
    @GetMapping("api/users/rating")
    public ResponseEntity<?> getUserRating() {
        return ResponseEntity.ok(userService.getUserRating());
    }

    @CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
    @GetMapping("api/voting/{voting_id}/info")
    public ResponseEntity<?> getVoting(@PathVariable Integer voting_id) {
        return ResponseEntity.ok(votingService.getVoting(voting_id));
    }

    @CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
    @DeleteMapping("api/voting/{voting_id}/position/{position_id}/remove")
    public ResponseEntity<?> removePosition(
            @RequestHeader("Authorization") String accessToken,
            @PathVariable Integer voting_id,
            @PathVariable Integer position_id
    ) {
        try  {
            if (!votingService.isCreator(voting_id, userService.findByLogin(jwtUtil.getLogin(accessToken)).get().getId())) {
                return new ResponseEntity<>(
                        new ApplicationError(
                                HttpStatus.FORBIDDEN.value(),
                                "not creator"
                        ),
                        HttpStatus.FORBIDDEN
                );
            }
            votingService.removePosition(voting_id, position_id);
        } catch (Throwable e) {
            return new ResponseEntity<>(
                    new ApplicationError(
                            HttpStatus.FORBIDDEN.value(),
                            "invalid token"
                    ),
                    HttpStatus.FORBIDDEN
            );
        }
        return ResponseEntity.ok("removed");
    }

    @CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
    @GetMapping("api/voting/{voting_id}/isVoted")
    public ResponseEntity<?> getVoted(
            @RequestHeader("Authorization") String accessToken,
            @PathVariable Integer voting_id
    ) {
        try {
            jwtUtil.checkExpiration(accessToken);
            var user = userService.findByLogin(jwtUtil.getLogin(accessToken));
            return ResponseEntity.ok(votingService.getVotedPosition(voting_id, user.get().getId()));
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
                            "failed to check voted position"
                    ),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    @CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
    @GetMapping("api/position/{position_id}/votes")
    public ResponseEntity<?> votesForPosition(@PathVariable Integer position_id) {
        try {
            return ResponseEntity.ok(votingService.votesForPosition(position_id));
        } catch (Throwable e) {
            logger.severe(e.getMessage());
            return new ResponseEntity<>(
                    new ApplicationError(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "failed to load votes for position"
                    ),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    @CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
    @DeleteMapping("api/position/{position_id}/retract")
    public ResponseEntity<?> retractVote(
            @RequestHeader("Authorization") String accessToken,
            @PathVariable Integer position_id
    ) {
        try {
            jwtUtil.checkExpiration(accessToken);
            var user = userService.findByLogin(jwtUtil.getLogin(accessToken));
            return ResponseEntity.ok(votingService.retractVote(position_id, user.get().getId()));
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
                            "failed to retract vote"
                    ),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }
    @CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
    @GetMapping("api/position/{position_id}/users")
    public ResponseEntity<?> getUsersForPosition(
            @RequestHeader("Authorization") String accessToken,
            @PathVariable Integer position_id
    ) {
        try {
            jwtUtil.checkExpiration(accessToken);
            return ResponseEntity.ok(votingService.getUsersForPosition(position_id));
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
                            "failed to get participants"
                    ),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    @CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
    @GetMapping("api/voting/search/title/{title}")
    public ResponseEntity<?> searchByTitle (
            @RequestHeader("Authorization") String accessToken,
            @PathVariable String title
    ) {
        try {
            jwtUtil.checkExpiration(accessToken);
            return ResponseEntity.ok(votingService.searchByTitle(title));
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
                            "failed to find by title"
                    ),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    @CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
    @GetMapping("api/voting/search/title/")
    public ResponseEntity<?> searchByTitleNull (
            @RequestHeader("Authorization") String accessToken
    ) {
        return getVotingRating();
    }

    @CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
    @GetMapping("api/voting/category/")
    public ResponseEntity<?> getVotingFromCategoryNull (
            @RequestHeader("Authorization") String accessToken
    ) {
        return getVotingRating();
    }
}
