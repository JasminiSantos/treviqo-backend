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
							"Basic Auth **admin**/**admin** para criar, alterar ou excluir viagem, despesa, destino ou documento (POST/PUT/DELETE).",
						)
					},
				)
		)
		.components(
			Components().addSecuritySchemes(
				"basicAuth",
				SecurityScheme()
					.type(SecurityScheme.Type.HTTP)
					.scheme("basic"),
			),
		)
}
