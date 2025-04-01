package wang.zhanwei.config;

import net.devh.boot.grpc.server.security.authentication.BearerAuthenticationReader;
import net.devh.boot.grpc.server.security.authentication.GrpcAuthenticationReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

@Configuration
public class GrpcConfig {
  @Bean
  public GrpcAuthenticationReader buildGrpcAuthenticationReader() {
    return new BearerAuthenticationReader(
        token -> { return new UsernamePasswordAuthenticationToken("test", token); });
  }
}
