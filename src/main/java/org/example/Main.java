package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.article.ArticleRepository;
import org.example.article.ArticleRepositoryInMemory;
import org.example.comment.CommentRepository;
import org.example.comment.CommentRepositoryInMemory;
import org.example.controller.ArticleController;
import org.example.controller.Controller;
import org.example.controller.TemplateFactory;
import spark.Service;

public class Main {
  public static void main(String[] args) {
    ArticleRepository articleRepository = new ArticleRepositoryInMemory();
    CommentRepository commentRepository = new CommentRepositoryInMemory(articleRepository);
    Service service = Service.ignite();
    service.staticFileLocation("/web");
    ObjectMapper objectMapper = new ObjectMapper();

    Controller controller = new ArticleController(
        articleRepository,
        commentRepository,
        objectMapper,
        service,
        TemplateFactory.freeMarkerEngine()
    );
    controller.init();
  }
}