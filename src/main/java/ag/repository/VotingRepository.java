package ag.repository;

import ag.controllers.VotingController;
import ag.exceptions.UserAlreadyExistsException;
import ag.models.Position;
import ag.models.User;
import ag.models.Vote;
import ag.models.Voting;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.logging.Logger;

@Repository
@RequiredArgsConstructor
public class VotingRepository {
    private final JdbcTemplate jdbcTemplate;
    private final KeyHolder keyHolder = new GeneratedKeyHolder();
    private static final Logger logger = Logger.getLogger(VotingController.class.getName());
    @Transactional
    public Integer addVoting(Voting voting) throws Throwable {
        Integer id;
        try {
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(
                        "INSERT INTO voting (title, description) VALUES (?, ?)",
                        Statement.RETURN_GENERATED_KEYS
                );
                ps.setString(1, voting.getTitle());
                ps.setString(2, voting.getDescription());
                return ps;
            }, keyHolder);
            var keys = keyHolder.getKeyList();
            id = (Integer)keys.get(0).get("id");
        } catch (Exception e) {
            logger.severe(e.getMessage());
            throw e.getCause();
        }
        return id;
    }
    @Transactional
    public Integer addPosition(Position position) throws Throwable {
        Integer id;
        try {
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(
                        "INSERT INTO position (description, voting_id) VALUES (?, ?)",
                        Statement.RETURN_GENERATED_KEYS
                );
                ps.setString(1, position.getDescription());
                ps.setInt(2, position.getVoting_id());
                return ps;
            }, keyHolder);
            var keys = keyHolder.getKeyList();
            id = (Integer)keys.get(0).get("id");
        } catch (Exception e) {
            logger.severe(e.getMessage());
            throw e.getCause();
        }
        return id;
    }
    @Transactional
    public Integer addVote(Vote vote) throws Throwable {
        Integer id;
        try {
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(
                        "INSERT INTO vote (position_id, user_id) VALUES (?, ?)",
                        Statement.RETURN_GENERATED_KEYS
                );
                ps.setInt(1, vote.getPosition_id());
                ps.setInt(2, vote.getUser_id());
                return ps;
            }, keyHolder);
            var keys = keyHolder.getKeyList();
            id = (Integer)keys.get(0).get("id");
        } catch (Exception e) {
            logger.severe(e.getMessage());
            throw e.getCause();
        }
        return id;
    }
}
