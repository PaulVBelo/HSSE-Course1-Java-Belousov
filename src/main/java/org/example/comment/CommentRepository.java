package org.example.comment;

import org.example.article.ArticleId;
import org.example.records.CommentDTO;

import java.util.Optional;

public interface CommentRepository {
  public Optional<Comment> getComment(CommentId commentId);
  public Comment addComment(CommentDTO commentDTO, ArticleId articleId);
  public void deleteComment(CommentId id);
}
