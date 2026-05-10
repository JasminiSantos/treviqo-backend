package br.pucpr.authserver.treviqo.service

import br.pucpr.authserver.exception.BusinessRuleException
import br.pucpr.authserver.exception.ResourceNotFoundException
import br.pucpr.authserver.treviqo.domain.Trip
import br.pucpr.authserver.treviqo.dto.TripCreateRequest
import br.pucpr.authserver.treviqo.dto.TripUpdateRequest
import br.pucpr.authserver.treviqo.repository.TripRepository
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.domain.Specification
import java.math.BigDecimal
import java.time.Instant
import java.util.Optional
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@ExtendWith(MockitoExtension::class)
class TripServiceTest {

	@Mock
	private lateinit var tripRepository: TripRepository

	@InjectMocks
	private lateinit var tripService: TripService

	private val start = Instant.parse("2026-07-01T12:00:00Z")
	private val end = Instant.parse("2026-07-05T12:00:00Z")

	@Test
	fun `getById lanca quando viagem nao existe`() {
		whenever(tripRepository.findById(42L)).thenReturn(Optional.empty())

		assertFailsWith<ResourceNotFoundException> {
			tripService.getById(42L)
		}
	}

	@Test
	fun `getById retorna detalhe quando existe`() {
		val trip = Trip(
			id = 1L,
			title = "Viagem",
			city = "Curitiba",
			country = "BR",
			tripStart = start,
			tripEnd = end,
			budget = BigDecimal("800"),
		)
		whenever(tripRepository.findById(1L)).thenReturn(Optional.of(trip))

		val dto = tripService.getById(1L)

		assertEquals("Viagem", dto.title)
		assertEquals(0, dto.expenseCount)
		assertEquals(BigDecimal.ZERO.setScale(2), dto.totalSpent.setScale(2))
	}

	@Test
	fun `create lanca quando periodo invalido`() {
		val req = TripCreateRequest(
			title = " A ",
			city = "X",
			country = "Y",
			start = end,
			end = start,
			budget = BigDecimal("1"),
		)

		assertFailsWith<BusinessRuleException> {
			tripService.create(req)
		}
		verify(tripRepository, never()).save(any())
	}

	@Test
	fun `create persiste e retorna detalhe`() {
		val req = TripCreateRequest(
			title = " Europa ",
			city = " Roma ",
			country = " IT ",
			coverUrl = " ",
			start = start,
			end = end,
			budget = BigDecimal("5000"),
			notes = " obs ",
		)
		whenever(tripRepository.save(any())).thenAnswer { invocation ->
			val t = invocation.getArgument<Trip>(0)
			t.id = 99L
			t
		}

		val dto = tripService.create(req)

		assertEquals(99L, dto.id)
		val captor = argumentCaptor<Trip>()
		verify(tripRepository).save(captor.capture())
		assertEquals("Europa", captor.firstValue.title)
		assertEquals("Roma", captor.firstValue.city)
		assertEquals("IT", captor.firstValue.country)
		assertEquals(null, captor.firstValue.coverUrl)
		assertEquals("obs", captor.firstValue.notes)
	}

	@Test
	fun `update lanca quando id nao existe`() {
		whenever(tripRepository.findById(5L)).thenReturn(Optional.empty())

		val req = TripUpdateRequest(
			title = "T",
			city = "C",
			country = "BR",
			start = start,
			end = end,
			budget = BigDecimal("10"),
		)

		assertFailsWith<ResourceNotFoundException> {
			tripService.update(5L, req)
		}
	}

	@Test
	fun `delete lanca quando id nao existe`() {
		whenever(tripRepository.existsById(3L)).thenReturn(false)

		assertFailsWith<ResourceNotFoundException> {
			tripService.delete(3L)
		}
		verify(tripRepository, never()).deleteById(any())
	}

	@Test
	fun `delete remove quando existe`() {
		whenever(tripRepository.existsById(3L)).thenReturn(true)

		tripService.delete(3L)

		verify(tripRepository).deleteById(3L)
	}

	@Test
	fun `search delega ao repositorio e mapeia resumo`() {
		val trip = Trip(
			id = 2L,
			title = "Test",
			city = "C",
			country = "BR",
			tripStart = start,
			tripEnd = end,
			budget = BigDecimal("100"),
		)
		whenever(tripRepository.findAll(any<Specification<Trip>>(), any<Sort>())).thenReturn(listOf(trip))

		val list = tripService.search(null, null, null, null, null, null, null, null, null)

		assertEquals(1, list.size)
		assertEquals(2L, list[0].id)
		verify(tripRepository).findAll(any<Specification<Trip>>(), any<Sort>())
	}
}
