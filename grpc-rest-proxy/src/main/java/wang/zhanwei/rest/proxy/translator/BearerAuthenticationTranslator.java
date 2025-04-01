package wang.zhanwei.rest.proxy.translator;

import io.grpc.CallCredentials;
import jakarta.servlet.http.HttpServletRequest;
import net.devh.boot.grpc.client.security.CallCredentialsHelper;
import net.devh.boot.grpc.common.security.SecurityConstants;

public class BearerAuthenticationTranslator implements AuthenticationTranslator {
  public static final String AUTHORIZATION_HEADER = "Authorization";

  public CallCredentials translate(HttpServletRequest request) {
    String token = request.getHeader(AUTHORIZATION_HEADER);

    if (token != null) {
      return CallCredentialsHelper.bearerAuth(
          token.substring(SecurityConstants.BEARER_AUTH_PREFIX.length()));
    }

    return null;
  }
}
