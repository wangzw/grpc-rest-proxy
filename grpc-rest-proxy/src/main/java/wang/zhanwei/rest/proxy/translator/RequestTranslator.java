package wang.zhanwei.rest.proxy.translator;

import com.google.api.HttpRule;
import com.google.protobuf.Message;
import jakarta.servlet.http.HttpServletRequest;
import wang.zhanwei.rest.proxy.error.InvalidRequestException;

public interface RequestTranslator {
  void translate(HttpRule rule, HttpServletRequest request, Message.Builder builder)
      throws InvalidRequestException;
}
