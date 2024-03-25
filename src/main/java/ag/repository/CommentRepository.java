package ag.repository;

import ag.models.Comment;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class CommentRepository {
    private final JdbcTemplate jdbcTemplate;

    @Transactional
    public void addComment(Comment comment) throws Throwable {
        try {
            jdbcTemplate.update("INSERT INTO comment (text, user_id, voting_id, publication_date) VALUES (?, ?, ?, ?)",
                    comment.getText(),
                    comment.getUser_id(),
                    comment.getVoting_id(),
                    comment.getPublication_date()
            );
        } catch (Exception e) {
            throw e.getCause();
        }
    }

    @Transactional
    public List<Comment> getComments(Integer voting_id) {
        return jdbcTemplate.query(
                "SELECT * FROM comment WHERE voting_id=?",
                new BeanPropertyRowMapper<>(Comment.class),
                voting_id
        );
    }
}
