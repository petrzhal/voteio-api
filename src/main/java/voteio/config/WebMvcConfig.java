package voteio.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("api/login").allowedOrigins("http://localhost:3000");
        registry.addMapping("api/register").allowedOrigins("http://localhost:3000");
        registry.addMapping("api/refresh").allowedOrigins("http://localhost:3000");
    }
}
