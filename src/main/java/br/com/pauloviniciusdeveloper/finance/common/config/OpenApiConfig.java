package br.com.pauloviniciusdeveloper.finance.common.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Finance API")
                .description("""
                    API REST para gerenciamento de finanças pessoais.

                    **Autenticação:** todas as rotas protegidas exigem um Bearer token JWT obtido em `POST /api/v1/auth/login`.

                    **Controle de acesso (RBAC):**
                    - `USER` — acesso padrão às próprias transações, contas e categorias.
                    - `ADMIN` — acesso total, incluindo gerenciamento de todos os usuários em `/api/v1/admin/users`.

                    O usuário admin padrão é criado automaticamente na inicialização (`admin@admin.com`).
                    """)
                .version("v1.0.0"))
            .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
            .components(new Components()
                .addSecuritySchemes("bearerAuth",
                    new SecurityScheme()
                        .name("bearerAuth")
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("Informe o access token obtido em POST /api/v1/auth/login")));
    }
}
