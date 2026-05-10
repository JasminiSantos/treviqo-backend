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
import java.time.Instant

@Entity
@Table(name = "destinations")
class Destination(
	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	var id: Long? = null,

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "trip_id", nullable = false)
	var trip: Trip,

	@Column(nullable = false, length = 120)
	var city: String,

	@Column(name = "stay_start", nullable = false)
	var stayStart: Instant,

	@Column(name = "stay_end", nullable = false)
	var stayEnd: Instant,

	@Column(length = 4000)
	var notes: String? = null,
)
