package br.pucpr.authserver.treviqo.controller

import br.pucpr.authserver.treviqo.dto.AddExpenseRequest
import br.pucpr.authserver.treviqo.dto.ExpenseResponse
import br.pucpr.authserver.treviqo.dto.ExpenseUpdateRequest
import br.pucpr.authserver.treviqo.dto.TripResponse
import br.pucpr.authserver.treviqo.service.ExpenseService
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
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/trips/{tripId}/expenses")
class TripExpenseController(
	private val expenseService: ExpenseService,
) {

	@GetMapping
	fun list(@PathVariable tripId: Long): List<ExpenseResponse> =
		expenseService.listByTrip(tripId)

	@GetMapping("/{expenseId}")
	fun getById(
		@PathVariable tripId: Long,
		@PathVariable expenseId: Long,
	): ExpenseResponse = expenseService.getById(tripId, expenseId)

	@PostMapping
	@SecurityRequirement(name = "bearerJwt")
	fun create(
		@PathVariable tripId: Long,
		@Valid @RequestBody req: AddExpenseRequest,
	): ResponseEntity<TripResponse> =
		ResponseEntity.status(HttpStatus.CREATED).body(expenseService.addExpense(tripId, req))

	@PutMapping("/{expenseId}")
	@SecurityRequirement(name = "bearerJwt")
	fun update(
		@PathVariable tripId: Long,
		@PathVariable expenseId: Long,
		@Valid @RequestBody req: ExpenseUpdateRequest,
	): TripResponse = expenseService.updateExpense(tripId, expenseId, req)

	@DeleteMapping("/{expenseId}")
	@SecurityRequirement(name = "bearerJwt")
	fun delete(
		@PathVariable tripId: Long,
		@PathVariable expenseId: Long,
	): TripResponse = expenseService.removeExpense(tripId, expenseId)
}
