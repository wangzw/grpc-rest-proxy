package wang.zhanwei.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import wang.zhanwei.rest.proxy.path.GoogleApiPathMatcher;

@Configuration
public class RequestMappingConfig implements WebMvcConfigurer {

  @Override
  public void configurePathMatch(PathMatchConfigurer configurer) {
    configurer.setPathMatcher(new GoogleApiPathMatcher());
  }
}
