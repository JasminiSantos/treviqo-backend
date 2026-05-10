package br.pucpr.authserver.treviqo.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@Table(name = "trip_documents")
class TripDocument(
	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	var id: Long? = null,

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "trip_id", nullable = false)
	var trip: Trip,

	@Column(nullable = false, length = 200)
	var name: String,

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 32)
	var type: DocumentType,

	@Column(length = 2000)
	var uri: String? = null,

	@Column(length = 4000)
	var notes: String? = null,
)
