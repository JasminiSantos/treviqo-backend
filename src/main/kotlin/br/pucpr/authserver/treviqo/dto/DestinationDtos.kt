package br.pucpr.authserver.treviqo.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.time.Instant

data class DestinationResponse(
	val id: Long,
	val city: String,
	val stayStart: Instant,
	val stayEnd: Instant,
	val notes: String?,
)

data class DestinationCreateRequest(
	@field:NotBlank @field:Size(max = 120)
	val city: String,
	@field:NotNull
	val stayStart: Instant,
	@field:NotNull
	val stayEnd: Instant,
	@field:Size(max = 4000)
	val notes: String? = null,
)

data class DestinationUpdateRequest(
	@field:NotBlank @field:Size(max = 120)
	val city: String,
	@field:NotNull
	val stayStart: Instant,
	@field:NotNull
	val stayEnd: Instant,
	@field:Size(max = 4000)
	val notes: String? = null,
)
