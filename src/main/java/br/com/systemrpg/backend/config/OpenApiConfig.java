package br.com.systemrpg.backend.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;

/**
 * Configuração do OpenAPI/Swagger para documentação da API.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("SystemRPG Backend API")
                        .description("API para autenticação e autorização de usuários")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("SystemRPG")
                                .email("support@systemrpg.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .addServersItem(new Server()
                        .url("http://localhost:8080/backend/api/v1")
                        .description("Servidor de desenvolvimento"))
                .addServersItem(new Server()
                        .url("https://api.systemrpg.com/backend/api/v1")
                        .description("Servidor de produção"));
    }


}
