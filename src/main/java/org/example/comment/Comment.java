package org.example.comment;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.example.article.ArticleId;

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
