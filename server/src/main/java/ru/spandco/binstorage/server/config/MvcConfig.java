package ru.spandco.binstorage.server.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import org.springdoc.core.SwaggerUiConfigProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.util.unit.DataSize;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.servlet.MultipartConfigElement;

@Configuration
@ComponentScan(basePackages = "ru.spandco.logic.storage.config")
@ComponentScan(basePackages = "ru.spandco.logic.storage.service")
@ComponentScan(basePackages = "ru.spandco.binstorage.server.hub")
@EnableScheduling
@ConditionalOnProperty(name = "scheduler.enabled", matchIfMissing = true)
public class MvcConfig implements WebMvcConfigurer {

    @Bean
    WebServerFactoryCustomizer<ConfigurableServletWebServerFactory> enableDefaultServlet() {
        return (factory) -> factory.setRegisterDefaultServlet(true);
    }

    @OpenAPIDefinition(
            info = @Info(
                    title = "Loyalty System Api",
                    description = "Loyalty System", version = "1.0.0",
                    contact = @Contact(
                            name = "Struchkov Mark",
                            email = "mark@struchkov.dev",
                            url = "https://mark.struchkov.dev"
                    )
            )
    )
    public class OpenApiConfig {
    }

    @Bean
    public SwaggerUiConfigProperties swaggerUiConfig(SwaggerUiConfigProperties config) {
        config.setValidatorUrl("none");
        return config;
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("bin");
    }

    @Override
    public void configureDefaultServletHandling(
            DefaultServletHandlerConfigurer configurer
    ) {
        configurer.enable();
    }

    @Bean
    public MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();
        factory.setMaxFileSize(DataSize.ofGigabytes(1));
        factory.setMaxRequestSize(DataSize.ofGigabytes(1));
        return factory.createMultipartConfig();
    }
}
