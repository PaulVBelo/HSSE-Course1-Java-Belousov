package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.example.article.ArticleRepository;
import org.example.article.ArticleRepositoryWithDBChecks;
import org.example.comment.CommentRepository;
import org.example.comment.CommentRepositoryWithDBChecks;
import org.example.controller.ArticleController;
import org.example.controller.Controller;
import org.example.controller.TemplateFactory;
import org.flywaydb.core.Flyway;
import org.jdbi.v3.core.Jdbi;
import spark.Service;

public class Main {
  public static void main(String[] args) {
    Config config = ConfigFactory.load();

    Flyway flyway =
        Flyway.configure()
            .outOfOrder(true)
            .locations("classpath:db/migrations")
            .dataSource(config.getString("app.database.url"), config.getString("app.database.user"),
                config.getString("app.database.password"))
            .load();
    flyway.migrate();

    Jdbi jdbi = Jdbi.create(
        config.getString("app.database.url"),
        config.getString("app.database.user"),
        config.getString("app.database.password")
    );

    ArticleRepository articleRepository = new ArticleRepositoryWithDBChecks(jdbi);
    CommentRepository commentRepository = new CommentRepositoryWithDBChecks(jdbi);
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