package ir.niopdc.gslatlon.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class MvcConfig implements WebMvcConfigurer {

//        @Override
//        public void extendMessageConverters(final List<HttpMessageConverter<?>> converters) {
//            converters.removeIf(StringHttpMessageConverter.class::isInstance);
//        }

}
