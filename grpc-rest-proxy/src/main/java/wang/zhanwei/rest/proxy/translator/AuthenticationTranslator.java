package wang.zhanwei.rest.proxy.translator;

import io.grpc.CallCredentials;
import jakarta.servlet.http.HttpServletRequest;

public interface AuthenticationTranslator {
  CallCredentials translate(HttpServletRequest request);
}
