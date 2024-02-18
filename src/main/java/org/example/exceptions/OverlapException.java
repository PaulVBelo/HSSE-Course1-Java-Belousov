package org.example.exceptions;

public class OverlapException extends RuntimeException{
  public final String fieldName;
  public final String overlappingValue;

  public OverlapException(String message, String fieldName, String overlappingValue) {
    super(message);
    this.fieldName = fieldName;
    this.overlappingValue = overlappingValue;
  }
}
