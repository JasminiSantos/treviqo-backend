package br.pucpr.authserver.treviqo.service

import br.pucpr.authserver.exception.ResourceNotFoundException
import br.pucpr.authserver.treviqo.domain.Trip
import br.pucpr.authserver.treviqo.dto.TripCreateRequest
import br.pucpr.authserver.treviqo.dto.TripResponse
import br.pucpr.authserver.treviqo.dto.TripUpdateRequest
import br.pucpr.authserver.treviqo.repository.TripRepository
import jakarta.persistence.criteria.Predicate
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.Instant

@Service
class TripService(
	private val tripRepository: TripRepository,
) {

	private val log = LoggerFactory.getLogger(javaClass)

	@Transactional(readOnly = true)
	fun search(
		titleContains: String?,
		cityContains: String?,
		countryContains: String?,
		startAfter: Instant?,
		startBefore: Instant?,
		minBudget: BigDecimal?,
		maxBudget: BigDecimal?,
		sortBy: String?,
		sortDirection: String?,
	): List<TripResponse> {
		val spec = buildTripSpecification(
			titleContains = titleContains,
			cityContains = cityContains,
			countryContains = countryContains,
			startAfter = startAfter,
			startBefore = startBefore,
			minBudget = minBudget,
			maxBudget = maxBudget,
		)
		val sort = resolveTripSort(sortBy, sortDirection)
		val trips = tripRepository.findAll(spec, sort)
		log.info("Consulta de viagens retornou {} registro(s)", trips.size)
		return trips.map { it.toResponse(includeNested = false) }
	}

	@Transactional(readOnly = true)
	fun getById(id: Long): TripResponse {
		val trip = tripRepository.findById(id).orElseThrow {
			log.warn("Viagem id={} não encontrada", id)
			ResourceNotFoundException("Viagem $id não encontrada")
		}
		trip.expenses.size
		trip.destinations.size
		trip.documents.size
		return trip.toResponse(includeNested = true)
	}

	@Transactional
	fun create(req: TripCreateRequest): TripResponse {
		validateTripPeriod(req.start, req.end)
		val trip = Trip(
			title = req.title.trim(),
			city = req.city.trim(),
			country = req.country.trim(),
			coverUrl = req.coverUrl?.trim()?.takeIf { it.isNotEmpty() },
			tripStart = req.start,
			tripEnd = req.end,
			budget = req.budget,
			notes = req.notes?.trim(),
		)
		val saved = tripRepository.save(trip)
		log.info("Viagem criada id={}, título={}", saved.id, saved.title)
		return saved.toResponse(includeNested = true)
	}

	@Transactional
	fun update(id: Long, req: TripUpdateRequest): TripResponse {
		val trip = tripRepository.findById(id).orElseThrow {
			log.warn("Atualização falhou: viagem id={} não existe", id)
			ResourceNotFoundException("Viagem $id não encontrada")
		}
		validateTripPeriod(req.start, req.end)
		trip.title = req.title.trim()
		trip.city = req.city.trim()
		trip.country = req.country.trim()
		trip.coverUrl = req.coverUrl?.trim()?.takeIf { it.isNotEmpty() }
		trip.tripStart = req.start
		trip.tripEnd = req.end
		trip.budget = req.budget
		trip.notes = req.notes?.trim()
		log.info("Viagem atualizada id={}", id)
		return tripRepository.save(trip).toResponse(includeNested = true)
	}

	@Transactional
	fun delete(id: Long) {
		if (!tripRepository.existsById(id)) {
			log.warn("Exclusão falhou: viagem id={} não existe", id)
			throw ResourceNotFoundException("Viagem $id não encontrada")
		}
		tripRepository.deleteById(id)
		log.info("Viagem removida id={}", id)
	}

	private fun buildTripSpecification(
		titleContains: String?,
		cityContains: String?,
		countryContains: String?,
		startAfter: Instant?,
		startBefore: Instant?,
		minBudget: BigDecimal?,
		maxBudget: BigDecimal?,
	): Specification<Trip> = Specification { root, _, cb ->
		val predicates = mutableListOf<Predicate>()
		titleContains?.trim()?.takeIf { it.isNotEmpty() }?.let { frag ->
			predicates += cb.like(cb.lower(root.get("title")), "%${frag.lowercase()}%")
		}
		cityContains?.trim()?.takeIf { it.isNotEmpty() }?.let { frag ->
			predicates += cb.like(cb.lower(root.get("city")), "%${frag.lowercase()}%")
		}
		countryContains?.trim()?.takeIf { it.isNotEmpty() }?.let { frag ->
			predicates += cb.like(cb.lower(root.get("country")), "%${frag.lowercase()}%")
		}
		startAfter?.let { predicates += cb.greaterThanOrEqualTo(root.get("tripStart"), it) }
		startBefore?.let { predicates += cb.lessThanOrEqualTo(root.get("tripStart"), it) }
		minBudget?.let { predicates += cb.ge(root.get("budget"), it) }
		maxBudget?.let { predicates += cb.le(root.get("budget"), it) }
		if (predicates.isEmpty()) cb.conjunction() else cb.and(*predicates.toTypedArray())
	}

	private fun resolveTripSort(sortBy: String?, sortDirection: String?): Sort {
		val property = when (sortBy?.lowercase()) {
			"city" -> "city"
			"country" -> "country"
			"budget" -> "budget"
			"end", "tripend" -> "tripEnd"
			else -> "tripStart"
		}
		val dir =
			if (sortDirection?.equals("desc", ignoreCase = true) == true) Sort.Direction.DESC else Sort.Direction.ASC
		return Sort.by(dir, property)
	}
}
