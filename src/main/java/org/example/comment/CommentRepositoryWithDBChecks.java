package org.example.comment;

import org.example.article.ArticleId;
import org.example.exceptions.EmptyStringException;
import org.example.records.CommentDTO;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;

import java.util.List;
import java.util.Optional;

public class CommentRepositoryWithDBChecks implements CommentRepository{
  private final Jdbi jdbi;

  public CommentRepositoryWithDBChecks(Jdbi jdbi) {
    this.jdbi = jdbi;
  }

  @Override
  public Optional<Comment> getComment(CommentId commentId) {
    return jdbi.inTransaction((Handle handle) -> {
      var result =
          handle.createQuery("SELECT * FROM comment WHERE comment_id = :comment_id")
              .bind("comment_id", commentId.value())
              .mapToMap()
              .stream().findFirst();
      if (result.isEmpty()) {
        return Optional.empty();
      }
      return Optional.of(Comment.parseMap(result.get()));
    });
  }

  @Override
  public Comment addComment(CommentDTO commentDTO, ArticleId articleId) {
    if (commentDTO.content().equals("")) {throw new EmptyStringException("Comment content cannot be an empty String");}
    return jdbi.inTransaction((Handle handle) -> {
      var result = handle.createUpdate("INSERT INTO comment (content, article_id) VALUES (:content, :article_id);")
          .bind("content", commentDTO.content())
          .bind("article_id", articleId.value())
          .executeAndReturnGeneratedKeys("comment_id").mapToMap().findFirst();
      long generatedID = (long) result.get().get("comment_id");
      return new Comment(new CommentId(generatedID), commentDTO.content(), articleId);
    });
  }

  @Override
  public void deleteComment(Comment commentToDelete) {
    jdbi.useTransaction((Handle handle) -> {
      handle.createUpdate("DELETE FROM comment WHERE comment_id = :comment_id")
          .bind("comment_id", commentToDelete.getCommentId().value()).execute();
    });
  }

  @Override
  public void massDeleteComments(List<Comment> commentList) {
    int listSize = commentList.size();
    for (int i=0; i<listSize; ++i) {
      deleteComment(commentList.get(i));
    }
  }
}
