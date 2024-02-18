package org.example.article;

import org.example.records.ArticleDTO;

import java.util.ArrayList;
import java.util.Optional;

public interface ArticleRepository {
  public ArrayList<Article> getAllArticles();
  public Optional<Article> getArticle(ArticleId articleId);
  public Article addArticle(ArticleDTO articleDTO);
  public Article editArticle(Article articleFrom, ArticleDTO articleTo);
  public void deleteArticle(ArticleId id);
}
