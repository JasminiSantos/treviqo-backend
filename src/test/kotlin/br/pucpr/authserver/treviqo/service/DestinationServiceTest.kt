package br.pucpr.authserver.treviqo.service

import br.pucpr.authserver.exception.BusinessRuleException
import br.pucpr.authserver.exception.ResourceNotFoundException
import br.pucpr.authserver.treviqo.domain.Destination
import br.pucpr.authserver.treviqo.domain.Trip
import br.pucpr.authserver.treviqo.dto.DestinationCreateRequest
import br.pucpr.authserver.treviqo.dto.DestinationUpdateRequest
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
import java.time.Instant
import java.util.Optional
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@ExtendWith(MockitoExtension::class)
class DestinationServiceTest {

	@Mock
	private lateinit var tripRepository: TripRepository

	@InjectMocks
	private lateinit var destinationService: DestinationService

	private val tripStart = Instant.parse("2026-09-01T08:00:00Z")
	private val tripEnd = Instant.parse("2026-09-10T20:00:00Z")

	private fun baseTrip() = Trip(
		id = 1L,
		title = "T",
		city = "C",
		country = "BR",
		tripStart = tripStart,
		tripEnd = tripEnd,
		budget = java.math.BigDecimal("1000"),
	)

	private fun flushAssignDestinationIds(): (org.mockito.invocation.InvocationOnMock) -> Trip = { invocation ->
		val t = invocation.getArgument<Trip>(0)
		var seq = 200L
		for (d in t.destinations) {
			if (d.id == null) {
				d.id = seq++
			}
		}
		t
	}

	@Test
	fun `listByTrip lanca quando viagem nao existe`() {
		whenever(tripRepository.findById(99L)).thenReturn(Optional.empty())
		assertFailsWith<ResourceNotFoundException> {
			destinationService.listByTrip(99L)
		}
	}

	@Test
	fun `listByTrip retorna destinos ordenados por stayStart`() {
		val trip = baseTrip()
		trip.destinations.add(
			Destination(
				id = 2L,
				trip = trip,
				city = "B",
				stayStart = tripStart.plusSeconds(86400),
				stayEnd = tripEnd.minusSeconds(86400),
				notes = null,
			),
		)
		trip.destinations.add(
			Destination(
				id = 1L,
				trip = trip,
				city = "A",
				stayStart = tripStart,
				stayEnd = tripStart.plusSeconds(3600),
				notes = null,
			),
		)
		whenever(tripRepository.findById(1L)).thenReturn(Optional.of(trip))
		val list = destinationService.listByTrip(1L)
		assertEquals(listOf("A", "B"), list.map { it.city })
	}

	@Test
	fun `getById lanca quando destino nao existe`() {
		val trip = baseTrip()
		whenever(tripRepository.findById(1L)).thenReturn(Optional.of(trip))
		assertFailsWith<ResourceNotFoundException> {
			destinationService.getById(1L, 404L)
		}
	}

	@Test
	fun `create lanca quando periodo fora da viagem`() {
		val trip = baseTrip()
		whenever(tripRepository.findById(1L)).thenReturn(Optional.of(trip))
		val req = DestinationCreateRequest(
			city = "X",
			stayStart = tripEnd.plusSeconds(1),
			stayEnd = tripEnd.plusSeconds(3600),
			notes = null,
		)
		assertFailsWith<BusinessRuleException> {
			destinationService.create(1L, req)
		}
		verify(tripRepository, never()).saveAndFlush(any())
	}

	@Test
	fun `create persiste e retorna destino`() {
		val trip = baseTrip()
		whenever(tripRepository.findById(1L)).thenReturn(Optional.of(trip))
		whenever(tripRepository.saveAndFlush(any())).thenAnswer(flushAssignDestinationIds())
		val req = DestinationCreateRequest(
			city = " Gramado ",
			stayStart = tripStart.plusSeconds(3600),
			stayEnd = tripStart.plusSeconds(7200),
			notes = " frio ",
		)
		val resp = destinationService.create(1L, req)
		assertEquals("Gramado", resp.city)
		assertEquals("frio", resp.notes)
		verify(tripRepository).saveAndFlush(any())
	}

	@Test
	fun `update lanca quando destino nao existe`() {
		val trip = baseTrip()
		whenever(tripRepository.findById(1L)).thenReturn(Optional.of(trip))
		val req = DestinationUpdateRequest(
			city = "Y",
			stayStart = tripStart,
			stayEnd = tripStart.plusSeconds(7200),
			notes = null,
		)
		assertFailsWith<ResourceNotFoundException> {
			destinationService.update(1L, 999L, req)
		}
		verify(tripRepository, never()).save(any())
	}

	@Test
	fun `update persiste alteracoes`() {
		val trip = baseTrip()
		val dest = Destination(
			id = 10L,
			trip = trip,
			city = "Old",
			stayStart = tripStart,
			stayEnd = tripStart.plusSeconds(3600),
			notes = null,
		)
		trip.destinations.add(dest)
		whenever(tripRepository.findById(1L)).thenReturn(Optional.of(trip))
		whenever(tripRepository.save(trip)).thenReturn(trip)
		val req = DestinationUpdateRequest(
			city = "NewCity",
			stayStart = tripStart,
			stayEnd = tripStart.plusSeconds(7200),
			notes = null,
		)
		val resp = destinationService.update(1L, 10L, req)
		assertEquals("NewCity", resp.city)
		verify(tripRepository).save(trip)
	}

	@Test
	fun `delete lanca quando destino nao existe`() {
		val trip = baseTrip()
		whenever(tripRepository.findById(1L)).thenReturn(Optional.of(trip))
		assertFailsWith<ResourceNotFoundException> {
			destinationService.delete(1L, 42L)
		}
		verify(tripRepository, never()).save(any())
	}

	@Test
	fun `delete remove e persiste`() {
		val trip = baseTrip()
		val dest = Destination(
			id = 10L,
			trip = trip,
			city = "X",
			stayStart = tripStart,
			stayEnd = tripStart.plusSeconds(100),
			notes = null,
		)
		trip.destinations.add(dest)
		whenever(tripRepository.findById(1L)).thenReturn(Optional.of(trip))
		whenever(tripRepository.save(trip)).thenReturn(trip)
		destinationService.delete(1L, 10L)
		assertEquals(0, trip.destinations.size)
		verify(tripRepository).save(trip)
	}
}
