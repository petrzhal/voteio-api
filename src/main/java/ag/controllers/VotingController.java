package ag.controllers;

import ag.dtos.*;
import ag.exceptions.ApplicationError;
import ag.models.Position;
import ag.models.Vote;
import ag.models.Voting;
import ag.models.VotingType;
import ag.service.UserService;
import ag.service.VotingService;
import ag.token.JwtUtil;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.logging.Logger;


@RestController
@RequiredArgsConstructor
@RequestMapping
public class VotingController {
    private final JwtUtil jwtUtil;
    private final VotingService votingService;
    private final UserService userService;
    private static final Logger logger = Logger.getLogger(VotingController.class.getName());

    @PostMapping("api/addVoting")
    public ResponseEntity<?> addVoting(
            @RequestHeader("Authorization") String accessToken,
            @RequestBody VotingRequest votingRequest
    ) {
        Integer id;
        try {
            jwtUtil.checkExpiration(accessToken);
            id = votingService.addVoting(
                    Voting.builder()
                            .title(votingRequest.getTitle())
                            .description(votingRequest.getDescription())
                            .category(votingRequest.getCategory())
                            .type(VotingType.valueOf(votingRequest.getType().toUpperCase()))
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

    @PostMapping("api/addPosition")
    public ResponseEntity<?> addPosition(
            @RequestHeader("Authorization") String accessToken,
            @RequestBody PositionRequest positionRequest
    ) {
        Integer id;
        try {
            jwtUtil.checkExpiration(accessToken);
            id = votingService.addPosition(
                    new Position(
                            null,
                            positionRequest.getDescription(),
                            positionRequest.getVoting_id()
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

    @PostMapping("api/vote")
    public ResponseEntity<?> vote(@RequestHeader("Authorization") String accessToken, @RequestBody VoteRequest voteRequest) {
        Integer id;
        try {
            jwtUtil.checkExpiration(accessToken);
            var login = jwtUtil.getClaimsFromToken(accessToken).getSubject();
            var user_id = userService.findByLogin(login).orElseThrow(() -> new UsernameNotFoundException(
                    String.format("User '%s' not found", login)
            )).getId();
            id = votingService.addVote(new Vote(null, voteRequest.getPosition_id(), user_id));
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
                            "failed to add vote"
                    ),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
        return ResponseEntity.ok(new VoteResponse(id));
    }
}
