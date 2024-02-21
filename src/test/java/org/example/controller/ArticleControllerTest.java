package org.example.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.article.Article;
import org.example.article.ArticleRepository;
import org.example.article.ArticleRepositoryWithDBChecks;
import org.example.comment.Comment;
import org.example.comment.CommentRepository;
import org.example.comment.CommentRepositoryWithDBChecks;
import org.example.controller.ArticleController;
import org.example.controller.Controller;
import org.example.controller.TemplateFactory;
import org.example.records.ArticleDTO;
import org.example.records.CommentDTO;
import org.flywaydb.core.Flyway;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.org.apache.commons.lang3.StringUtils;
import spark.Service;

import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Testcontainers
class ArticleControllerTest {
  //Я немного отошёл от теста просто методов, что печально. Хотя бы протестирую все исходы.
  @Container
  public static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:15");

  private static Jdbi jdbi;
  private static Service service;
  private static ObjectMapper objectMapper;
  private static Controller controller;

  @BeforeAll
  static void setController(){
    String postgresJdbcUrl = POSTGRES.getJdbcUrl();
    Flyway flyway =
        Flyway.configure()
            .outOfOrder(true)
            .locations("classpath:db/migrations")
            .dataSource(postgresJdbcUrl, POSTGRES.getUsername(), POSTGRES.getPassword())
            .load();
    flyway.migrate();
    jdbi = Jdbi.create(postgresJdbcUrl, POSTGRES.getUsername(), POSTGRES.getPassword());

    ArticleRepository articleRepository = new ArticleRepositoryWithDBChecks(jdbi);
    CommentRepository commentRepository = new CommentRepositoryWithDBChecks(jdbi);
    service = Service.ignite();
    service.staticFileLocation("/web");
    objectMapper = new ObjectMapper();

    controller = new ArticleController(
        articleRepository,
        commentRepository,
        objectMapper,
        service,
        TemplateFactory.freeMarkerEngine()
    );
    controller.init();
    service.awaitInitialization();

  }

  @BeforeEach
  void setUp() {
    jdbi.useTransaction(handle -> handle.createUpdate("DELETE FROM comment; DELETE FROM article;").execute());
  }

  private HttpResponse<String> createArticle(ArticleDTO articleDTO) throws Exception{
    return HttpClient.newHttpClient().send(
        HttpRequest
            .newBuilder()
            .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(articleDTO)))
            .uri(URI.create("http://localhost:%d/api/article/create".formatted(service.port())))
            .build(),
        HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
  }

  private HttpResponse<String> createMultipleArticles(List<ArticleDTO> articleDTOList) throws Exception{
    return HttpClient.newHttpClient().send(
    HttpRequest
        .newBuilder()
        .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(articleDTOList)))
        .uri(URI.create("http://localhost:%d/api/article/cascade".formatted(service.port())))
        .build(),
        HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
  }

  private HttpResponse<String> getArticle(long id) throws Exception{
    return HttpClient.newHttpClient().send(
        HttpRequest
            .newBuilder()
            .GET()
            .uri(URI.create("http://localhost:%d/api/article/%d".formatted(service.port(), id)))
            .build(),
        HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
  }

  private HttpResponse<String> getArticleList() throws Exception{
    return HttpClient.newHttpClient().send(
        HttpRequest
            .newBuilder()
            .GET()
            .uri(URI.create("http://localhost:%d/api/list/articles".formatted(service.port())))
            .build(),
        HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
  }

  private HttpResponse<String> editArticle(long id, ArticleDTO articleDTO) throws Exception{
    return HttpClient.newHttpClient().send(
        HttpRequest
            .newBuilder()
            .PUT(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(articleDTO)))
            .uri(URI.create("http://localhost:%d/api/article/%d".formatted(service.port(), id)))
            .build(),
        HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
  }

  private HttpResponse<String> deleteArticle(long id) throws Exception{
    return HttpClient.newHttpClient().send(
        HttpRequest
            .newBuilder()
            .DELETE()
            .uri(URI.create("http://localhost:%d/api/article/%d".formatted(service.port(), id)))
            .build(),
        HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
  }

  private HttpResponse<String> createComment(CommentDTO commentDTO, long articleId) throws Exception{
    return HttpClient.newHttpClient().send(
        HttpRequest
            .newBuilder()
            .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(commentDTO)))
            .uri(URI.create("http://localhost:%d/api/article/%d/comment/create".formatted(service.port(), articleId)))
            .build(),
        HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
  }

  private HttpResponse<String> deleteComment(long id) throws Exception{
    return HttpClient.newHttpClient().send(
        HttpRequest
            .newBuilder()
            .DELETE()
            .uri(URI.create("http://localhost:%d/api/comment/%d".formatted(service.port(), id)))
            .build(),
        HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
  }

  @Test
  void endToEndExampleTest() throws Exception{
    String debugTitle = "cat";
    String debugNextTitle = "puss in boots";
    String debugContent = "meow";
    Map<String, Long> debugTags = new HashMap<>();
    debugTags.put("animals", 0l);

    var response1 = createArticle(new ArticleDTO(debugTitle, debugTags));
    Assertions.assertEquals(201, response1.statusCode());
    Article article = objectMapper.readValue(response1.body(), Article.class);
    Assertions.assertEquals(debugTitle, article.getTitle());
    Assertions.assertTrue(article.getTags().size()==1 && article.getTags().contains("animals"));
    Assertions.assertFalse(article.isTrending());

    var response2 = createComment(new CommentDTO(debugContent), article.getArticleId().value());
    Assertions.assertEquals(201, response2.statusCode());
    Comment comment = objectMapper.readValue(response2.body(), Comment.class);
    Assertions.assertEquals(debugContent, comment.getContent());

    var response3 = editArticle(article.getArticleId().value(), new ArticleDTO(debugNextTitle, new HashMap<>()));
    Assertions.assertEquals(200, response3.statusCode());
    Article articleUpd = objectMapper.readValue(response3.body(), Article.class);
    Assertions.assertEquals(debugNextTitle, articleUpd.getTitle());
    Assertions.assertTrue(articleUpd.getTags().size()==1 && articleUpd.getTags().contains("animals"));
    Assertions.assertFalse(articleUpd.isTrending());

    var response4 = deleteComment(comment.getCommentId().value());
    Assertions.assertEquals(200, response4.statusCode());
    Assertions.assertEquals("Comment with ID " + comment.getCommentId().value() + " has been successfully deleted.",
        response4.body());

    var response5 = getArticle(article.getArticleId().value());
    Assertions.assertEquals(200, response4.statusCode());
    Article articleNoCom = objectMapper.readValue(response5.body(), Article.class);
    Assertions.assertTrue(articleNoCom.getCommentIdList().size()==0);
    Assertions.assertFalse(articleUpd.isTrending());
  }

  @Test
  void deleteNothingTest() throws Exception {
    var response1 = deleteArticle(1l);
    Assertions.assertEquals(400, response1.statusCode());
    Assertions.assertEquals("Cannot find an article with ID " + 1l,
        response1.body());

    var response2 = deleteComment(1l);
    Assertions.assertEquals(400, response2.statusCode());
    Assertions.assertEquals("Cannot find a comment with ID " + 1l,
        response2.body());
  }

  @Test
  void deleteArticleTest() throws Exception {
    String debugTitle = "cat";
    String debugNextTitle = "puss in boots";
    String debugContent = "meow";
    Map<String, Long> debugTags = new HashMap<>();
    debugTags.put("animals", 0l);

    var response1 = createArticle(new ArticleDTO(debugTitle, debugTags));
    Assertions.assertEquals(201, response1.statusCode());
    Article article = objectMapper.readValue(response1.body(), Article.class);
    Assertions.assertEquals(debugTitle, article.getTitle());
    Assertions.assertTrue(article.getTags().size()==1 && article.getTags().contains("animals"));

    var response2 = createComment(new CommentDTO(debugContent), article.getArticleId().value());
    Assertions.assertEquals(201, response2.statusCode());
    Comment comment1 = objectMapper.readValue(response2.body(), Comment.class);
    Assertions.assertEquals(debugContent, comment1.getContent());

    var response3 = createComment(new CommentDTO(debugContent), article.getArticleId().value());
    Assertions.assertEquals(201, response3.statusCode());
    Comment comment2 = objectMapper.readValue(response3.body(), Comment.class);
    Assertions.assertEquals(debugContent, comment2.getContent());

    var response4 = getArticle(article.getArticleId().value());
    Assertions.assertEquals(200, response4.statusCode());
    Article articleUpd = objectMapper.readValue(response4.body(), Article.class);
    Assertions.assertEquals(2, articleUpd.getCommentIdList().size());

    var response5 = deleteArticle(articleUpd.getArticleId().value());
    Assertions.assertEquals(200, response5.statusCode());
    Assertions.assertEquals("Article with ID " + articleUpd.getArticleId().value() +
                                      " has been successfully deleted with all the comments.",
        response5.body());
  }

  @Test
  void getArticleListTest() throws Exception {
    String debugTitle = "cat";
    String debugContent = "meow";
    Map<String, Long> debugTags = new HashMap<>();
    debugTags.put("animals", 0l);

    var response1 = createArticle(new ArticleDTO(debugTitle, debugTags));
    Assertions.assertEquals(201, response1.statusCode());
    Article article = objectMapper.readValue(response1.body(), Article.class);
    Assertions.assertEquals(debugTitle, article.getTitle());
    Assertions.assertTrue(article.getTags().size()==1 && article.getTags().contains("animals"));
    Assertions.assertFalse(article.isTrending());

    var response2 = createComment(new CommentDTO(debugContent), article.getArticleId().value());
    Assertions.assertEquals(201, response2.statusCode());
    Comment comment1 = objectMapper.readValue(response2.body(), Comment.class);
    Assertions.assertEquals(debugContent, comment1.getContent());

    var response3 = createComment(new CommentDTO(debugContent), article.getArticleId().value());
    Assertions.assertEquals(201, response3.statusCode());
    Comment comment2 = objectMapper.readValue(response3.body(), Comment.class);
    Assertions.assertEquals(debugContent, comment2.getContent());

    var response4 = createComment(new CommentDTO(debugContent), article.getArticleId().value());
    Assertions.assertEquals(201, response4.statusCode());
    Comment comment3 = objectMapper.readValue(response4.body(), Comment.class);
    Assertions.assertEquals(debugContent, comment3.getContent());

    var response5 = createComment(new CommentDTO(debugContent), article.getArticleId().value());
    Assertions.assertEquals(201, response5.statusCode());
    Comment comment4 = objectMapper.readValue(response5.body(), Comment.class);
    Assertions.assertEquals(debugContent, comment4.getContent());

    var html = getArticleList();
    Assertions.assertEquals(200, html.statusCode());
    Assertions.assertEquals(5, StringUtils.countMatches(html.body(), "<td>"));
    Assertions.assertTrue(html.body().contains(""+article.getArticleId().value()));
    Assertions.assertTrue(StringUtils.contains(html.body(), debugTitle));
    Assertions.assertTrue(StringUtils.contains(html.body(), "animals"));
    Assertions.assertTrue(StringUtils.contains(html.body(), "4"));
    Assertions.assertTrue(StringUtils.contains(html.body(), "Trending"));
  }

  @Test
  void createMultipleArticlesTest() throws Exception {
    String debugTitle = "cat";
    String altDebugTitle = "feline";
    Map<String, Long> debugTags = new HashMap<>();
    debugTags.put("animals", 0l);
    HashSet<String> debugTagSet = new HashSet<>(debugTags.keySet());

    ArticleDTO articleDTO1 = new ArticleDTO(debugTitle, debugTags);
    ArticleDTO articleDTO2 = new ArticleDTO(altDebugTitle, debugTags);
    ArticleDTO articleDTO3 = new ArticleDTO("", debugTags);

    List<ArticleDTO> legal = new ArrayList<>();
    legal.add(articleDTO1);
    legal.add(articleDTO2);

    List<ArticleDTO> illegal = new ArrayList<>();
    illegal.add(articleDTO1);
    illegal.add(articleDTO2);
    illegal.add(articleDTO3);

    var response1 = createMultipleArticles(illegal);
    Assertions.assertEquals(400, response1.statusCode());
    Assertions.assertEquals("Some JSON objects are invalid! " +
        1 + " of DTO's have an empty title or no tags." , response1.body());

    var response2 = createMultipleArticles(legal);
    Assertions.assertEquals(201, response2.statusCode());
    List<Article> articleList = objectMapper.readValue(response2.body(),
        new TypeReference<>() {
      @Override
      public Type getType() {
        return super.getType();
      }
    });
    Assertions.assertEquals(2, articleList.size());

    Assertions.assertEquals(debugTitle, articleList.get(0).getTitle());
    Assertions.assertEquals(debugTagSet, articleList.get(0).getTags());
    Assertions.assertFalse(articleList.get(0).isTrending());

    Assertions.assertEquals(altDebugTitle, articleList.get(1).getTitle());
    Assertions.assertEquals(debugTagSet, articleList.get(1).getTags());
    Assertions.assertFalse(articleList.get(1).isTrending());
  }

  @Test
  void illegalCreationTests() throws Exception {
    String debugTitle = "cat";
    String debugContent = "meow";
    Map<String, Long> debugTags = new HashMap<>();

    var response1 = createArticle(new ArticleDTO(debugTitle, debugTags));
    Assertions.assertEquals(400, response1.statusCode());
    Assertions.assertEquals("At least one tag is required",
        response1.body());

    debugTags.put("animals", 0l);
    
    var response2 = createArticle(new ArticleDTO("", debugTags));
    Assertions.assertEquals(400, response2.statusCode());
    Assertions.assertEquals("Title cannot be an empty String",
        response2.body());

    var response3 = createComment(new CommentDTO(""), 1l);
    Assertions.assertEquals(400, response3.statusCode());
    Assertions.assertEquals("Cannot find an article with ID 1", response3.body());

    var response4 = createArticle(new ArticleDTO(debugTitle, debugTags));
    Assertions.assertEquals(201, response4.statusCode());
    Article article = objectMapper.readValue(response4.body(), Article.class);
    Assertions.assertEquals(debugTitle, article.getTitle());
    Assertions.assertTrue(article.getTags().size()==1 && article.getTags().contains("animals"));
    Assertions.assertFalse(article.isTrending());

    var response5 = createComment(new CommentDTO(""), article.getArticleId().value());
    Assertions.assertEquals(400, response5.statusCode());
    Assertions.assertEquals("Comment content cannot be an empty String", response5.body());
  }
}