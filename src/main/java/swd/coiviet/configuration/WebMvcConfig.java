package swd.coiviet.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private LocalTimeConverter localTimeConverter;

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(localTimeConverter);
    }
}
