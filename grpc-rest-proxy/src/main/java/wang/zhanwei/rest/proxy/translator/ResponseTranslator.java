package wang.zhanwei.rest.proxy.translator;

import com.google.api.HttpRule;
import com.google.protobuf.Message;
import org.springframework.http.ResponseEntity;

public interface ResponseTranslator {
  ResponseEntity<String> translate(HttpRule rule, Message responseMessage);
}
