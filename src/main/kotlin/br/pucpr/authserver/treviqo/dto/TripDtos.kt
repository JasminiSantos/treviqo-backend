package br.pucpr.authserver.treviqo.dto

import com.fasterxml.jackson.annotation.JsonInclude
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.math.BigDecimal
import java.time.Instant

data class TripCreateRequest(
	@field:NotBlank @field:Size(max = 200)
	val title: String,
	@field:NotBlank @field:Size(max = 120)
	val city: String,
	@field:NotBlank @field:Size(max = 120)
	val country: String,
	@field:Size(max = 2000)
	val coverUrl: String? = null,
	@field:NotNull
	val start: Instant,
	@field:NotNull
	val end: Instant,
	@field:NotNull @field:DecimalMin("0.0")
	val budget: BigDecimal,
	@field:Size(max = 4000)
	val notes: String? = null,
)

data class TripUpdateRequest(
	@field:NotBlank @field:Size(max = 200)
	val title: String,
	@field:NotBlank @field:Size(max = 120)
	val city: String,
	@field:NotBlank @field:Size(max = 120)
	val country: String,
	@field:Size(max = 2000)
	val coverUrl: String? = null,
	@field:NotNull
	val start: Instant,
	@field:NotNull
	val end: Instant,
	@field:NotNull @field:DecimalMin("0.0")
	val budget: BigDecimal,
	@field:Size(max = 4000)
	val notes: String? = null,
)

/** Resposta única para lista e detalhe: em listagens `expenses` fica vazio e é omitido no JSON. */
data class TripResponse(
	val id: Long,
	val title: String,
	val city: String,
	val country: String,
	val coverUrl: String?,
	val start: Instant,
	val end: Instant,
	val budget: BigDecimal,
	val notes: String?,
	val expenseCount: Int,
	val totalSpent: BigDecimal,
	@field:JsonInclude(JsonInclude.Include.NON_EMPTY)
	val expenses: List<ExpenseResponse> = emptyList(),
	@field:JsonInclude(JsonInclude.Include.NON_EMPTY)
	val destinations: List<DestinationResponse> = emptyList(),
	@field:JsonInclude(JsonInclude.Include.NON_EMPTY)
	val documents: List<DocumentResponse> = emptyList(),
)

data class ExpenseResponse(
	val id: Long,
	val description: String,
	val amount: BigDecimal,
	val category: String,
	val date: Instant,
)

/** Mesmos campos para criar ou atualizar uma despesa. */
typealias ExpenseUpdateRequest = AddExpenseRequest

data class AddExpenseRequest(
	@field:NotBlank @field:Size(max = 500)
	val description: String,
	@field:NotNull @field:DecimalMin("0.01")
	val amount: BigDecimal,
	@field:NotBlank @field:Size(max = 80)
	val category: String,
	@field:NotNull
	val date: Instant,
)
