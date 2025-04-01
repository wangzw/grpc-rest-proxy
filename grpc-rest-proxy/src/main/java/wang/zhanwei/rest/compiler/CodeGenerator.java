package wang.zhanwei.rest.compiler;

import static com.google.api.HttpRule.PatternCase.PATTERN_NOT_SET;

import com.google.api.AnnotationsProto;
import com.google.api.HttpRule;
import com.google.common.base.CaseFormat;
import com.google.protobuf.Descriptors.FileDescriptor;
import com.google.protobuf.Descriptors.MethodDescriptor;
import com.google.protobuf.Descriptors.ServiceDescriptor;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

public class CodeGenerator {
  public boolean generate(FileDescriptor file, GeneratorContext context) throws IOException {
    for (ServiceDescriptor service : file.getServices()) {
      if (!generateService(file, service, context)) {
        return false;
      }
    }

    return true;
  }

  String generateServiceFilePath(String packageName, String serviceClassName) {
    String path = packageName + "/" + serviceClassName;
    path = path.replaceAll("\\.", "/");
    path += ".java";
    return path;
  }

  boolean generateService(FileDescriptor file, ServiceDescriptor service, GeneratorContext context)
      throws IOException {
    String serviceClassName = service.getName() + "RestProxy";
    String servicePath =
        generateServiceFilePath(file.getOptions().getJavaPackage(), serviceClassName);

    context.addNewFile(servicePath);

    if (!generateFileHeader(file, service, context)) {
      return false;
    }

    if (!generateServiceHeader(file, service, context)) {
      return false;
    }

    if (!generateServiceStaticBlock(file, service, context)) {
      return false;
    }

    for (MethodDescriptor method : service.getMethods()) {
      if (!generateRpcMethod(file, service, method, context)) {
        return false;
      }
    }

    if (!generateServiceFooter(file, service, context)) {
      return false;
    }

    return true;
  }

  boolean generateRpcMethod(FileDescriptor file, ServiceDescriptor service, MethodDescriptor method,
      GeneratorContext context) throws IOException {
    HttpRule httpRule = method.getOptions().getExtension(AnnotationsProto.http);

    if (httpRule.getPatternCase() == PATTERN_NOT_SET) {
      return true;
    }

    if (!generateRpcEndpoint(file, service, method, httpRule, "", context)) {
      return false;
    }

    int index = 0;

    for (HttpRule rule : httpRule.getAdditionalBindingsList()) {
      if (!generateRpcEndpoint(file, service, method, rule, Integer.toString(index), context)) {
        return false;
      }

      ++index;
    }

    return true;
  }

  String resolveEndpoint(HttpRule httpRule) {
    switch (httpRule.getPatternCase()) {
      case GET:
        return httpRule.getGet();
      case PUT:
        return httpRule.getPut();
      case POST:
        return httpRule.getPost();
      case DELETE:
        return httpRule.getDelete();
      case PATCH:
        return httpRule.getPatch();
      default:
        return "UNSUPPORTED";
    }
  }

  String resolveRequestMethod(HttpRule httpRule) {
    switch (httpRule.getPatternCase()) {
      case GET:
        return "GET";
      case PUT:
        return "PUT";
      case POST:
        return "POST";
      case DELETE:
        return "DELETE";
      case PATCH:
        return "PATCH";
      default:
        return "UNSUPPORTED";
    }
  }

  boolean generateFileHeader(
      FileDescriptor file, ServiceDescriptor service, GeneratorContext context) throws IOException {
    String packageName = file.getOptions().getJavaPackage();

    // clang-format off
    String importString =
    "package " + packageName + ";\n" +
    "\n" +
    "import com.google.api.HttpRule;\n" +
    "import com.google.protobuf.InvalidProtocolBufferException;\n" +
    "import com.google.protobuf.Message;\n" +
    "import io.grpc.Status;\n" +
    "import io.grpc.StatusRuntimeException;\n" +
    "import java.util.Base64;\n" +
    "import java.util.Locale;\n" +
    "import jakarta.servlet.http.HttpServletRequest;\n" +
    "import org.apache.commons.io.IOUtils;\n" +
    "import org.springframework.http.ResponseEntity;\n" +
    "import org.springframework.web.bind.annotation.RequestMapping;\n" +
    "import org.springframework.web.bind.annotation.RequestMethod;\n" +
    "import wang.zhanwei.rest.proxy.error.DefaultErrorHandler;\n" +
    "import wang.zhanwei.rest.proxy.error.ErrorHandler;\n" +
    "import wang.zhanwei.rest.proxy.error.InvalidRequestException;\n" +
    "import wang.zhanwei.rest.proxy.translator.AuthenticationTranslator;\n"+
    "import wang.zhanwei.rest.proxy.translator.BearerAuthenticationTranslator;\n" +
    "import wang.zhanwei.rest.proxy.translator.RequestTranslator;\n" +
    "import wang.zhanwei.rest.proxy.translator.ResponseTranslator;\n" +
    "import wang.zhanwei.rest.proxy.translator.RestRequestTranslator;\n" +
    "import wang.zhanwei.rest.proxy.translator.RestResponseTranslator;\n\n";
    // clang-format on

    context.write(importString);

    return true;
  }

  String serializeHttpRule(HttpRule rule) throws IOException {
    ByteArrayOutputStream stream = new ByteArrayOutputStream();
    rule.writeTo(stream);
    return Base64.getEncoder().encodeToString(stream.toByteArray());
  }

  boolean generateServiceStaticBlock(
      FileDescriptor file, ServiceDescriptor service, GeneratorContext context) throws IOException {
    boolean noEndpointFound = true;

    for (MethodDescriptor method : service.getMethods()) {
      String nameSuffix = method.getName();
      HttpRule rule = method.getOptions().getExtension(AnnotationsProto.http);

      if (rule.getPatternCase() == PATTERN_NOT_SET) {
        continue;
      }

      noEndpointFound = false;

      // clang-format off
      String block = "  static HttpRule rule" + nameSuffix + " = null;\n";
      // clang-format on

      context.write(block);
    }

    if (noEndpointFound) {
      return true;
    }

    context.write("\n");

    String staticBlockHeader = "  static {\n    try {\n";
    context.write(staticBlockHeader);

    for (MethodDescriptor method : service.getMethods()) {
      HttpRule rule = method.getOptions().getExtension(AnnotationsProto.http);

      if (rule.getPatternCase() == PATTERN_NOT_SET) {
        continue;
      }

      String ruleStr = serializeHttpRule(rule);
      String nameSuffix = method.getName();

      // clang-format off
      String block =
        "      final String ruleStr" + nameSuffix + " = \"" + ruleStr + "\";\n" +
        "      rule" + nameSuffix + " = HttpRule.parseFrom(Base64.getDecoder().decode(ruleStr" + nameSuffix + "));\n";
      // clang-format on

      context.write(block);
    }

    // clang-format off
    String staticBlockFooter =
      "    } catch (InvalidProtocolBufferException ignore) {\n" +
      "    }\n" +
      "  }\n\n";
    // clang-format on

    context.write(staticBlockFooter);
    return true;
  }

  boolean generateServiceHeader(
      FileDescriptor file, ServiceDescriptor service, GeneratorContext context) throws IOException {
    String serviceName = service.getName() + "RestProxy";
    String grpcStubName = service.getName() + "Grpc"
        + "." + service.getName() + "BlockingStub";

    // clang-format off
    String header = "public class "+ serviceName +" {\n" +
    "  protected "+ grpcStubName + " client;\n" +
    "  protected RequestTranslator requestTranslator = new RestRequestTranslator();\n" +
    "  protected ResponseTranslator responseTranslator = new RestResponseTranslator();\n" +
    "  protected AuthenticationTranslator authenticationTranslator = new BearerAuthenticationTranslator();\n" +
    "  protected ErrorHandler handler = new DefaultErrorHandler();\n" +
    "\n" +
    "  public void setClient(" + grpcStubName + " client) {\n" +
    "    this.client = client;\n" +
    "  }\n" +
    "\n" +
    "  public void setRequestTranslator(RequestTranslator requestTranslator) {\n" +
    "    this.requestTranslator = requestTranslator;\n" +
    "  }\n" +
    "\n" +
    "  public void setResponseTranslator(ResponseTranslator responseTranslator) {\n" +
    "    this.responseTranslator = responseTranslator;\n" +
    "  }\n" +
    "\n" +
    "  public void setHandler(ErrorHandler handler) {\n" +
    "    this.handler = handler;\n" +
    "  }\n\n";
    // clang-format on

    context.write(header);
    return true;
  }

  boolean generateRpcEndpoint(FileDescriptor file, ServiceDescriptor service,
      MethodDescriptor method, HttpRule httpRule, String suffix, GeneratorContext context)
      throws IOException {
    String packageName = file.getOptions().getJavaPackage();
    String httpEndpoint = resolveEndpoint(httpRule);
    String httpRequestMethod = resolveRequestMethod(httpRule);
    String rpcMethodName = upperCamelToLowerCamel(method.getName());
    String rpcMethodRequest = packageName + "." + method.getInputType().getName();
    String ruleNameSuffix = method.getName();

    // clang-format off
    String methodCall = 
    "  @RequestMapping(path = \"" + httpEndpoint + "\", method = RequestMethod." + httpRequestMethod + ")\n" +
    "  protected ResponseEntity<String> " + rpcMethodName + suffix + "(HttpServletRequest request, Locale locale) {\n" +
    "    try {\n" +
    "      " + rpcMethodRequest + ".Builder builder = " + rpcMethodRequest + ".newBuilder();\n" +
    "      requestTranslator.translate(rule" + ruleNameSuffix + ", request, builder);\n" +
    "      Message responseMessage = \n" +
    "          client.withCallCredentials(authenticationTranslator.translate(request))\n" +
    "              ." + rpcMethodName + "(builder.build());\n" +
    "      return responseTranslator.translate(rule" + ruleNameSuffix + ", responseMessage);\n" +
    "    } catch (InvalidRequestException e) {\n" +
    "      return handler.handle(e, locale);\n" +
    "    } catch (StatusRuntimeException e) {\n" +
    "      return handler.handle(e.getStatus(), Status.trailersFromThrowable(e), locale);\n" +
    "    } catch (Exception e) {\n" +
    "      return handler.handle(e, Status.trailersFromThrowable(e), locale);\n" +
    "    }\n" +
    "  }\n\n";
    // clang-format on

    context.write(methodCall);
    return true;
  }

  boolean generateServiceFooter(
      FileDescriptor file, ServiceDescriptor service, GeneratorContext context) throws IOException {
    context.write("}");
    return true;
  }

  public static String upperCamelToLowerCamel(String name) {
    return CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, name);
  }
}
