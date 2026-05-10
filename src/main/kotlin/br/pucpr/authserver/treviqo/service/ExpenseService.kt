package br.pucpr.authserver.treviqo.service

import br.pucpr.authserver.exception.ResourceNotFoundException
import br.pucpr.authserver.treviqo.dto.AddExpenseRequest
import br.pucpr.authserver.treviqo.dto.ExpenseResponse
import br.pucpr.authserver.treviqo.dto.ExpenseUpdateRequest
import br.pucpr.authserver.treviqo.dto.TripResponse
import br.pucpr.authserver.treviqo.repository.TripRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ExpenseService(
	private val tripRepository: TripRepository,
) {

	private val log = LoggerFactory.getLogger(javaClass)

	private fun loadTrip(tripId: Long) = tripRepository.findById(tripId).orElseThrow {
		log.warn("Operação em despesa falhou: viagem id={} não existe", tripId)
		ResourceNotFoundException("Viagem $tripId não encontrada")
	}

	@Transactional(readOnly = true)
	fun listByTrip(tripId: Long): List<ExpenseResponse> {
		val trip = loadTrip(tripId)
		trip.expenses.size
		return trip.expenses.sortedBy { it.expenseDate }.map { it.toResponse() }
	}

	@Transactional(readOnly = true)
	fun getById(tripId: Long, expenseId: Long): ExpenseResponse {
		val trip = loadTrip(tripId)
		val expense = trip.expenses.find { it.id == expenseId }
		if (expense == null) {
			log.warn("Despesa id={} não encontrada na viagem {}", expenseId, tripId)
			throw ResourceNotFoundException("Despesa $expenseId não pertence à viagem $tripId")
		}
		return expense.toResponse()
	}

	@Transactional
	fun addExpense(tripId: Long, req: AddExpenseRequest): TripResponse {
		val trip = loadTrip(tripId)
		trip.validateExpenseInsideTrip(req.date)
		trip.addExpense(
			description = req.description.trim(),
			amount = req.amount,
			category = req.category.trim(),
			expenseDate = req.date,
		)
		log.info("Despesa adicionada à viagem {} categoria={}", tripId, req.category)
		return tripRepository.saveAndFlush(trip).toResponse(includeNested = true)
	}

	@Transactional
	fun updateExpense(tripId: Long, expenseId: Long, req: ExpenseUpdateRequest): TripResponse {
		val trip = loadTrip(tripId)
		val expense = trip.expenses.find { it.id == expenseId }
		if (expense == null) {
			log.warn("Atualização de despesa falhou: id={} na viagem {}", expenseId, tripId)
			throw ResourceNotFoundException("Despesa $expenseId não pertence à viagem $tripId")
		}
		trip.validateExpenseInsideTrip(req.date)
		expense.description = req.description.trim()
		expense.amount = req.amount
		expense.category = req.category.trim()
		expense.expenseDate = req.date
		log.info("Despesa {} atualizada na viagem {}", expenseId, tripId)
		return tripRepository.save(trip).toResponse(includeNested = true)
	}

	@Transactional
	fun removeExpense(tripId: Long, expenseId: Long): TripResponse {
		val trip = loadTrip(tripId)
		val expense = trip.expenses.find { it.id == expenseId }
		if (expense == null) {
			log.warn("Despesa id={} não encontrada na viagem {}", expenseId, tripId)
			throw ResourceNotFoundException("Despesa $expenseId não pertence à viagem $tripId")
		}
		trip.removeExpense(expense)
		log.info("Despesa {} removida da viagem {}", expenseId, tripId)
		return tripRepository.save(trip).toResponse(includeNested = true)
	}
}
