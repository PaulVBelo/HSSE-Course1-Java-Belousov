package org.example.article;

import org.example.comment.Comment;
import org.example.comment.CommentId;
import org.example.comment.CommentRepositoryWithDBChecks;
import org.example.exceptions.EmptyStringException;
import org.example.exceptions.NoTagsException;
import org.example.exceptions.ValidationException;
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

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
class ArticleRepositoryWithDBChecksTest {
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
  void getArticleTest(){
    String debugTitle = "cat";
    Map<String, Long> debugTags = new HashMap<>();
    debugTags.put("animals", 0l);

    Article article = articleRepository.addArticle(new ArticleDTO(debugTitle, debugTags));

    Assertions.assertEquals(article.getTitle(), articleRepository.getArticle(article.getArticleId()).get().getTitle());
    Assertions.assertEquals(article.getTags(), articleRepository.getArticle(article.getArticleId()).get().getTags());
    Assertions.assertEquals(article.getCommentIdList(), articleRepository.getArticle(article.getArticleId()).get().getCommentIdList());
    Assertions.assertTrue(articleRepository.getArticle(new ArticleId(article.getArticleId().value()+10l)).isEmpty());
  }

  @Test
  void addArticleTest(){
    String debugTitle = "cat";
    Map<String, Long> debugTags = new HashMap<>();
    debugTags.put("animals", 0l);

    Article article = articleRepository.addArticle(new ArticleDTO(debugTitle, debugTags));

    Assertions.assertEquals(debugTitle, article.getTitle());
    Assertions.assertEquals(new HashSet<>(debugTags.keySet()), article.getTags());
    Assertions.assertFalse(article.isTrending());

    Assertions.assertThrows(EmptyStringException.class, () -> articleRepository.addArticle(new ArticleDTO("", debugTags)));
    Assertions.assertThrows(NoTagsException.class, () -> articleRepository.addArticle(new ArticleDTO(debugTitle, null)));
    Assertions.assertDoesNotThrow(() -> articleRepository.addArticle(new ArticleDTO(debugTitle, debugTags)));
  }

  @Test
  void getAllArticlesTest() {
    String debugTitle = "cat";
    Map<String, Long> debugTags = new HashMap<>();
    debugTags.put("animals", 0l);

    Article article1 = articleRepository.addArticle(new ArticleDTO(debugTitle, debugTags));
    Article article2 = articleRepository.addArticle(new ArticleDTO(debugTitle, debugTags));

    Assertions.assertEquals(2, articleRepository.getAllArticles().size());
  }

  @Test
  void massAddArticlesTest() {
    String debugTitle = "cat";
    Map<String, Long> debugTags = new HashMap<>();
    debugTags.put("animals", 0l);

    ArticleDTO articleDTO = new ArticleDTO(debugTitle, debugTags);
    ArticleDTO articleDTOJr = new ArticleDTO(debugTitle, debugTags);
    ArticleDTO articleDTOIncorrect = new ArticleDTO("", debugTags);

    List<ArticleDTO> articleDTOList = new ArrayList<>();
    articleDTOList.add(articleDTO);
    articleDTOList.add(articleDTOJr);

    Assertions.assertEquals(2, articleRepository.massAddArticles(articleDTOList).size());

    Assertions.assertDoesNotThrow(() -> articleRepository.massAddArticles(articleDTOList));

    articleDTOList.add(articleDTOIncorrect);
    Assertions.assertThrows(ValidationException.class, () -> articleRepository.massAddArticles(articleDTOList));
  }

  @Test
  void editArticleTest() {
    String debugTitle = "cat";
    String altDebugTitle = "feline";
    Map<String, Long> debugTags = new HashMap<>();
    debugTags.put("animals", 0l);

    Article article = articleRepository.addArticle(new ArticleDTO(debugTitle, debugTags));
    articleRepository.editArticle(article, new ArticleDTO(altDebugTitle, debugTags));

    Assertions.assertEquals(altDebugTitle, articleRepository.getArticle(article.getArticleId()).get().getTitle());
  }

  @Test
  void deleteArticleTest() {
    String debugTitle = "cat";
    Map<String, Long> debugTags = new HashMap<>();
    debugTags.put("animals", 0l);

    Article article = articleRepository.addArticle(new ArticleDTO(debugTitle, debugTags));
    articleRepository.deleteArticle(article);

    Assertions.assertTrue(articleRepository.getArticle(article.getArticleId()).isEmpty());
  }

  @Test
  void commentsAndArticleInteractionsTest() {
    String debugTitle = "cat";
    String debugContent = "meow";
    Map<String, Long> debugTags = new HashMap<>();
    debugTags.put("animals", 0l);

    Article article = articleRepository.addArticle(new ArticleDTO(debugTitle, debugTags));
    Comment comment1 = commentRepository.addComment(new CommentDTO(debugContent), article.getArticleId());
    articleRepository.addCommentToArticle(article, comment1.getCommentId());
    Article article1comment = articleRepository.getArticle(article.getArticleId()).get();

    Assertions.assertEquals(1, article1comment.getCommentIdList().size());
    Assertions.assertFalse(article1comment.isTrending());

    Comment comment2 = commentRepository.addComment(new CommentDTO(debugContent), article.getArticleId());
    articleRepository.addCommentToArticle(article, comment2.getCommentId());
    Comment comment3 = commentRepository.addComment(new CommentDTO(debugContent), article.getArticleId());
    articleRepository.addCommentToArticle(article, comment3.getCommentId());
    Comment comment4 = commentRepository.addComment(new CommentDTO(debugContent), article.getArticleId());
    articleRepository.addCommentToArticle(article, comment4.getCommentId());
    Article article4comments = articleRepository.getArticle(article.getArticleId()).get();

    Assertions.assertEquals(4, article4comments.getCommentIdList().size());
    Assertions.assertTrue(article4comments.isTrending());

    articleRepository.deleteCommentFromArticle(article4comments, comment4.getCommentId());
    Article article3comments = articleRepository.getArticle(article.getArticleId()).get();

    Assertions.assertEquals(3, article3comments.getCommentIdList().size());
    Assertions.assertFalse(article3comments.isTrending());
  }
}