package br.pucpr.authserver.auth

data class LoginResponse(
	val accessToken: String,
	val tokenType: String = "Bearer",
	val expiresIn: Long,
)
