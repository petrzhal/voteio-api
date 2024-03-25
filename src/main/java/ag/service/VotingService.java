package ag.service;

import ag.models.*;
import ag.repository.VotingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VotingService {
    private final VotingRepository votingRepository;
    public Integer addVoting(Voting voting) throws Throwable {
        Integer id;
        try {
            id = votingRepository.addVoting(voting);
        } catch (Exception e) {
            throw e.getCause();
        }
        return id;
    }
    public Integer addPosition(Position position) throws Throwable {
        return votingRepository.addPosition(position);
    }
    public Integer addVote(Vote vote, Integer voting_id) throws Throwable {
        return votingRepository.addVote(vote, voting_id);
    }
    public void deleteVoting(Integer voting_id) throws Throwable {
        votingRepository.deleteVoting(voting_id);
    }
    public String getCreator(Integer voting_id) throws Throwable {
        return votingRepository.getCreator(voting_id);
    }
    public List<Voting> getVotingFromCategory(String category) {
        return votingRepository.getVotingFromCategory(category);
    }
    public List<Voting> getCreatedVoting(Integer user_id) {
        return votingRepository.getCreatedVoting(user_id);
    }

    public List<Voting> getTakenPartVoting(Integer user_id) {
        return votingRepository.getTakenPartVoting(user_id);
    }

    public List<RateVoting> getVotingRating() {
        return votingRepository.getVotingRating();
    }

    public List<Position> getPositionsForVoting(Integer voting_id) {
        return votingRepository.getPositionsForVoting(voting_id);
    }

    public void addComment() {

    }
}
