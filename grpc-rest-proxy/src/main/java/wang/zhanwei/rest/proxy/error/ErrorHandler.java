package wang.zhanwei.rest.proxy.error;

import io.grpc.Metadata;
import io.grpc.Status;
import java.util.Locale;
import org.springframework.http.ResponseEntity;

public interface ErrorHandler {
  ResponseEntity<String> handle(Status status, Metadata metadata, Locale locale);
  ResponseEntity<String> handle(Exception e, Metadata metadata, Locale locale);
  ResponseEntity<String> handle(InvalidRequestException e, Locale locale);
}
