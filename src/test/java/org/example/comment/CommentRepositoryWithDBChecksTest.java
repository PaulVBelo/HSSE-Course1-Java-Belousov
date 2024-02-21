package org.example.comment;

import org.example.article.Article;
import org.example.article.ArticleRepositoryWithDBChecks;
import org.example.exceptions.EmptyStringException;
import org.example.records.ArticleDTO;
import org.example.records.CommentDTO;
import org.flywaydb.core.Flyway;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.*;


@Testcontainers
class CommentRepositoryWithDBChecksTest {
  @Container
  public static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:15");

  private static Jdbi jdbi;

  private static ArticleRepositoryWithDBChecks articleRepository;
  private static CommentRepositoryWithDBChecks commentRepository;

  @BeforeAll
  static void beforeAll() {
    String postgresJdbcUrl = POSTGRES.getJdbcUrl();
    Flyway flyway =
        Flyway.configure()
            .outOfOrder(true)
            .locations("classpath:db/migrations")
            .dataSource(postgresJdbcUrl, POSTGRES.getUsername(), POSTGRES.getPassword())
            .load();
    flyway.migrate();
    jdbi = Jdbi.create(postgresJdbcUrl, POSTGRES.getUsername(), POSTGRES.getPassword());
    articleRepository = new ArticleRepositoryWithDBChecks(jdbi);
    commentRepository = new CommentRepositoryWithDBChecks(jdbi);
  }

  @BeforeEach
  void beforeEach() {
    jdbi.useTransaction(handle -> handle.createUpdate("DELETE FROM comment; DELETE FROM article;").execute());
  }

  @Test
  void getCommentTest() {
    String debugTitle = "cat";
    String debugContent = "meow";
    Map<String, Long> debugTags = new HashMap<>();
    debugTags.put("animals", 0l);

    Optional<Comment> optionalComment1 = commentRepository.getComment(new CommentId(1l));
    Assertions.assertTrue(optionalComment1.isEmpty());

    Article article = articleRepository.addArticle(new ArticleDTO(debugTitle, debugTags));
    Comment comment = commentRepository.addComment(new CommentDTO(debugContent), article.getArticleId());

    Assertions.assertEquals(comment.getContent(), commentRepository.getComment(comment.getCommentId()).get().getContent());
    Assertions.assertEquals(comment.getArticleId().value(), commentRepository.getComment(comment.getCommentId()).get().getArticleId().value());
    Assertions.assertTrue(commentRepository.getComment(new CommentId(comment.getCommentId().value()+10l)).isEmpty());
  }

  @Test
  void addCommentTest() {
    String debugTitle = "cat";
    String debugContent = "meow";
    Map<String, Long> debugTags = new HashMap<>();
    debugTags.put("animals", 0l);

    Article article = articleRepository.addArticle(new ArticleDTO(debugTitle, debugTags));
    Comment comment = commentRepository.addComment(new CommentDTO(debugContent), article.getArticleId());

    Assertions.assertEquals(debugContent, comment.getContent());
    Assertions.assertThrows(EmptyStringException.class, () -> commentRepository.addComment(new CommentDTO(""), article.getArticleId()));
    Assertions.assertDoesNotThrow(() -> commentRepository.addComment(new CommentDTO(debugContent), article.getArticleId()));
  }

  @Test
  void deleteCommentTest() {
    String debugTitle = "cat";
    String debugContent = "meow";
    Map<String, Long> debugTags = new HashMap<>();
    debugTags.put("animals", 0l);

    Article article = articleRepository.addArticle(new ArticleDTO(debugTitle, debugTags));
    Comment comment = commentRepository.addComment(new CommentDTO(debugContent), article.getArticleId());

    commentRepository.deleteComment(comment);

    Assertions.assertTrue(commentRepository.getComment(comment.getCommentId()).isEmpty());
  }

  @Test
  void massDeleteCommentTest() {
    String debugTitle = "cat";
    String debugContent = "meow";
    Map<String, Long> debugTags = new HashMap<>();
    debugTags.put("animals", 0l);

    Article article = articleRepository.addArticle(new ArticleDTO(debugTitle, debugTags));
    List<Comment> commentList = new ArrayList<>();
    commentList.add(commentRepository.addComment(new CommentDTO(debugContent), article.getArticleId()));
    commentList.add(commentRepository.addComment(new CommentDTO(debugContent), article.getArticleId()));

    commentRepository.massDeleteComments(commentList);

    Assertions.assertTrue(commentRepository.getComment(commentList.get(0).getCommentId()).isEmpty());
    Assertions.assertTrue(commentRepository.getComment(commentList.get(1).getCommentId()).isEmpty());
  }
}