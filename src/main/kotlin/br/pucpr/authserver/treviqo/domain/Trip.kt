package br.pucpr.authserver.treviqo.domain

import br.pucpr.authserver.exception.BusinessRuleException
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import java.math.BigDecimal
import java.time.Instant

/**
 * Viagem planejada (cenário Treviqo): despesas, destinos e documentos em relação um-para-muitos.
 */
@Entity
@Table(name = "trips")
class Trip(
	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	var id: Long? = null,

	@Column(nullable = false, length = 200)
	var title: String,

	@Column(nullable = false, length = 120)
	var city: String,

	@Column(nullable = false, length = 120)
	var country: String,

	@Column(length = 2000)
	var coverUrl: String? = null,

	@Column(name = "trip_start", nullable = false)
	var tripStart: Instant,

	@Column(name = "trip_end", nullable = false)
	var tripEnd: Instant,

	@Column(nullable = false, precision = 14, scale = 2)
	var budget: BigDecimal,

	@Column(length = 4000)
	var notes: String? = null,

	@OneToMany(mappedBy = "trip", cascade = [CascadeType.ALL], orphanRemoval = true)
	val expenses: MutableList<Expense> = mutableListOf(),

	@OneToMany(mappedBy = "trip", cascade = [CascadeType.ALL], orphanRemoval = true)
	val destinations: MutableList<Destination> = mutableListOf(),

	@OneToMany(mappedBy = "trip", cascade = [CascadeType.ALL], orphanRemoval = true)
	val documents: MutableList<TripDocument> = mutableListOf(),
) {

	private fun ensureDestinationInsideTrip(stayStart: Instant, stayEnd: Instant) {
		if (stayEnd.isBefore(stayStart)) {
			throw BusinessRuleException("A data fim do destino deve ser igual ou posterior ao início.")
		}
		if (stayStart.isBefore(tripStart) || stayEnd.isAfter(tripEnd)) {
			throw BusinessRuleException(
				"O período do destino deve estar contido no período da viagem ($tripStart — $tripEnd).",
			)
		}
	}

	fun addExpense(
		description: String,
		amount: BigDecimal,
		category: String,
		expenseDate: Instant,
	): Expense {
		val e = Expense(
			trip = this,
			description = description,
			amount = amount,
			category = category,
			expenseDate = expenseDate,
		)
		expenses.add(e)
		return e
	}

	fun removeExpense(expense: Expense) {
		expenses.remove(expense)
	}

	fun addDestination(
		city: String,
		stayStart: Instant,
		stayEnd: Instant,
		notes: String?,
	): Destination {
		ensureDestinationInsideTrip(stayStart, stayEnd)
		val d = Destination(
			trip = this,
			city = city.trim(),
			stayStart = stayStart,
			stayEnd = stayEnd,
			notes = notes?.trim(),
		)
		destinations.add(d)
		return d
	}

	fun removeDestination(destination: Destination) {
		destinations.remove(destination)
	}

	fun updateDestination(
		destination: Destination,
		city: String,
		stayStart: Instant,
		stayEnd: Instant,
		notes: String?,
	) {
		ensureDestinationInsideTrip(stayStart, stayEnd)
		destination.city = city.trim()
		destination.stayStart = stayStart
		destination.stayEnd = stayEnd
		destination.notes = notes?.trim()
	}

	fun addDocument(
		name: String,
		type: DocumentType,
		uri: String?,
		notes: String?,
	): TripDocument {
		val doc = TripDocument(
			trip = this,
			name = name.trim(),
			type = type,
			uri = uri?.trim()?.takeIf { it.isNotEmpty() },
			notes = notes?.trim(),
		)
		documents.add(doc)
		return doc
	}

	fun removeDocument(document: TripDocument) {
		documents.remove(document)
	}

	fun updateDocument(
		document: TripDocument,
		name: String,
		type: DocumentType,
		uri: String?,
		notes: String?,
	) {
		document.name = name.trim()
		document.type = type
		document.uri = uri?.trim()?.takeIf { it.isNotEmpty() }
		document.notes = notes?.trim()
	}
}
