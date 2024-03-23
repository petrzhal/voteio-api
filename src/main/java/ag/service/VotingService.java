package ag.service;

import ag.models.Position;
import ag.models.Vote;
import ag.models.Voting;
import ag.repository.VotingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
    public Integer addVote(Vote vote) throws Throwable {
        return votingRepository.addVote(vote);
    }
}
