package org.example.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.article.Article;
import org.example.article.ArticleId;
import org.example.article.ArticleRepository;
import org.example.comment.Comment;
import org.example.comment.CommentId;
import org.example.comment.CommentRepository;
import org.example.exceptions.EmptyStringException;
import org.example.exceptions.NoTagsException;
import org.example.exceptions.ValidationException;
import org.example.records.ArticleDTO;
import org.example.records.CommentDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.ModelAndView;
import spark.Request;
import spark.Response;
import spark.Service;
import spark.template.freemarker.FreeMarkerEngine;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ArticleController implements Controller{
  private static final Logger LOG = LoggerFactory.getLogger(ArticleController.class);
  private final ArticleRepository articleRepository;
  private final CommentRepository commentRepository;
  private final ObjectMapper objectMapper;
  private final Service service;
  private final FreeMarkerEngine freeMarkerEngine;

  public ArticleController(ArticleRepository articleRepository,
                           CommentRepository commentRepository,
                           ObjectMapper objectMapper,
                           Service service,
                           FreeMarkerEngine freeMarkerEngine) {
    this.articleRepository = articleRepository;
    this.commentRepository = commentRepository;
    this.objectMapper = objectMapper;
    this.service = service;
    this.freeMarkerEngine = freeMarkerEngine;
  }

  @Override
  public void init() {
    service.init();
    service.awaitInitialization();
    LOG.debug("Article controller started");
    getArticle();
    getArticleList();
    createArticle();
    createMultipleArticles();
    editArticle();
    deleteArticle();
    createComment();
    deleteComment();
  }

  private void getArticle() {
    service.get(
        "/api/article/:articleId",
        (Request request, Response response) -> {
          response.type("application/json");
          ArticleId articleId = new ArticleId(Long.parseLong(request.params("articleId")));
          Optional<Article> optionalArticle = articleRepository.getArticle(articleId);
          if (optionalArticle.isEmpty()) {
            LOG.warn("Cannot find an article with ID " + articleId.value());
            response.status(400);
            return "Cannot find an article with ID " + articleId.value();
          }
          response.status(200);
          return objectMapper.writeValueAsString(optionalArticle.get());
        }
    );
  }

  private void getArticleList() {
    service.get(
        "/api/list/articles",
        (Request request, Response response) -> {
          response.type("application/json");
          LOG.debug("Trying to get the article list");

          response.type("text/html; charset=utf-8");
          List<Article> articles = articleRepository.getAllArticles();
          List<Map<String, String>> articleMapList =
              articles.stream()
                  .map(article -> Map.of(
                      "id", ""+article.getArticleId().value(),
                      "title", "" + article.getTitle(),
                      "tags", article.getTags().toString(),
                      "comments", "" + article.getCommentIdList().size(),
                      "trending", article.isTrending() ? "Trending" : "Not trending")
                  ).toList();
          Map<String, Object> model = new HashMap<>();
          model.put("articles", articleMapList);
          response.status(200);
          return freeMarkerEngine.render(new ModelAndView(model, "index.ftl"));
        }
    );
  }

  private void createArticle() {
    service.post(
        "/api/article/create",
        (Request request, Response response) -> {
          response.type("application/json");
          String body = request.body();
          ArticleDTO articleDTO = objectMapper.readValue(body,
              ArticleDTO.class);
          try {
            Article article = articleRepository.addArticle(articleDTO);
            response.status(201);
            return objectMapper.writeValueAsString(article);
          } catch (EmptyStringException e) {
            LOG.warn("Cannot create article", e);
            response.status(400);
            return e.getMessage();
          } catch (NoTagsException e) {
            LOG.warn("Cannot create article", e);
            response.status(400);
            return e.getMessage();
          }
        }
    );
  }

  private void createMultipleArticles() {
    service.post(
        "/api/article/cascade",
        (Request request, Response response) -> {
          response.type("application/json");
          String body = request.body();
          List<ArticleDTO> articleDTOList = objectMapper.readValue(body,
              new TypeReference<>() {
                @Override
                public Type getType() {
                  return super.getType();
                }
              });
          try {
            List<Article> article = articleRepository.massAddArticles(articleDTOList);
            response.status(201);
            return objectMapper.writeValueAsString(article);
          } catch (ValidationException e) {
            LOG.warn("Cannot validate cascade article creation request", e);
            response.status(400);
            return e.getMessage() + " " + e.reasonDescription;
          }
        }
    );
  }

  private void editArticle() {
    service.put(
        "/api/article/:articleId",
        (Request request, Response response) -> {
          LOG.debug("Trying to edit the article");

          ArticleId articleId = new ArticleId(Long.parseLong(request.params("articleId")));
          response.type("application/json");
          String body = request.body();
          ArticleDTO articleDTO = objectMapper.readValue(body,
              ArticleDTO.class);
          Optional<Article> optionalArticle = articleRepository.getArticle(articleId);
          if (optionalArticle.isEmpty()) {
            LOG.debug("Cannot find an article with ID " + articleId.value());
            response.status(400);
            return "Cannot find an article with ID " + articleId.value();
          }
          Article article = articleRepository.editArticle(optionalArticle.get(), articleDTO);
          response.status(200);
          return objectMapper.writeValueAsString(article);
        }
    );
  }

  private void deleteArticle() {
    service.delete(
        "/api/article/:articleId",
        (Request request, Response response) -> {
          LOG.debug("Trying to delete the article");

          ArticleId articleId = new ArticleId(Long.parseLong(request.params("articleId")));
          response.type("application/json");
          Optional<Article> optionalArticle = articleRepository.getArticle(articleId);

          if (optionalArticle.isEmpty()) {
            LOG.debug("Cannot find an article with ID " + articleId.value());
            response.status(400);
            return "Cannot find an article with ID " + articleId.value();
          }
          List<CommentId> commentsToDelete = optionalArticle.get().getCommentIdList();
          int size = commentsToDelete.size();
          for (int i=0; i<size; i++) {
            Optional<Comment> commentOptional = commentRepository.getComment(commentsToDelete.get(i));
            if (!commentOptional.isEmpty()) {
              commentRepository.deleteComment(commentOptional.get());
            }
          }
          articleRepository.deleteArticle(optionalArticle.get());
          response.status(200);
          return "Article with ID " + articleId.value() + " has been successfully deleted with all the comments.";
        }
    );
  }

  private void createComment() {
    service.post(
        "/api/article/:articleID/comment/create",
        (Request request, Response response) -> {
          ArticleId articleId = new ArticleId(Long.parseLong(request.params("articleId")));
          response.type("application/json");
          String body = request.body();
          CommentDTO commentDTO = objectMapper.readValue(body,
              CommentDTO.class);
          try {
            Optional<Article> optionalArticle = articleRepository.getArticle(articleId);
            if (optionalArticle.isEmpty()) {
              LOG.warn("Cannot find an article with ID " + articleId.value());
              response.status(400);
              return "Cannot find an article with ID " + articleId.value();
            }
            Comment comment = commentRepository.addComment(commentDTO, articleId);
            articleRepository.addCommentToArticle(optionalArticle.get(), comment.getCommentId());
            response.status(201);
            return objectMapper.writeValueAsString(comment);
          } catch (EmptyStringException e) {
            LOG.warn("Cannot create comment", e);
            response.status(400);
            return e.getMessage();
          }
        }
    );
  }

  private void deleteComment() {
    service.delete(
        "/api/comment/:commentId",
        (Request request, Response response) -> {
          LOG.debug("Trying to delete the comment");
          CommentId commentId = new CommentId(Long.parseLong(request.params("commentId")));
          response.type("application/json");
          Optional<Comment> optionalComment = commentRepository.getComment(commentId);
          if (optionalComment.isEmpty()) {
            LOG.debug("Cannot find a comment with ID " + commentId.value());
            response.status(400);
            return "Cannot find a comment with ID " + commentId.value();
          }
          Optional<Article> optionalArticle = articleRepository.getArticle(optionalComment.get().getArticleId());
          if (!optionalArticle.isEmpty()) {
            articleRepository.deleteCommentFromArticle(optionalArticle.get(), optionalComment.get().getCommentId());
          }
          commentRepository.deleteComment(optionalComment.get());
          response.status(200);
          return "Comment with ID " + commentId.value() + " has been successfully deleted.";
        }
    );
  }
}
