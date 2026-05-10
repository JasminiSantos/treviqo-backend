package br.pucpr.authserver.treviqo.controller

import br.pucpr.authserver.treviqo.dto.DocumentCreateRequest
import br.pucpr.authserver.treviqo.dto.DocumentResponse
import br.pucpr.authserver.treviqo.dto.DocumentUpdateRequest
import br.pucpr.authserver.treviqo.service.TripDocumentService
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/trips/{tripId}/documents")
class TripDocumentController(
	private val tripDocumentService: TripDocumentService,
) {

	@GetMapping
	fun list(@PathVariable tripId: Long): List<DocumentResponse> =
		tripDocumentService.listByTrip(tripId)

	@GetMapping("/{documentId}")
	fun getById(
		@PathVariable tripId: Long,
		@PathVariable documentId: Long,
	): DocumentResponse = tripDocumentService.getById(tripId, documentId)

	@PostMapping
	@SecurityRequirement(name = "basicAuth")
	fun create(
		@PathVariable tripId: Long,
		@Valid @RequestBody req: DocumentCreateRequest,
	): ResponseEntity<DocumentResponse> =
		ResponseEntity.status(HttpStatus.CREATED).body(tripDocumentService.create(tripId, req))

	@PutMapping("/{documentId}")
	@SecurityRequirement(name = "basicAuth")
	fun update(
		@PathVariable tripId: Long,
		@PathVariable documentId: Long,
		@Valid @RequestBody req: DocumentUpdateRequest,
	): DocumentResponse = tripDocumentService.update(tripId, documentId, req)

	@DeleteMapping("/{documentId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@SecurityRequirement(name = "basicAuth")
	fun delete(
		@PathVariable tripId: Long,
		@PathVariable documentId: Long,
	) {
		tripDocumentService.delete(tripId, documentId)
	}
}
