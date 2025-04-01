package wang.zhanwei.rest.proxy.error;

import jakarta.servlet.http.HttpServletRequest;

public class InvalidRequestException extends Exception {
  private HttpServletRequest request;

  public InvalidRequestException(Throwable cause, HttpServletRequest request) {
    super(null, cause);
    this.request = request;
  }

  public HttpServletRequest getRequest() {
    return request;
  }
}
