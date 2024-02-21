package org.example.article;

import org.example.comment.CommentId;
import org.example.exceptions.EmptyStringException;
import org.example.exceptions.NoTagsException;
import org.example.exceptions.ValidationException;
import org.example.records.ArticleDTO;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;

import java.util.*;
import java.util.stream.Collectors;

public class ArticleRepositoryWithDBChecks implements ArticleRepository{
  private final Jdbi jdbi;

  public ArticleRepositoryWithDBChecks(Jdbi jdbi) {
    this.jdbi = jdbi;
  }

  @Override
  public List<Article> getAllArticles() {
    return jdbi.inTransaction((Handle handle) -> {
      var result = handle.createQuery("SELECT * FROM article")
          .mapToMap().list();
      return result.stream().map(Article::parseMap).toList();
    });
  }

  @Override
  public Optional<Article> getArticle(ArticleId articleId) {
    return jdbi.inTransaction((Handle handle) -> {
      var result =
          handle.createQuery("SELECT * FROM article WHERE article_id = :article_id")
              .bind("article_id", articleId.value())
              .mapToMap()
              .stream().findFirst();
      if (result.isEmpty()) {
        return Optional.empty();
      }
      return Optional.of(Article.parseMap(result.get()));
    });
  }

  @Override
  public Article addArticle(ArticleDTO articleDTO) {
    if (articleDTO.title().equals("")) {throw new EmptyStringException("Title cannot be an empty String");}
    if (articleDTO.tags() == null) {throw new NoTagsException("At least one tag is required");}
    if (articleDTO.tags().size()==0) {throw new NoTagsException("At least one tag is required");}
    return jdbi.inTransaction((Handle handle) -> {
      var result = handle.createUpdate("INSERT INTO article (title, tags, trending) VALUES (:title, :tags, :trending);")
          .bind("title", articleDTO.title())
          .bind("tags", articleDTO.tags().keySet()
              .stream().collect(Collectors.joining(", ")))
          .bind("trending", false)
          .executeAndReturnGeneratedKeys("article_id").mapToMap().findFirst();
      long generatedID = (long) result.get().get("article_id");
      return new Article(new ArticleId(generatedID), articleDTO.title(), new HashSet<>(articleDTO.tags().keySet()));
    });
  }

  @Override
  public List<Article> massAddArticles(List<ArticleDTO> articleDTOList) {
    List<ArticleDTO> invalidDTOs = new ArrayList<>();
    List<Article> articleList = new ArrayList<>();
    for (int i=0; i<articleDTOList.size(); ++i) {
      ArticleDTO articleDTO = articleDTOList.get(i);
      if (articleDTO.title().equals("") || articleDTO.tags()==null) {
        invalidDTOs.add(articleDTO);
      } else if (articleDTO.tags().size()==0) {
        invalidDTOs.add(articleDTO);
      }

    }

    if (invalidDTOs.size()==0) {
      for (int i=0; i<articleDTOList.size(); ++i) {
        articleList.add(addArticle(articleDTOList.get(i)));
      }
      return articleList;
    } else {
      throw new ValidationException("Some JSON objects are invalid!",
          invalidDTOs.size() + " of DTO's have an empty title or no tags.");
    }
  }

  @Override
  public Article editArticle(Article articleFrom, ArticleDTO articleTo) {
    String titleTo;
    if (!articleTo.title().equals("")) {
      titleTo = articleTo.title();
    } else {
      titleTo = articleFrom.getTitle();
    }

    Map<String, Long> tagMap;
    HashSet<String> currTagSet = articleFrom.getTags();
    Map<String, Long> currTagMap = new HashMap<>();

    for (String tag:
        currTagSet ) {
      currTagMap.put(tag, 0l);
    }

    if (articleTo.tags()!=null) {
      if (articleTo.tags().size()!=0) {
        tagMap = articleTo.tags();
      } else {
        tagMap = currTagMap;
      }
    } else {
      tagMap = currTagMap;
    }

    String tagsTo = tagMap.keySet()
        .stream().collect(Collectors.joining(", "));

    jdbi.useTransaction((Handle handle) -> {
      handle.createUpdate("UPDATE article SET " +
                "title = :title, " +
                "tags = :tags " +
                "WHERE article_id = :id")
            .bind("title", titleTo)
            .bind("tags", tagsTo)
            .bind("id", articleFrom.getArticleId().value())
            .execute();
    });
    return getArticle(articleFrom.getArticleId()).get();
  }

  @Override
  public void deleteArticle(Article articleToDelete) {
    jdbi.useTransaction((Handle handle) -> {
      handle.createUpdate("DELETE FROM article WHERE article_id = :article_id")
          .bind("article_id", articleToDelete.getArticleId().value())
          .execute();
    });
  }

  @Override
  public void addCommentToArticle(Article article, CommentId commentId) {
    List<CommentId> commentIdList = article.getCommentIdList();
    commentIdList.add(commentId);
    if (commentIdList.size()>3) {article.setTrending(true);}
    jdbi.useTransaction((Handle handle) -> {
      handle.createUpdate("UPDATE article SET " +
              "comments = :comments, " +
              "trending = :trending " +
              "WHERE article_id = :id")
          .bind("comments", commentIdList.stream()
              .map(CommentId::value)
              .map(String::valueOf)
              .collect(Collectors.joining(", ")))
          .bind("trending", article.isTrending())
          .bind("id", article.getArticleId().value())
          .execute();
    });
  }

  @Override
  public void deleteCommentFromArticle(Article article, CommentId commentId) {
    List<CommentId> commentIdList = article.getCommentIdList();
    commentIdList.remove(commentId);
    if (commentIdList.size()<=3) {article.setTrending(false);}
    jdbi.useTransaction((Handle handle) -> {
      handle.createUpdate("UPDATE article SET " +
              "comments = :comments, " +
              "trending = :trending " +
              "WHERE article_id = :id")
          .bind("comments", commentIdList.stream()
              .map(CommentId::value)
              .map(String::valueOf)
              .collect(Collectors.joining(", ")))
          .bind("trending", article.isTrending())
          .bind("id", article.getArticleId().value())
          .execute();
    });
  }
}
