package voteio.service;

import voteio.models.Comment;
import voteio.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;

    public void addComment(Comment comment) throws Throwable {
        commentRepository.addComment(comment);
    }

    public List<Comment> getComments(Integer voting_id) {
        return commentRepository.getComments(voting_id);
    }
}
