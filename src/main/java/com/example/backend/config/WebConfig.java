package com.example.backend.config;

import java.io.File;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    String uploadDir =
        System.getProperty("user.home") + File.separator + "remate_uploads" + File.separator;
    registry.addResourceHandler("/images/**").addResourceLocations("file:///" + uploadDir);
  }
}
