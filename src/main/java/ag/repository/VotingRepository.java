package ag.repository;

import ag.controllers.VotingController;
import ag.exceptions.UserAlreadyVotedException;
import ag.models.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Types;
import java.util.Comparator;
import java.util.List;
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
                        "INSERT INTO voting (title, description, category, type, creator_id) VALUES (?, ?, ?, ?, ?)",
                        Statement.RETURN_GENERATED_KEYS
                );
                ps.setString(1, voting.getTitle());
                ps.setString(2, voting.getDescription());
                ps.setString(3, voting.getCategory());
                ps.setObject(4, voting.getType().toString().toLowerCase(), Types.OTHER);
                ps.setInt(5, voting.getCreator_id());
                return ps;
            }, keyHolder);
            var keys = keyHolder.getKeyList();
            id = (Integer) keys.get(0).get("id");
        } catch (Throwable e) {
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
            id = (Integer) keys.get(0).get("id");
        } catch (Throwable e) {
            logger.severe(e.getMessage());
            throw e.getCause();
        }
        return id;
    }

    @Transactional
    public Integer addVote(Vote vote, Integer voting_id) throws Throwable {
        Integer id;
        var votes = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM vote v JOIN position p ON v.position_id = p.id WHERE v.user_id = ? AND p.voting_id = ?",
                Integer.class,
                vote.getUser_id(),
                voting_id
        );
        if (votes > 0) {
            throw new UserAlreadyVotedException();
        }
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
            id = (Integer) keys.get(0).get("id");
        } catch (Throwable e) {
            logger.severe(e.getMessage());
            throw e.getCause();
        }
        return id;
    }

    @Transactional
    public void deleteVoting(Integer voting_id) throws Throwable {
        try {
            jdbcTemplate.update("DELETE FROM vote WHERE position_id IN (SELECT id FROM position WHERE voting_id=?)", voting_id);
            jdbcTemplate.update("DELETE FROM position WHERE voting_id=?", voting_id);
            jdbcTemplate.update("DELETE FROM voting WHERE id=?", voting_id);
        } catch (Throwable e) {
            logger.severe(e.getMessage());
            throw e.getCause();
        }
    }

    @Transactional
    public String getCreator(Integer voting_id) throws Throwable {
        try {
            return jdbcTemplate.queryForObject(
                    "SELECT login FROM account WHERE id=(SELECT creator_id FROM voting WHERE id=?)",
                    String.class,
                    voting_id
            );
        } catch (Throwable e) {
            logger.severe(e.getMessage());
            throw e.getCause();
        }
    }

    @Transactional
    public List<Voting> getVotingFromCategory(String category) {
        return jdbcTemplate.query(
                "SELECT * FROM voting WHERE category=?",
                new Object[]{category},
                (resultSet, rowNum) -> Voting.builder()
                        .id(resultSet.getInt("id"))
                        .title(resultSet.getString("title"))
                        .description(resultSet.getString("description"))
                        .category(resultSet.getString("category"))
                        .creator_id(resultSet.getInt("creator_id"))
                        .type(VotingType.valueOf(resultSet.getString("type").toUpperCase()))
                        .build()
        );
    }

    @Transactional
    public List<Voting> getCreatedVoting(Integer user_id) {
        return jdbcTemplate.query(
                "SELECT * FROM voting WHERE creator_id=?",
                new Object[]{user_id},
                (resultSet, rowNum) -> Voting.builder()
                        .id(resultSet.getInt("id"))
                        .title(resultSet.getString("title"))
                        .description(resultSet.getString("description"))
                        .category(resultSet.getString("category"))
                        .creator_id(resultSet.getInt("creator_id"))
                        .type(VotingType.valueOf(resultSet.getString("type").toUpperCase()))
                        .build()
        );
    }

    @Transactional
    public List<Voting> getTakenPartVoting(Integer user_id) {
        return jdbcTemplate.query(
                "SELECT v.* FROM voting v JOIN position p ON v.id = p.voting_id JOIN vote vt ON p.id = vt.position_id WHERE vt.user_id = ?",
                new Object[]{user_id},
                (resultSet, rowNum) -> Voting.builder()
                        .id(resultSet.getInt("id"))
                        .title(resultSet.getString("title"))
                        .description(resultSet.getString("description"))
                        .category(resultSet.getString("category"))
                        .creator_id(resultSet.getInt("creator_id"))
                        .type(VotingType.valueOf(resultSet.getString("type").toUpperCase()))
                        .build()
        );
    }

    @Transactional
    public List<RateVoting> getVotingRating() {
        var votingList = jdbcTemplate.query(
                "SELECT * FROM voting",
                new Object[]{},
                (resultSet, rowNum) -> {
                    RateVoting rateVoting = RateVoting.builder()
                            .id(resultSet.getInt("id"))
                            .title(resultSet.getString("title"))
                            .description(resultSet.getString("description"))
                            .category(resultSet.getString("category"))
                            .creator_id(resultSet.getInt("creator_id"))
                            .type(VotingType.valueOf(resultSet.getString("type").toUpperCase()))
                            .build();
                    rateVoting.setVotes_num(jdbcTemplate.queryForObject(
                            "SELECT count(*) FROM vote v JOIN position p ON v.position_id = p.id WHERE p.voting_id = ?",
                            Integer.class,
                            rateVoting.getId()
                    ));
                    return rateVoting;
                }
        );
        votingList.sort(Comparator.comparing(RateVoting::getVotes_num).reversed());
        return votingList;
    }

    @Transactional
    public List<Position> getPositionsForVoting(Integer voting_id) {
        return jdbcTemplate.query("SELECT * FROM position WHERE voting_id=?", new BeanPropertyRowMapper<>(Position.class), voting_id);
    }
}
