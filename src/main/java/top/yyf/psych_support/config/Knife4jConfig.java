package top.yyf.psych_support.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Knife4j API 文档配置
 *
 * @author mqxu
 */
@Configuration
public class Knife4jConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("PsychSupport 接口文档")
                        .version("1.0.0")
                        .summary("PsychSupport 接口文档")
                        .description("Psych Support 演示项目")
                        .contact(new Contact()
                                .name("yyf")
                                .email("yyf@gmail.com")));
    }

    @Bean
    public GroupedOpenApi myApi() {
        String[] paths = {"/**"};
        String[] packagedToMatch = {"top.yyf.psych_support.controller"};
        return GroupedOpenApi.builder()
                .group("1")
                .displayName("PsychSupport API")
                .pathsToMatch(paths)
                .packagesToScan(packagedToMatch).build();
    }
}
