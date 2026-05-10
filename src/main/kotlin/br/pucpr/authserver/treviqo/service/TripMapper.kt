package br.pucpr.authserver.treviqo.service

import br.pucpr.authserver.exception.BusinessRuleException
import br.pucpr.authserver.treviqo.domain.Destination
import br.pucpr.authserver.treviqo.domain.Expense
import br.pucpr.authserver.treviqo.domain.Trip
import br.pucpr.authserver.treviqo.domain.TripDocument
import br.pucpr.authserver.treviqo.dto.DestinationResponse
import br.pucpr.authserver.treviqo.dto.DocumentResponse
import br.pucpr.authserver.treviqo.dto.ExpenseResponse
import br.pucpr.authserver.treviqo.dto.TripResponse
import java.math.BigDecimal
import java.time.Instant

internal fun validateTripPeriod(start: Instant, end: Instant) {
	if (end.isBefore(start)) {
		throw BusinessRuleException("A data fim da viagem deve ser igual ou posterior à data de início.")
	}
}

internal fun Trip.validateExpenseInsideTrip(expenseDate: Instant) {
	if (expenseDate.isBefore(tripStart) || expenseDate.isAfter(tripEnd)) {
		throw BusinessRuleException(
			"A data da despesa deve estar entre o início e o fim da viagem ($tripStart — $tripEnd).",
		)
	}
}

private fun Trip.aggregate(): Pair<Int, BigDecimal> {
	val count = expenses.size
	val total = expenses.fold(BigDecimal.ZERO) { acc, e -> acc.add(e.amount) }
	return count to total
}

internal fun Trip.toResponse(includeNested: Boolean): TripResponse {
	val (count, total) = aggregate()
	return TripResponse(
		id = id!!,
		title = title,
		city = city,
		country = country,
		coverUrl = coverUrl,
		start = tripStart,
		end = tripEnd,
		budget = budget,
		notes = notes,
		expenseCount = count,
		totalSpent = total,
		expenses = if (includeNested) expenses.sortedBy { it.expenseDate }.map { it.toResponse() } else emptyList(),
		destinations = if (includeNested) {
			destinations.sortedBy { it.stayStart }.map { it.toResponse() }
		} else {
			emptyList()
		},
		documents = if (includeNested) {
			documents.sortedBy { it.name.lowercase() }.map { it.toResponse() }
		} else {
			emptyList()
		},
	)
}

internal fun Expense.toResponse() = ExpenseResponse(
	id = id!!,
	description = description,
	amount = amount,
	category = category,
	date = expenseDate,
)

internal fun Destination.toResponse() = DestinationResponse(
	id = id!!,
	city = city,
	stayStart = stayStart,
	stayEnd = stayEnd,
	notes = notes,
)

internal fun TripDocument.toResponse() = DocumentResponse(
	id = id!!,
	name = name,
	type = type,
	uri = uri,
	notes = notes,
)
