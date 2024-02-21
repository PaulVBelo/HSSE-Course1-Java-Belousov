package org.example.records;

import org.example.comment.Comment;

import java.util.Objects;

public record CommentDTO(String content) {
  public CommentDTO {
    Objects.requireNonNull(content);
  }
}