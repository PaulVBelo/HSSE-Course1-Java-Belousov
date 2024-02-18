package org.example.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.article.Article;
import org.example.article.ArticleRepository;
import org.example.article.ArticleRepositoryInMemory;
import org.example.comment.Comment;
import org.example.comment.CommentRepository;
import org.example.comment.CommentRepositoryInMemory;
import org.example.records.ArticleDTO;
import org.example.records.CommentDTO;
import org.junit.jupiter.api.*;
import spark.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

class ArticleControllerTest {
  private ArticleRepository articleRepository;
  private CommentRepository commentRepository;
  private Service service;
  private ObjectMapper objectMapper;
  private Controller controller;

  @BeforeEach
  void setController(){
    this.articleRepository = new ArticleRepositoryInMemory();
    this.commentRepository = new CommentRepositoryInMemory(articleRepository);
    this.service = Service.ignite();
    service.staticFileLocation("/web");
    this.objectMapper = new ObjectMapper();

    this.controller = new ArticleController(
        articleRepository,
        commentRepository,
        objectMapper,
        service,
        TemplateFactory.freeMarkerEngine()
    );
    controller.init();
    service.awaitInitialization();

  }

  @AfterEach
  void afterEach() {
    service.stop();
    service.awaitStop();
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

    var response2 = createComment(new CommentDTO(debugContent), article.getArticleId().value());
    Assertions.assertEquals(201, response2.statusCode());
    Comment comment = objectMapper.readValue(response2.body(), Comment.class);
    Assertions.assertEquals(debugContent, comment.getContent());

    var response3 = editArticle(article.getArticleId().value(), new ArticleDTO(debugNextTitle, null));
    Assertions.assertEquals(200, response3.statusCode());
    Article articleUpd = objectMapper.readValue(response3.body(), Article.class);
    Assertions.assertEquals(debugNextTitle, articleUpd.getTitle());
    Assertions.assertTrue(articleUpd.getTags().size()==1 && articleUpd.getTags().contains("animals"));
    Assertions.assertEquals(debugContent, articleUpd.getCommentList().get(0).getContent());

    var response4 = deleteComment(comment.getCommentId().value());
    Assertions.assertEquals(200, response4.statusCode());
    Assertions.assertEquals("Comment with ID " + comment.getCommentId().value() + " has been successfully deleted.",
        response4.body());

    var response5 = getArticle(article.getArticleId().value());
    Assertions.assertEquals(200, response4.statusCode());
    Article articleNoCom = objectMapper.readValue(response5.body(), Article.class);
    Assertions.assertTrue(articleNoCom.getCommentList().size()==0);
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
  void noCommentsLeftBehindTest() throws Exception {
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

    var response4 = createComment(new CommentDTO(debugContent), article.getArticleId().value());
    Assertions.assertEquals(201, response4.statusCode());
    Comment comment3 = objectMapper.readValue(response4.body(), Comment.class);
    Assertions.assertEquals(debugContent, comment3.getContent());

    Assertions.assertEquals(3, articleRepository.getArticle(article.getArticleId()).get().getCommentList().size());

    var response5 = deleteArticle(article.getArticleId().value());
    Assertions.assertEquals(200, response5.statusCode());
    Assertions.assertTrue(articleRepository.getArticle(article.getArticleId()).isEmpty());
    Assertions.assertTrue(commentRepository.getComment(comment1.getCommentId()).isEmpty());
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

    var response2 = createComment(new CommentDTO(debugContent), article.getArticleId().value());
    Assertions.assertEquals(201, response2.statusCode());
    Comment comment1 = objectMapper.readValue(response2.body(), Comment.class);
    Assertions.assertEquals(debugContent, comment1.getContent());

    var html = getArticleList();
    Assertions.assertEquals(200, html.statusCode());
    Assertions.assertTrue(html.body().contains("1"));
    Assertions.assertTrue(html.body().contains("cat"));
    Assertions.assertTrue(html.body().contains("[animals]"));
    Assertions.assertTrue(html.body().contains("[meow]"));
  }
}