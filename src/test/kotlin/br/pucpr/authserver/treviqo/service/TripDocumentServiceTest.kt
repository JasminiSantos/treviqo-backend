package br.pucpr.authserver.treviqo.service

import br.pucpr.authserver.exception.ResourceNotFoundException
import br.pucpr.authserver.treviqo.domain.DocumentType
import br.pucpr.authserver.treviqo.domain.Trip
import br.pucpr.authserver.treviqo.domain.TripDocument
import br.pucpr.authserver.treviqo.dto.DocumentCreateRequest
import br.pucpr.authserver.treviqo.dto.DocumentUpdateRequest
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
import kotlin.test.assertNull

@ExtendWith(MockitoExtension::class)
class TripDocumentServiceTest {

	@Mock
	private lateinit var tripRepository: TripRepository

	@InjectMocks
	private lateinit var tripDocumentService: TripDocumentService

	private val tripStart = Instant.parse("2026-10-01T08:00:00Z")
	private val tripEnd = Instant.parse("2026-10-05T20:00:00Z")

	private fun baseTrip() = Trip(
		id = 1L,
		title = "T",
		city = "C",
		country = "BR",
		tripStart = tripStart,
		tripEnd = tripEnd,
		budget = BigDecimal("500"),
	)

	private fun flushAssignDocumentIds(): (org.mockito.invocation.InvocationOnMock) -> Trip = { invocation ->
		val t = invocation.getArgument<Trip>(0)
		var seq = 300L
		for (doc in t.documents) {
			if (doc.id == null) {
				doc.id = seq++
			}
		}
		t
	}

	@Test
	fun `listByTrip lanca quando viagem nao existe`() {
		whenever(tripRepository.findById(99L)).thenReturn(Optional.empty())
		assertFailsWith<ResourceNotFoundException> {
			tripDocumentService.listByTrip(99L)
		}
	}

	@Test
	fun `listByTrip ordena por nome ignore case`() {
		val trip = baseTrip()
		trip.documents.add(
			TripDocument(id = 1L, trip = trip, name = "beta", type = DocumentType.Other, uri = null, notes = null),
		)
		trip.documents.add(
			TripDocument(id = 2L, trip = trip, name = "Alpha", type = DocumentType.Ticket, uri = null, notes = null),
		)
		whenever(tripRepository.findById(1L)).thenReturn(Optional.of(trip))
		val names = tripDocumentService.listByTrip(1L).map { it.name }
		assertEquals(listOf("Alpha", "beta"), names)
	}

	@Test
	fun `getById lanca quando documento nao existe`() {
		whenever(tripRepository.findById(1L)).thenReturn(Optional.of(baseTrip()))
		assertFailsWith<ResourceNotFoundException> {
			tripDocumentService.getById(1L, 404L)
		}
	}

	@Test
	fun `create persiste documento`() {
		val trip = baseTrip()
		whenever(tripRepository.findById(1L)).thenReturn(Optional.of(trip))
		whenever(tripRepository.saveAndFlush(any())).thenAnswer(flushAssignDocumentIds())
		val req = DocumentCreateRequest(
			name = " Passagem ",
			type = DocumentType.Ticket,
			uri = " https://x/y ",
			notes = null,
		)
		val resp = tripDocumentService.create(1L, req)
		assertEquals("Passagem", resp.name)
		assertEquals(DocumentType.Ticket, resp.type)
		assertEquals("https://x/y", resp.uri)
		verify(tripRepository).saveAndFlush(any())
	}

	@Test
	fun `update persiste alteracoes`() {
		val trip = baseTrip()
		val doc = TripDocument(
			id = 5L,
			trip = trip,
			name = "Old",
			type = DocumentType.Other,
			uri = null,
			notes = null,
		)
		trip.documents.add(doc)
		whenever(tripRepository.findById(1L)).thenReturn(Optional.of(trip))
		whenever(tripRepository.save(trip)).thenReturn(trip)
		val req = DocumentUpdateRequest(
			name = "Seguro",
			type = DocumentType.Insurance,
			uri = "",
			notes = null,
		)
		val resp = tripDocumentService.update(1L, 5L, req)
		assertEquals("Seguro", resp.name)
		assertEquals(DocumentType.Insurance, resp.type)
		assertNull(resp.uri)
		assertNull(resp.notes)
		verify(tripRepository).save(trip)
	}

	@Test
	fun `update lanca quando documento nao existe`() {
		whenever(tripRepository.findById(1L)).thenReturn(Optional.of(baseTrip()))
		val req = DocumentUpdateRequest(name = "X", type = DocumentType.Other, uri = null, notes = null)
		assertFailsWith<ResourceNotFoundException> {
			tripDocumentService.update(1L, 999L, req)
		}
		verify(tripRepository, never()).save(any())
	}

	@Test
	fun `delete remove e persiste`() {
		val trip = baseTrip()
		val doc = TripDocument(
			id = 7L,
			trip = trip,
			name = "Z",
			type = DocumentType.Hotel,
			uri = null,
			notes = null,
		)
		trip.documents.add(doc)
		whenever(tripRepository.findById(1L)).thenReturn(Optional.of(trip))
		whenever(tripRepository.save(trip)).thenReturn(trip)
		tripDocumentService.delete(1L, 7L)
		assertEquals(0, trip.documents.size)
		verify(tripRepository).save(trip)
	}
}
