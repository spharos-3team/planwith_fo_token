package com.planwith.planwith_fo_token.config;

import java.util.List;

import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
public class OpenApiConfig {

	private static final String BEARER_AUTH_SCHEME = "bearerAuth";

	@Value("${app.gateway.public-url:/}")
	private String gatewayPublicUrl;

	@Bean
	public OpenAPI planwith_fo_tokenOpenAPI(TokenMockMemberProperties mockMemberProperties) {
		String mockMemberUuid = mockMemberProperties.resolvedMemberUuid().toString();
		return new OpenAPI()
				.info(new Info()
						.title("PlanWith planwith-fo-token API")
						.description("""
								Call APIs through the API Gateway (:8000).
								Do not put Docker hostname or :8084 in OpenAPI servers.
								Swagger Try-it-out must use the browser origin (Gateway).

								테스트용 memberUuid: %s
								""".formatted(mockMemberUuid))
						.version("v1"))
				.components(new Components()
						.addSecuritySchemes(BEARER_AUTH_SCHEME, new SecurityScheme()
								.type(SecurityScheme.Type.HTTP)
								.scheme("bearer")
								.bearerFormat("JWT")))
				.addSecurityItem(new SecurityRequirement().addList(BEARER_AUTH_SCHEME))
				.servers(List.of(new Server()
						.url(gatewayPublicUrl)
						.description("API Gateway")));
	}

	@Bean
	public OperationCustomizer memberUuidExampleCustomizer(TokenMockMemberProperties mockMemberProperties) {
		String mockMemberUuid = mockMemberProperties.resolvedMemberUuid().toString();
		return (operation, handlerMethod) -> {
			if (operation.getParameters() == null) {
				return operation;
			}
			operation.getParameters().stream()
					.filter(parameter -> "memberUuid".equals(parameter.getName()))
					.forEach(parameter -> parameter.setExample(mockMemberUuid));
			return operation;
		};
	}
}
