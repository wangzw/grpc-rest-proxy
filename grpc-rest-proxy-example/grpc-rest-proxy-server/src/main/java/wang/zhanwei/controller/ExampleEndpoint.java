package wang.zhanwei.controller;

import javax.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Locale;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import wang.zhanwei.example.proto.ExampleServiceGrpc;
import wang.zhanwei.example.proto.ExampleServiceRestProxy;

@RestController
public class ExampleEndpoint extends ExampleServiceRestProxy {
  @GrpcClient("example") ExampleServiceGrpc.ExampleServiceBlockingStub client;

  @PostConstruct
  public void init() {
    this.setClient(client);
  }

  @Override
  protected ResponseEntity<String> emptyMessage(HttpServletRequest request, Locale locale) {
    return super.emptyMessage(request, locale);
  }
}
