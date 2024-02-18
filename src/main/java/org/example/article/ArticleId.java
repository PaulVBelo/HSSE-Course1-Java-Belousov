package org.example.article;

import java.util.Objects;

public record ArticleId(long value) {
  public ArticleId {
    Objects.requireNonNull(value);
  }
}