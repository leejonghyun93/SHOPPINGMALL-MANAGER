package org.kosa.shoppingmaillmanager.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    /* 
    업로드된 파일을 정적 자원으로 매핑 (예: /upload/image.png → C:/upload/image.png)
     */
 @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
      registry.addResourceHandler("/upload/**")
                .addResourceLocations("file:///C:/upload/");
    }
}