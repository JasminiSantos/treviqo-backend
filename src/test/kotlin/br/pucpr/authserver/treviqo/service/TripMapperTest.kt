package br.pucpr.authserver.treviqo.service

import br.pucpr.authserver.exception.BusinessRuleException
import br.pucpr.authserver.treviqo.domain.Expense
import br.pucpr.authserver.treviqo.domain.Trip
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import java.math.BigDecimal
import java.time.Instant

class TripMapperTest {

	private val t0 = Instant.parse("2026-06-01T08:00:00Z")
	private val t1 = Instant.parse("2026-06-10T18:00:00Z")

	@Test
	fun `validateTripPeriod falha quando fim antes do inicio`() {
		val ex = assertFailsWith<BusinessRuleException> {
			validateTripPeriod(t1, t0)
		}
		assertTrue(ex.message!!.contains("fim", ignoreCase = true))
	}

	@Test
	fun `validateTripPeriod aceita mesmo instante`() {
		validateTripPeriod(t0, t0)
	}

	@Test
	fun `validateExpenseInsideTrip falha fora do intervalo da viagem`() {
		val trip = Trip(
			title = "X",
			city = "Y",
			country = "Z",
			tripStart = t0,
			tripEnd = t1,
			budget = BigDecimal("100"),
		)
		assertFailsWith<BusinessRuleException> {
			trip.validateExpenseInsideTrip(t0.minusSeconds(1))
		}
		assertFailsWith<BusinessRuleException> {
			trip.validateExpenseInsideTrip(t1.plusSeconds(1))
		}
	}

	@Test
	fun `validateExpenseInsideTrip aceita datas nos limites`() {
		val trip = Trip(
			title = "X",
			city = "Y",
			country = "Z",
			tripStart = t0,
			tripEnd = t1,
			budget = BigDecimal("100"),
		)
		trip.validateExpenseInsideTrip(t0)
		trip.validateExpenseInsideTrip(t1)
	}

	@Test
	fun `toResponse sem linhas agrega totais com lista vazia e com linhas inclui despesas`() {
		val trip = Trip(
			id = 7L,
			title = "Férias",
			city = "Lisboa",
			country = "Portugal",
			coverUrl = null,
			tripStart = t0,
			tripEnd = t1,
			budget = BigDecimal("3000"),
			notes = null,
		)
		val e1 = Expense(
			id = 10L,
			trip = trip,
			description = "Hotel",
			amount = BigDecimal("100.00"),
			category = "Hospedagem",
			expenseDate = t0,
		)
		val e2 = Expense(
			id = 11L,
			trip = trip,
			description = "Metro",
			amount = BigDecimal("25.50"),
			category = "Transporte",
			expenseDate = t0.plusSeconds(3600),
		)
		trip.expenses.add(e1)
		trip.expenses.add(e2)

		val listShape = trip.toResponse(includeNested = false)
		assertEquals(7L, listShape.id)
		assertEquals(2, listShape.expenseCount)
		assertEquals(BigDecimal("125.50"), listShape.totalSpent)
		assertEquals(0, listShape.expenses.size)

		val detailShape = trip.toResponse(includeNested = true)
		assertEquals(2, detailShape.expenses.size)
		assertEquals("Hotel", detailShape.expenses.first { it.id == 10L }.description)
	}
}
