package br.pucpr.authserver.treviqo

import br.pucpr.authserver.treviqo.domain.DocumentType
import br.pucpr.authserver.treviqo.domain.Trip
import br.pucpr.authserver.treviqo.repository.TripRepository
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.math.BigDecimal
import java.time.Instant

@Configuration
class TreviqoDataLoader {

	private val log = LoggerFactory.getLogger(javaClass)

	@Bean
	fun loadSampleTrips(tripRepository: TripRepository) = CommandLineRunner {
		if (tripRepository.count() > 0) return@CommandLineRunner
		val summerStart = Instant.parse("2026-01-10T12:00:00Z")
		val summerEnd = Instant.parse("2026-01-20T12:00:00Z")
		val trip = Trip(
			title = "Réveillon na praia",
			city = "Florianópolis",
			country = "Brasil",
			coverUrl = "https://www.civitatis.com/blog/wp-content/uploads/2024/11/shutterstock_2304644691-scaled.jpg",
			tripStart = summerStart,
			tripEnd = summerEnd,
			budget = BigDecimal("4500.00"),
			notes = "Reserva de hospedagem confirmada.",
		)
		trip.addExpense(
			description = "Passagem aérea ida e volta",
			amount = BigDecimal("980.00"),
			category = "Transporte",
			expenseDate = Instant.parse("2026-01-02T15:00:00Z"),
		)
		trip.addExpense(
			description = "Airbnb — 5 noites",
			amount = BigDecimal("2100.00"),
			category = "Hospedagem",
			expenseDate = Instant.parse("2026-01-03T10:00:00Z"),
		)
		trip.addDestination(
			city = "Jurerê Internacional",
			stayStart = Instant.parse("2026-01-11T14:00:00Z"),
			stayEnd = Instant.parse("2026-01-17T12:00:00Z"),
			notes = "Período principal na ilha",
		)
		trip.addDocument(
			name = "Reserva hotel.pdf",
			type = DocumentType.Hotel,
			uri = "https://example.com/docs/hotel.pdf",
			notes = "Check-in após 14h",
		)
		val weekend = Trip(
			title = "Encontro em POA",
			city = "Porto Alegre",
			country = "Brasil",
			coverUrl = null,
			tripStart = Instant.parse("2026-03-01T08:00:00Z"),
			tripEnd = Instant.parse("2026-03-03T18:00:00Z"),
			budget = BigDecimal("900.00"),
			notes = null,
		)
		weekend.addExpense(
			description = "Ônibus",
			amount = BigDecimal("120.00"),
			category = "Transporte",
			expenseDate = Instant.parse("2026-03-01T06:00:00Z"),
		)
		weekend.addDocument(
			name = "Convite encontro",
			type = DocumentType.Other,
			uri = null,
			notes = null,
		)
		tripRepository.saveAll(listOf(trip, weekend))
		log.info("Viagens de exemplo Treviqo inseridas.")
	}
}
