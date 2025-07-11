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
		// 업로드된 이미지, 썸네일 등 전반적인 파일 처리
		    registry.addResourceHandler("/upload/**")
		            .addResourceLocations("file:///C:/upload/");

		// 다시보기 영상(mp4 등) 전용 경로 처리
		    registry.addResourceHandler("/video/upload/**")
		            .addResourceLocations("file:///C:/videos/");
	  }
}