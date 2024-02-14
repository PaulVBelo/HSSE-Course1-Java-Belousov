package org.example.exceptions;

public class ValidationException extends RuntimeException {
  public final String reasonDescription;

  public ValidationException(String message, String reasonDescription) {
    super(message);
    this.reasonDescription = reasonDescription;
  }
}
