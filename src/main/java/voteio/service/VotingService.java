package voteio.service;

import voteio.models.*;
import voteio.repository.VotingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VotingService {
    private final VotingRepository votingRepository;

    public Integer addVoting(Voting voting) throws Throwable {
         return votingRepository.addVoting(voting);
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

    public Voting getVoting(Integer voting_id) {
        return votingRepository.getVoting(voting_id);
    }

    public void removePosition(Integer voting_id, Integer position_id) throws Throwable {
        votingRepository.removePosition(voting_id, position_id);
    }

    public boolean isCreator(Integer voting_id, Integer user_id) throws Throwable {
        return votingRepository.isCreator(voting_id, user_id);
    }

    public Position getVotedPosition(Integer voting_id, Integer user_id) throws Throwable {
        return votingRepository.getVotedPosition(voting_id, user_id);
    }

    public Integer votesForPosition(Integer position_id) throws Throwable {
        return votingRepository.votesForPosition(position_id);
    }

    public Integer retractVote(Integer position_id, Integer user_id) throws Throwable {
        return votingRepository.retractVote(position_id, user_id);
    }

    public List<UserView> getUsersForPosition(Integer position_id) throws Throwable {
        return votingRepository.getUsersForPosition(position_id);
    }

    public List<Voting> searchByTitle(String title) throws Throwable {
        return votingRepository.searchByTitle(title);
    }
}
