package wang.zhanwei.rest.proxy.error;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.grpc.Metadata;
import io.grpc.Metadata.AsciiMarshaller;
import io.grpc.Status;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Base64;
import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

public class DefaultErrorHandler implements ErrorHandler {
  public static final Metadata.Key<String> ERROR_MESSAGE_KEY =
      Metadata.Key.of("grpc-rest-proxy-error-message", new ErrorMessageMarshaller());

  private static final class ErrorMessageMarshaller implements AsciiMarshaller<String> {
    @Override
    public String toAsciiString(String message) {
      return Base64.getEncoder().encodeToString(message.getBytes(Charset.forName("UTF8")));
    }

    @Override
    public String parseAsciiString(String serialized) {
      try {
        return new String(Base64.getDecoder().decode(serialized), "UTF8");
      } catch (UnsupportedEncodingException e) {
        throw new RuntimeException(e);
      }
    }
  }

  @Override
  public ResponseEntity<String> handle(Status status, Metadata metadata, Locale locale) {
    try {
      String body = resolveResponseBody(status, metadata);
      HttpStatus responseStatus = HttpStatus.INTERNAL_SERVER_ERROR;

      switch (status.getCode()) {
        case INVALID_ARGUMENT:
        case ALREADY_EXISTS:
        case RESOURCE_EXHAUSTED:
        case FAILED_PRECONDITION:
          responseStatus = HttpStatus.BAD_REQUEST;
          break;
        case DEADLINE_EXCEEDED:
          responseStatus = HttpStatus.REQUEST_TIMEOUT;
          break;
        case NOT_FOUND:
          responseStatus = HttpStatus.NOT_FOUND;
          break;
        case PERMISSION_DENIED:
          responseStatus = HttpStatus.UNAUTHORIZED;
          break;
        case UNAUTHENTICATED:
          responseStatus = HttpStatus.FORBIDDEN;
          break;
        case OUT_OF_RANGE:
          responseStatus = HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE;
          break;
      }

      return ResponseEntity.status(responseStatus)
          .contentType(MediaType.APPLICATION_JSON)
          .body(body);
    } catch (JsonProcessingException e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
    }
  }

  protected String resolveResponseMessage(Metadata metadata) {
    if (metadata != null) {
      return metadata.get(ERROR_MESSAGE_KEY);
    }

    return null;
  }

  protected String resolveResponseMessage(Status status) {
    String body = status.getDescription();

    if (body == null && status.getCause() != null) {
      body = resolveResponseMessage(status.getCause());
    }

    if (body == null) {
      body = status.getCode().toString();
    }

    return body;
  }

  protected String resolveResponseMessage(Throwable exception) {
    String message = null;

    if (exception.getCause() != null) {
      message = resolveResponseMessage(exception.getCause());
    }

    if (message != null) {
      return message;
    }

    return exception.getMessage();
  }

  protected boolean isJson(String message) {
    try {
      final ObjectMapper mapper = new ObjectMapper();
      mapper.readTree(message);
      return true;
    } catch (IOException e) {
      return false;
    }
  }

  protected String toJson(String body) throws JsonProcessingException {
    if (isJson(body)) {
      return body;
    }

    ObjectMapper mapper = new ObjectMapper();
    ErrorBody errorBody = new ErrorBody(body);
    return mapper.writeValueAsString(errorBody);
  }

  protected String resolveResponseBody(Status status, Metadata metadata)
      throws JsonProcessingException {
    String body = resolveResponseMessage(metadata);

    if (body == null) {
      body = resolveResponseMessage(status);
    }

    if (body == null) {
      body = "Unknown";
    }

    return toJson(body);
  }

  protected String resolveResponseBody(Exception e, Metadata metadata)
      throws JsonProcessingException {
    String body = resolveResponseMessage(metadata);

    if (body == null) {
      body = resolveResponseMessage(e);
    }

    if (body == null) {
      body = e.toString();
    }

    return toJson(body);
  }

  @Override
  public ResponseEntity<String> handle(Exception exception, Metadata metadata, Locale locale) {
    try {
      String body = resolveResponseBody(exception, metadata);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .contentType(MediaType.APPLICATION_JSON)
          .body(body);
    } catch (JsonProcessingException e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
    }
  }

  @Override
  public ResponseEntity<String> handle(InvalidRequestException exception, Locale locale) {
    try {
      String body = resolveResponseBody(exception, null);
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .contentType(MediaType.APPLICATION_JSON)
          .body(body);
    } catch (JsonProcessingException e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
    }
  }
}
