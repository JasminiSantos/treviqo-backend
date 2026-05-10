package br.pucpr.authserver.treviqo.controller

import br.pucpr.authserver.treviqo.dto.DestinationCreateRequest
import br.pucpr.authserver.treviqo.dto.DestinationResponse
import br.pucpr.authserver.treviqo.dto.DestinationUpdateRequest
import br.pucpr.authserver.treviqo.service.DestinationService
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
@RequestMapping("/api/trips/{tripId}/destinations")
class TripDestinationController(
	private val destinationService: DestinationService,
) {

	@GetMapping
	fun list(@PathVariable tripId: Long): List<DestinationResponse> =
		destinationService.listByTrip(tripId)

	@GetMapping("/{destinationId}")
	fun getById(
		@PathVariable tripId: Long,
		@PathVariable destinationId: Long,
	): DestinationResponse = destinationService.getById(tripId, destinationId)

	@PostMapping
	@SecurityRequirement(name = "basicAuth")
	fun create(
		@PathVariable tripId: Long,
		@Valid @RequestBody req: DestinationCreateRequest,
	): ResponseEntity<DestinationResponse> =
		ResponseEntity.status(HttpStatus.CREATED).body(destinationService.create(tripId, req))

	@PutMapping("/{destinationId}")
	@SecurityRequirement(name = "basicAuth")
	fun update(
		@PathVariable tripId: Long,
		@PathVariable destinationId: Long,
		@Valid @RequestBody req: DestinationUpdateRequest,
	): DestinationResponse = destinationService.update(tripId, destinationId, req)

	@DeleteMapping("/{destinationId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@SecurityRequirement(name = "basicAuth")
	fun delete(
		@PathVariable tripId: Long,
		@PathVariable destinationId: Long,
	) {
		destinationService.delete(tripId, destinationId)
	}
}
