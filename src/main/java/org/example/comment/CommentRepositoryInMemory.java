package org.example.comment;

import org.example.article.ArticleId;
import org.example.article.ArticleRepository;
import org.example.exceptions.EmptyStringException;
import org.example.exceptions.OverlapException;
import org.example.exceptions.ValidationException;
import org.example.records.CommentDTO;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class CommentRepositoryInMemory implements CommentRepository{
  private ConcurrentHashMap<Long, Comment> commentMap;
  private ArticleRepository articleRepository;
  private final AtomicLong nextCommentId = new AtomicLong(0);

  public CommentRepositoryInMemory(ArticleRepository articleRepository) {
    this.commentMap = new ConcurrentHashMap<>();
    this.articleRepository = articleRepository;
  }

  private CommentId generateCommentId(){
    return new CommentId(nextCommentId.incrementAndGet());
  }

  @Override
  public synchronized Optional<Comment> getComment(CommentId commentId) {
    if (commentMap.get(commentId.value())!=null) {
      return Optional.of(commentMap.get(commentId.value()));
    } else {
      return Optional.empty();
    }
  }

  @Override
  public synchronized Comment addComment(CommentDTO commentDTO, ArticleId articleId)
      throws OverlapException, ValidationException, EmptyStringException {
    CommentId commentId = generateCommentId();
    if (commentMap.get(commentId.value())==null) {
      if (!articleRepository.getArticle(articleId).isEmpty()) {
        if (commentDTO.content()!=null) {
          if (commentDTO.content().equals("")) {
            throw new EmptyStringException("Comment content cannot be an empty string");
          }
          Comment comment = new Comment(commentId, commentDTO.content(), articleId);
          articleRepository.getArticle(articleId).get().addLinkedComment(comment);
          commentMap.put(commentId.value(), comment);
          return comment;
        } else {
          throw new EmptyStringException("Comment content cannot be null");
        }
      } else {
        throw new ValidationException("Invalid articleId", "Article with articleId " + articleId.value() + " does not exist");
      }
    } else {
      throw new OverlapException("Overlapping value", "commentId", ""+commentId.value());
    }
  }

  @Override
  public synchronized void deleteComment(CommentId commentId) {
    Comment comment = commentMap.get(commentId.value());
    if (comment!=null) {
      articleRepository.getArticle(comment.getArticleId()).get().removeComment(comment);
      commentMap.remove(commentId.value());
    }
  }
}
