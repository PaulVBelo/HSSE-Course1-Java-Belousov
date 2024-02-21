package org.example.comment;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import org.example.article.Article;
import org.example.article.ArticleId;

import java.util.Map;

public class Comment {
  private CommentId commentId;
  private String content;
  private ArticleId articleId;

  @JsonCreator
  public Comment(
      @JsonProperty("commentId") CommentId commentId,
      @JsonProperty("content") String content,
      @JsonProperty("articleId") ArticleId articleId) {
    this.commentId = commentId;
    this.content = content;
    this.articleId = articleId;
  }

  public static Comment parseMap(Map<String, Object> map) {
    return new Comment(new CommentId((long) map.get("comment_id")),
        (String) map.get("content"),
        new ArticleId((long) map.get("article_id")));
  }

  public CommentId getCommentId() {
    return commentId;
  }

  public String getContent() {
    return content;
  }

  public ArticleId getArticleId() {
    return articleId;
  }
}
