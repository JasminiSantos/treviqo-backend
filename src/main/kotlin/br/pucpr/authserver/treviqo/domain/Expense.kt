package br.pucpr.authserver.treviqo.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.math.BigDecimal
import java.time.Instant

/**
 * Despesa registrada em uma viagem (espelha o modelo Treviqo: tripId, description, amount, category, date).
 */
@Entity
@Table(name = "expenses")
class Expense(
	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	var id: Long? = null,

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "trip_id", nullable = false)
	var trip: Trip,

	@Column(nullable = false, length = 500)
	var description: String,

	@Column(nullable = false, precision = 14, scale = 2)
	var amount: BigDecimal,

	@Column(nullable = false, length = 80)
	var category: String,

	@Column(name = "expense_date", nullable = false)
	var expenseDate: Instant,
)
