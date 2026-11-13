package ai.maum.data.edit.common.filter;

public class AccessAuthorityException extends RuntimeException {
  
  public AccessAuthorityException(String message) {
    super(message);
  }

  public AccessAuthorityException(String message, Throwable cause) {
    super(message, cause);
  }
}
