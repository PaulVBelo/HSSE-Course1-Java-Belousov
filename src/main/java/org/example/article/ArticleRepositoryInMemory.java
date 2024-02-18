package org.example.article;

import org.example.exceptions.EmptyStringException;
import org.example.exceptions.OverlapException;
import org.example.records.ArticleDTO;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class ArticleRepositoryInMemory implements ArticleRepository{
  private ConcurrentHashMap<Long, Article> articleMap;
  private final AtomicLong nextArticleId = new AtomicLong(0);

  public ArticleRepositoryInMemory() {
    this.articleMap = new ConcurrentHashMap<>();
  }

  private ArticleId generateArticleId(){
    return new ArticleId(nextArticleId.incrementAndGet());
  }

  @Override
  public synchronized ArrayList<Article> getAllArticles() {
    return new ArrayList<>(articleMap.values());
  }

  @Override
  public synchronized Optional<Article> getArticle(ArticleId articleId) {
    if (articleMap.get(articleId.value())==null) {
      return Optional.empty();
    } else {
      return Optional.of(articleMap.get(articleId.value()));
    }
  }

  @Override
  public synchronized Article addArticle(ArticleDTO articleDTO)
      throws EmptyStringException, OverlapException {
    ArticleId articleId = generateArticleId();
    if (articleMap.get(articleId.value()) == null) {
      if (articleDTO.title()!=null && articleDTO.tags()!=null){
        if (!articleDTO.title().equals("")) {
          Article article = new Article(articleId, articleDTO.title(), new HashSet<>(articleDTO.tags().keySet()));
          articleMap.put(articleId.value(), article);
          return article;
        } else {
          throw new EmptyStringException("Title String is empty!");
        }
      } else {
        throw new IllegalArgumentException("Title and tags cannot be null");
      }
    } else {
      throw new OverlapException("Overlapping value", "articleId", ""+articleId.value());
    }
  }

  @Override
  public synchronized Article editArticle(Article articleFrom, ArticleDTO articleTo) {
    if (articleTo.title()!=null) {
      if (!articleTo.title().equals("")){
        articleFrom.setTitle(articleTo.title());
      }
    }
    if (articleTo.tags()!=null) {
      articleFrom.setTags(new HashSet<>(articleTo.tags().keySet()));
    }
    return articleFrom;
  }

  @Override
  public synchronized void deleteArticle(ArticleId articleId) {
    articleMap.remove(articleId.value());
  }
}
