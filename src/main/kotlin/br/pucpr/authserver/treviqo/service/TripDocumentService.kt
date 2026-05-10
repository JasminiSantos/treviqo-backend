package br.pucpr.authserver.treviqo.service

import br.pucpr.authserver.exception.ResourceNotFoundException
import br.pucpr.authserver.treviqo.dto.DocumentCreateRequest
import br.pucpr.authserver.treviqo.dto.DocumentResponse
import br.pucpr.authserver.treviqo.dto.DocumentUpdateRequest
import br.pucpr.authserver.treviqo.repository.TripRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class TripDocumentService(
	private val tripRepository: TripRepository,
) {

	private val log = LoggerFactory.getLogger(javaClass)

	private fun loadTrip(tripId: Long) = tripRepository.findById(tripId).orElseThrow {
		log.warn("Operação em documento falhou: viagem id={} não existe", tripId)
		ResourceNotFoundException("Viagem $tripId não encontrada")
	}

	@Transactional(readOnly = true)
	fun listByTrip(tripId: Long): List<DocumentResponse> {
		val trip = loadTrip(tripId)
		trip.documents.size
		return trip.documents.sortedBy { it.name.lowercase() }.map { it.toResponse() }
	}

	@Transactional(readOnly = true)
	fun getById(tripId: Long, documentId: Long): DocumentResponse {
		val trip = loadTrip(tripId)
		val doc = trip.documents.find { it.id == documentId }
		if (doc == null) {
			log.warn("Documento id={} não encontrado na viagem {}", documentId, tripId)
			throw ResourceNotFoundException("Documento $documentId não pertence à viagem $tripId")
		}
		return doc.toResponse()
	}

	@Transactional
	fun create(tripId: Long, req: DocumentCreateRequest): DocumentResponse {
		val trip = loadTrip(tripId)
		val doc = trip.addDocument(req.name, req.type, req.uri, req.notes)
		tripRepository.saveAndFlush(trip)
		log.info("Documento criado id={} na viagem {}", doc.id, tripId)
		return doc.toResponse()
	}

	@Transactional
	fun update(tripId: Long, documentId: Long, req: DocumentUpdateRequest): DocumentResponse {
		val trip = loadTrip(tripId)
		val doc = trip.documents.find { it.id == documentId }
		if (doc == null) {
			log.warn("Atualização de documento falhou: id={} na viagem {}", documentId, tripId)
			throw ResourceNotFoundException("Documento $documentId não pertence à viagem $tripId")
		}
		trip.updateDocument(doc, req.name, req.type, req.uri, req.notes)
		tripRepository.save(trip)
		log.info("Documento {} atualizado na viagem {}", documentId, tripId)
		return doc.toResponse()
	}

	@Transactional
	fun delete(tripId: Long, documentId: Long) {
		val trip = loadTrip(tripId)
		val doc = trip.documents.find { it.id == documentId }
		if (doc == null) {
			log.warn("Exclusão de documento falhou: id={} na viagem {}", documentId, tripId)
			throw ResourceNotFoundException("Documento $documentId não pertence à viagem $tripId")
		}
		trip.removeDocument(doc)
		tripRepository.save(trip)
		log.info("Documento {} removido da viagem {}", documentId, tripId)
	}
}
