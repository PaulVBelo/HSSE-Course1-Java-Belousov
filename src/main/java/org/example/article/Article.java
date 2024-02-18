package org.example.article;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.example.comment.Comment;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class Article {
  private ArticleId articleId;
  private String title;
  private HashSet<String> tags;
  private List<Comment> commentList;


  public Article(
      ArticleId articleId,
      String title,
      HashSet<String> tags) {
    this.articleId = articleId;
    this.title = title;
    this.tags = tags;
    this.commentList = new ArrayList<>();
  }

  @JsonCreator
  public Article(
      @JsonProperty("articleId") ArticleId articleId,
      @JsonProperty("title") String title,
      @JsonProperty("tags") HashSet<String> tags,
      @JsonProperty("comments") List<Comment> commentList) {
    this.articleId = articleId;
    this.title = title;
    this.tags = tags;
    this.commentList = commentList;
  }

  public ArticleId getArticleId() {
    return articleId;
  }

  public String getTitle() {
    return title;
  }

  public HashSet<String> getTags() {
    return tags;
  }

  public List<Comment> getCommentList() {
    return commentList;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public void setTags(HashSet<String> tags) {
    this.tags = tags;
  }

  public void addLinkedComment(Comment comment) {
    commentList.add(comment);
  }

  public void removeComment(Comment comment) {
    commentList.remove(comment);
  }

  public String parseCommentList() {
    List<String> commentsContent = new ArrayList<>();
    for (int i=0; i<commentList.size(); ++i) {
      commentsContent.add(commentList.get(i).getContent());
    }
    return commentsContent.toString();
  }
}
