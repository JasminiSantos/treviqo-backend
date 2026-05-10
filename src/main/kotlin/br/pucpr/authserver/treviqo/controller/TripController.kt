package br.pucpr.authserver.treviqo.controller

import br.pucpr.authserver.treviqo.dto.TripCreateRequest
import br.pucpr.authserver.treviqo.dto.TripResponse
import br.pucpr.authserver.treviqo.dto.TripUpdateRequest
import br.pucpr.authserver.treviqo.service.TripService
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import jakarta.validation.Valid
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal
import java.time.Instant

@RestController
@RequestMapping("/api/trips")
class TripController(
	private val tripService: TripService,
) {

	/**
	 * Lista viagens com filtros e ordenação via query params:
	 * `titleContains`, `cityContains`, `countryContains`, `startAfter`, `startBefore`, `minBudget`, `maxBudget`,
	 * `sortBy` (tripStart|city|country|budget|tripEnd), `sortDirection` (asc|desc).
	 */
	@GetMapping
	fun search(
		@RequestParam(required = false) titleContains: String?,
		@RequestParam(required = false) cityContains: String?,
		@RequestParam(required = false) countryContains: String?,
		@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) startAfter: Instant?,
		@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) startBefore: Instant?,
		@RequestParam(required = false) minBudget: BigDecimal?,
		@RequestParam(required = false) maxBudget: BigDecimal?,
		@RequestParam(required = false, defaultValue = "tripStart") sortBy: String,
		@RequestParam(required = false, defaultValue = "desc") sortDirection: String,
	): List<TripResponse> =
		tripService.search(
			titleContains,
			cityContains,
			countryContains,
			startAfter,
			startBefore,
			minBudget,
			maxBudget,
			sortBy,
			sortDirection,
		)

	@GetMapping("/{id}")
	fun getById(@PathVariable id: Long): TripResponse = tripService.getById(id)

	@PostMapping
	@SecurityRequirement(name = "basicAuth")
	fun create(@Valid @RequestBody req: TripCreateRequest): ResponseEntity<TripResponse> {
		val created = tripService.create(req)
		return ResponseEntity.status(HttpStatus.CREATED).body(created)
	}

	@PutMapping("/{id}")
	@SecurityRequirement(name = "basicAuth")
	fun update(
		@PathVariable id: Long,
		@Valid @RequestBody req: TripUpdateRequest,
	): TripResponse = tripService.update(id, req)

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@SecurityRequirement(name = "basicAuth")
	fun delete(@PathVariable id: Long) {
		tripService.delete(id)
	}
}
