package de.elite12.musikbot.server.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import springfox.bean.validators.configuration.BeanValidatorPluginsConfiguration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.*;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger.web.SecurityConfiguration;
import springfox.documentation.swagger.web.SecurityConfigurationBuilder;
import springfox.documentation.swagger.web.UiConfiguration;
import springfox.documentation.swagger.web.UiConfigurationBuilder;

import java.util.Collections;
import java.util.Set;

@Configuration
@Import(BeanValidatorPluginsConfiguration.class)
public class SwaggerConfig {
    @Autowired
    private ServiceProperties serviceProperties;

    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors
                        .basePackage("de.elite12.musikbot.server.api")
                )
                .build()
                .apiInfo(apiEndPointsInfo())
                .protocols(Set.of("https"))
                .produces(Set.of("application/json", "application/xml"))
                .useDefaultResponseMessages(true)
                .securitySchemes(Collections.singletonList(
                        new OAuth("Elite12 Identity",
                                Collections.emptyList(),
                                Collections.singletonList(new ImplicitGrant(
                                        new LoginEndpoint("https://id.elite12.de/auth/realms/elite12/protocol/openid-connect/auth"),
                                        "tokenName"
                                ))
                        )
                ))
                .securityContexts(Collections.singletonList(
                        SecurityContext.builder()
                                .securityReferences(Collections.singletonList(new SecurityReference("Elite12 Identity", new AuthorizationScope[0])))
                                .operationSelector(operationContext -> true)
                                .build()
                ));
    }

    private ApiInfo apiEndPointsInfo() {
        return new ApiInfoBuilder().title("Musikbot REST API")
                .description("REST API to interact with the Musikbot Service")
                .contact(new Contact("Sven Kirschbaum", "https://www.kirschbaum.me", "sven@kirschbaum.me"))
                .version(serviceProperties.getVersion())
                .build();
    }

    @Bean
    SecurityConfiguration security() {
        return SecurityConfigurationBuilder.builder()
                .clientId("musikbot-frontend")
                .build();
    }

    @Bean
    UiConfiguration uiConfig() {
        return UiConfigurationBuilder.builder()
                .deepLinking(true)
                .build();
    }
}
