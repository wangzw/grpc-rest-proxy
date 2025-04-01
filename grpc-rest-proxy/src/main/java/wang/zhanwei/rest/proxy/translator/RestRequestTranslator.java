package wang.zhanwei.rest.proxy.translator;

import com.google.api.HttpRule;
import com.google.gson.Gson;
import com.google.protobuf.Descriptors;
import com.google.protobuf.Message;
import com.google.protobuf.util.JsonFormat;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import org.apache.commons.io.IOUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerMapping;
import wang.zhanwei.rest.proxy.error.InvalidRequestException;

public class RestRequestTranslator implements RequestTranslator {
  protected JsonFormat.Parser bodyParser = JsonFormat.parser().ignoringUnknownFields();

  @Override
  public void translate(HttpRule rule, HttpServletRequest request, Message.Builder builder)
      throws InvalidRequestException {
    try {
      Gson gson = new Gson();
      Descriptors.Descriptor descriptor = builder.getDescriptorForType();

      if (!descriptor.getFields().isEmpty()) {
        bodyParser.merge(gson.toJson(request.getParameterMap()), builder);
        bodyParser.merge(
            gson.toJson(request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE)),
            builder);

        if (!StringUtils.isEmpty(rule.getBody())) {
          bodyParser.merge(IOUtils.toString(request.getReader()), builder);
        }
      }
    } catch (IOException e) {
      throw new InvalidRequestException(e, request);
    }
  }
}
