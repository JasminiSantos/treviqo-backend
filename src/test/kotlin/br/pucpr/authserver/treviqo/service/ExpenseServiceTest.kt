package br.pucpr.authserver.treviqo.service

import br.pucpr.authserver.exception.BusinessRuleException
import br.pucpr.authserver.exception.ResourceNotFoundException
import br.pucpr.authserver.treviqo.domain.Expense
import br.pucpr.authserver.treviqo.domain.Trip
import br.pucpr.authserver.treviqo.dto.AddExpenseRequest
import br.pucpr.authserver.treviqo.dto.ExpenseUpdateRequest
import br.pucpr.authserver.treviqo.repository.TripRepository
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.math.BigDecimal
import java.time.Instant
import java.util.Optional
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@ExtendWith(MockitoExtension::class)
class ExpenseServiceTest {

	@Mock
	private lateinit var tripRepository: TripRepository

	@InjectMocks
	private lateinit var expenseService: ExpenseService

	private val tripStart = Instant.parse("2026-08-01T10:00:00Z")
	private val tripEnd = Instant.parse("2026-08-07T10:00:00Z")

	@Test
	fun `addExpense lanca quando viagem nao existe`() {
		whenever(tripRepository.findById(1L)).thenReturn(Optional.empty())

		val req = AddExpenseRequest(
			description = "Almoço",
			amount = BigDecimal("35"),
			category = "Alimentação",
			date = tripStart,
		)

		assertFailsWith<ResourceNotFoundException> {
			expenseService.addExpense(1L, req)
		}
		verify(tripRepository, never()).save(any())
		verify(tripRepository, never()).saveAndFlush(any())
	}

	@Test
	fun `addExpense lanca quando data fora da viagem`() {
		val trip = Trip(
			id = 1L,
			title = "T",
			city = "C",
			country = "BR",
			tripStart = tripStart,
			tripEnd = tripEnd,
			budget = BigDecimal("500"),
		)
		whenever(tripRepository.findById(1L)).thenReturn(Optional.of(trip))

		val req = AddExpenseRequest(
			description = "X",
			amount = BigDecimal("10"),
			category = "Outros",
			date = tripEnd.plusSeconds(1),
		)

		assertFailsWith<BusinessRuleException> {
			expenseService.addExpense(1L, req)
		}
		verify(tripRepository, never()).save(any())
		verify(tripRepository, never()).saveAndFlush(any())
	}

	@Test
	fun `addExpense persiste despesa`() {
		val trip = Trip(
			id = 1L,
			title = "T",
			city = "C",
			country = "BR",
			tripStart = tripStart,
			tripEnd = tripEnd,
			budget = BigDecimal("500"),
		)
		whenever(tripRepository.findById(1L)).thenReturn(Optional.of(trip))
		val assignExpenseIds: (org.mockito.invocation.InvocationOnMock) -> Trip = { invocation ->
			val t = invocation.getArgument<Trip>(0)
			var seq = 70L
			for (e in t.expenses) {
				if (e.id == null) {
					e.id = seq++
				}
			}
			t
		}
		whenever(tripRepository.saveAndFlush(any())).thenAnswer(assignExpenseIds)

		val req = AddExpenseRequest(
			description = " Museu ",
			amount = BigDecimal("20"),
			category = " Lazer ",
			date = tripStart.plusSeconds(60),
		)

		val dto = expenseService.addExpense(1L, req)

		assertEquals(1, dto.expenses.size)
		assertEquals("Museu", dto.expenses[0].description)
		assertEquals("Lazer", dto.expenses[0].category)
		verify(tripRepository).saveAndFlush(any())
	}

	@Test
	fun `listByTrip retorna despesas ordenadas por data`() {
		val trip = Trip(
			id = 1L,
			title = "T",
			city = "C",
			country = "BR",
			tripStart = tripStart,
			tripEnd = tripEnd,
			budget = BigDecimal("500"),
		)
		trip.expenses.add(
			Expense(
				id = 2L,
				trip = trip,
				description = "Depois",
				amount = BigDecimal("5"),
				category = "X",
				expenseDate = tripEnd.minusSeconds(100),
			),
		)
		trip.expenses.add(
			Expense(
				id = 1L,
				trip = trip,
				description = "Antes",
				amount = BigDecimal("3"),
				category = "X",
				expenseDate = tripStart.plusSeconds(60),
			),
		)
		whenever(tripRepository.findById(1L)).thenReturn(Optional.of(trip))
		val list = expenseService.listByTrip(1L)
		assertEquals(listOf("Antes", "Depois"), list.map { it.description })
	}

	@Test
	fun `getById retorna despesa`() {
		val trip = Trip(
			id = 1L,
			title = "T",
			city = "C",
			country = "BR",
			tripStart = tripStart,
			tripEnd = tripEnd,
			budget = BigDecimal("500"),
		)
		trip.expenses.add(
			Expense(
				id = 9L,
				trip = trip,
				description = "Almoço",
				amount = BigDecimal("40"),
				category = "Food",
				expenseDate = tripStart,
			),
		)
		whenever(tripRepository.findById(1L)).thenReturn(Optional.of(trip))
		val dto = expenseService.getById(1L, 9L)
		assertEquals("Almoço", dto.description)
		assertEquals(BigDecimal("40"), dto.amount)
	}

	@Test
	fun `getById lanca quando despesa nao existe`() {
		whenever(tripRepository.findById(1L)).thenReturn(Optional.of(baseTripForExpense()))
		assertFailsWith<ResourceNotFoundException> {
			expenseService.getById(1L, 404L)
		}
	}

	@Test
	fun `updateExpense atualiza campos`() {
		val trip = Trip(
			id = 1L,
			title = "T",
			city = "C",
			country = "BR",
			tripStart = tripStart,
			tripEnd = tripEnd,
			budget = BigDecimal("500"),
		)
		trip.expenses.add(
			Expense(
				id = 20L,
				trip = trip,
				description = "Old",
				amount = BigDecimal("10"),
				category = "X",
				expenseDate = tripStart,
			),
		)
		whenever(tripRepository.findById(1L)).thenReturn(Optional.of(trip))
		whenever(tripRepository.save(trip)).thenReturn(trip)
		val req = ExpenseUpdateRequest(
			description = " Novo ",
			amount = BigDecimal("99"),
			category = " Cat ",
			date = tripStart.plusSeconds(120),
		)
		val dto = expenseService.updateExpense(1L, 20L, req)
		assertEquals("Novo", dto.expenses.single { it.id == 20L }.description)
		assertEquals("Cat", dto.expenses.single { it.id == 20L }.category)
		verify(tripRepository).save(trip)
	}

	@Test
	fun `updateExpense lanca quando despesa nao existe`() {
		whenever(tripRepository.findById(1L)).thenReturn(Optional.of(baseTripForExpense()))
		val req = ExpenseUpdateRequest(
			description = "A",
			amount = BigDecimal("1"),
			category = "B",
			date = tripStart,
		)
		assertFailsWith<ResourceNotFoundException> {
			expenseService.updateExpense(1L, 404L, req)
		}
		verify(tripRepository, never()).save(any())
	}

	@Test
	fun `removeExpense lanca quando despesa nao pertence ao pedido`() {
		val trip = Trip(
			id = 1L,
			title = "T",
			city = "C",
			country = "BR",
			tripStart = tripStart,
			tripEnd = tripEnd,
			budget = BigDecimal("500"),
		)
		trip.expenses.add(
			Expense(
				id = 50L,
				trip = trip,
				description = "A",
				amount = BigDecimal("1"),
				category = "X",
				expenseDate = tripStart,
			),
		)
		whenever(tripRepository.findById(1L)).thenReturn(Optional.of(trip))

		assertFailsWith<ResourceNotFoundException> {
			expenseService.removeExpense(1L, 999L)
		}
		verify(tripRepository, never()).save(any())
		verify(tripRepository, never()).saveAndFlush(any())
	}

	@Test
	fun `removeExpense remove e persiste`() {
		val trip = Trip(
			id = 1L,
			title = "T",
			city = "C",
			country = "BR",
			tripStart = tripStart,
			tripEnd = tripEnd,
			budget = BigDecimal("500"),
		)
		val exp = Expense(
			id = 50L,
			trip = trip,
			description = "A",
			amount = BigDecimal("15"),
			category = "X",
			expenseDate = tripStart,
		)
		trip.expenses.add(exp)
		whenever(tripRepository.findById(1L)).thenReturn(Optional.of(trip))
		whenever(tripRepository.save(trip)).thenReturn(trip)

		val dto = expenseService.removeExpense(1L, 50L)

		assertEquals(0, dto.expenses.size)
		verify(tripRepository).save(trip)
	}

	private fun baseTripForExpense() = Trip(
		id = 1L,
		title = "T",
		city = "C",
		country = "BR",
		tripStart = tripStart,
		tripEnd = tripEnd,
		budget = BigDecimal("500"),
	)
}
