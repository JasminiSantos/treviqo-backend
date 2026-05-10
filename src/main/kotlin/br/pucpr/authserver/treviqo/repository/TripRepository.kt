package br.pucpr.authserver.treviqo.repository

import br.pucpr.authserver.treviqo.domain.Trip
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor

interface TripRepository : JpaRepository<Trip, Long>, JpaSpecificationExecutor<Trip>
