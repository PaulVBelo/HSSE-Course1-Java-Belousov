package org.example.comment;

import java.util.Objects;

public record CommentId(long value) {
  public CommentId {
    Objects.requireNonNull(value);
  }
}
