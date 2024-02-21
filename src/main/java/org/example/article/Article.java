package org.example.article;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import org.example.comment.Comment;
import org.example.comment.CommentId;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Article {
  private ArticleId articleId;
  private String title;
  private HashSet<String> tags;
  private List<CommentId> commentIdList;
  private boolean trending;


  public Article(
      ArticleId articleId,
      String title,
      HashSet<String> tags) {
    this.articleId = articleId;
    this.title = title;
    this.tags = tags;
    this.commentIdList = new ArrayList<>();
    this.trending = false;
  }

  @JsonCreator
  public Article(
      @JsonProperty("article_id") ArticleId articleId,
      @JsonProperty("title") String title,
      @JsonProperty("tags") HashSet<String> tags,
      @JsonProperty("comments") List<CommentId> commentIdList,
      @JsonProperty("trending") boolean trending) {
    this.articleId = articleId;
    this.title = title;
    this.tags = tags;
    if (commentIdList!=null) {
      this.commentIdList = commentIdList;
    } else {
      this.commentIdList = new ArrayList<>();
    }
    this.trending = trending;
  }

  public static Article parseMap(Map<String, Object> map) {
    String tagString = (String) map.get("tags");
    HashSet<String> tags= new HashSet<>(List.of(tagString.split("[,\\s]+")));
    String commentString = (String) map.get("comments");
    List<CommentId> commentIds = new ArrayList<>();
    if (commentString!=null) {
      List<String> stringCommentList = new ArrayList<>(List.of(commentString.split("[,\\s]+")));
      for (String s : stringCommentList) {
        if (s.equals("")) {break;}
        commentIds.add(new CommentId(Long.parseLong(s)));
      }
    }
    return new Article(new ArticleId((long) map.get("article_id")),
        (String) map.get("title"),
        tags,
        commentIds,
        (boolean) map.get("trending"));
  }

  public boolean isTrending() {
    return trending;
  }

  public void setTrending(boolean trending) {
    this.trending = trending;
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

  public List<CommentId> getCommentIdList() {
    return commentIdList;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public void setTags(HashSet<String> tags) {
    this.tags = tags;
  }
}
