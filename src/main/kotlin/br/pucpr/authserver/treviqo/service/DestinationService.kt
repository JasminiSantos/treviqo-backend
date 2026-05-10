package br.pucpr.authserver.treviqo.service

import br.pucpr.authserver.exception.ResourceNotFoundException
import br.pucpr.authserver.treviqo.dto.DestinationCreateRequest
import br.pucpr.authserver.treviqo.dto.DestinationResponse
import br.pucpr.authserver.treviqo.dto.DestinationUpdateRequest
import br.pucpr.authserver.treviqo.repository.TripRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DestinationService(
	private val tripRepository: TripRepository,
) {

	private val log = LoggerFactory.getLogger(javaClass)

	private fun loadTrip(tripId: Long) = tripRepository.findById(tripId).orElseThrow {
		log.warn("Operação em destino falhou: viagem id={} não existe", tripId)
		ResourceNotFoundException("Viagem $tripId não encontrada")
	}

	@Transactional(readOnly = true)
	fun listByTrip(tripId: Long): List<DestinationResponse> {
		val trip = loadTrip(tripId)
		trip.destinations.size
		return trip.destinations.sortedBy { it.stayStart }.map { it.toResponse() }
	}

	@Transactional(readOnly = true)
	fun getById(tripId: Long, destinationId: Long): DestinationResponse {
		val trip = loadTrip(tripId)
		val d = trip.destinations.find { it.id == destinationId }
		if (d == null) {
			log.warn("Destino id={} não encontrado na viagem {}", destinationId, tripId)
			throw ResourceNotFoundException("Destino $destinationId não pertence à viagem $tripId")
		}
		return d.toResponse()
	}

	@Transactional
	fun create(tripId: Long, req: DestinationCreateRequest): DestinationResponse {
		val trip = loadTrip(tripId)
		val d = trip.addDestination(req.city, req.stayStart, req.stayEnd, req.notes)
		tripRepository.saveAndFlush(trip)
		log.info("Destino criado id={} na viagem {}", d.id, tripId)
		return d.toResponse()
	}

	@Transactional
	fun update(tripId: Long, destinationId: Long, req: DestinationUpdateRequest): DestinationResponse {
		val trip = loadTrip(tripId)
		val d = trip.destinations.find { it.id == destinationId }
		if (d == null) {
			log.warn("Atualização de destino falhou: id={} na viagem {}", destinationId, tripId)
			throw ResourceNotFoundException("Destino $destinationId não pertence à viagem $tripId")
		}
		trip.updateDestination(d, req.city, req.stayStart, req.stayEnd, req.notes)
		tripRepository.save(trip)
		log.info("Destino {} atualizado na viagem {}", destinationId, tripId)
		return d.toResponse()
	}

	@Transactional
	fun delete(tripId: Long, destinationId: Long) {
		val trip = loadTrip(tripId)
		val d = trip.destinations.find { it.id == destinationId }
		if (d == null) {
			log.warn("Exclusão de destino falhou: id={} na viagem {}", destinationId, tripId)
			throw ResourceNotFoundException("Destino $destinationId não pertence à viagem $tripId")
		}
		trip.removeDestination(d)
		tripRepository.save(trip)
		log.info("Destino {} removido da viagem {}", destinationId, tripId)
	}
}
