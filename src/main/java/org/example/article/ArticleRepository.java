package org.example.article;

import org.example.comment.Comment;
import org.example.comment.CommentId;
import org.example.records.ArticleDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public interface ArticleRepository {
  public List<Article> getAllArticles();
  public Optional<Article> getArticle(ArticleId articleId);
  public Article addArticle(ArticleDTO articleDTO);
  public List<Article> massAddArticles(List<ArticleDTO> articleDTOList);
  public Article editArticle(Article articleFrom, ArticleDTO articleTo);
  public void deleteArticle(Article articleToDelete);

  public void addCommentToArticle(Article article, CommentId commentId);
  public void deleteCommentFromArticle(Article article, CommentId commentId);
}
