package br.pucpr.authserver.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityScheme
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {

	@Bean
	fun openAPI(): OpenAPI = OpenAPI()
		.info(
			Info()
				.title("Treviqo — viagens e despesas")
				.description(
					buildString {
						append(
							"CRUD de viagens (`Trip`) com despesas (`Expense`), destinos (`Destination`) e documentos (`TripDocument`).\n\n",
						)
						append(
							"Tipos de documento (`DocumentType`): Ticket, Hotel, Reservation, Insurance, Visa, Other.\n\n",
						)
						append(
							"**Login:** `POST /api/auth/login` com username e password, com resposta que traz `accessToken`.\n\n",
						)
						append(
							"**API protegida:** envie `Authorization: Bearer token` nas operações POST/PUT/DELETE de viagens e recursos aninhados.",
						)
					},
				)
		)
		.components(
			Components().addSecuritySchemes(
				"bearerJwt",
				SecurityScheme()
					.type(SecurityScheme.Type.HTTP)
					.scheme("bearer")
					.bearerFormat("JWT"),
			),
		)
}
