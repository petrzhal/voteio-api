package ag.controllers;

import ag.dtos.CommentRequest;
import ag.exceptions.ApplicationError;
import ag.models.Comment;
import ag.models.Voting;
import ag.models.VotingType;
import ag.service.CommentService;
import ag.service.UserService;
import ag.token.JwtUtil;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.logging.Logger;

@RestController
@RequiredArgsConstructor
@RequestMapping
public class CommentController {
    private final JwtUtil jwtUtil;
    private final UserService userService;
    private final CommentService commentService;
    private static final Logger logger = Logger.getLogger(VotingController.class.getName());

    @PostMapping("api/voting/{voting_id}/addComment")
    public ResponseEntity<?> addComment(
            @RequestHeader("Authorization") String accessToken,
            @RequestBody CommentRequest commentRequest,
            @PathVariable Integer voting_id
    ) {
        try {
            jwtUtil.checkExpiration(accessToken);
            commentService.addComment(Comment.builder()
                    .text(commentRequest.getText())
                    .user_id(userService.findByLogin(jwtUtil.getLogin(accessToken)).get().getId())
                    .voting_id(voting_id)
                    .publication_date(new Date())
                    .build()
            );
            return ResponseEntity.ok("comment added");
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
                            "failed to add comment"
                    ),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("api/voting/{voting_id}/comments")
    public ResponseEntity<?> getComments(
            @PathVariable Integer voting_id
    ) {
        return ResponseEntity.ok(commentService.getComments(voting_id));
    }
}
