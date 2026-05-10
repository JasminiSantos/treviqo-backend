package br.pucpr.authserver.treviqo.dto

import br.pucpr.authserver.treviqo.domain.DocumentType
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

data class DocumentResponse(
	val id: Long,
	val name: String,
	val type: DocumentType,
	val uri: String?,
	val notes: String?,
)

data class DocumentCreateRequest(
	@field:NotBlank @field:Size(max = 200)
	val name: String,
	@field:NotNull
	val type: DocumentType,
	@field:Size(max = 2000)
	val uri: String? = null,
	@field:Size(max = 4000)
	val notes: String? = null,
)

data class DocumentUpdateRequest(
	@field:NotBlank @field:Size(max = 200)
	val name: String,
	@field:NotNull
	val type: DocumentType,
	@field:Size(max = 2000)
	val uri: String? = null,
	@field:Size(max = 4000)
	val notes: String? = null,
)
