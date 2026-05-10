package br.pucpr.authserver.security

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.GrantedAuthority
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets
import java.util.Date
import javax.crypto.SecretKey

/**
 * Codifica (emissão no login) e decodifica/valida o JWT usado nas requisições autenticadas.
 */
@Component
class JwtTokenProvider(
	@Value("\${app.jwt.secret}")
	private val secret: String,
	@Value("\${app.jwt.expiration-ms}")
	private val expirationMs: Long,
) {

	private val key: SecretKey by lazy {
		val bytes = secret.toByteArray(StandardCharsets.UTF_8)
		require(bytes.size >= 32) {
			"app.jwt.secret deve ter pelo menos 32 bytes em UTF-8 (HS256)."
		}
		Keys.hmacShaKeyFor(bytes)
	}

	fun generateToken(username: String, authorities: Collection<GrantedAuthority>): String {
		val now = Date()
		val expiry = Date(now.time + expirationMs)
		val roles = authorities.map { it.authority }
		return Jwts.builder()
			.subject(username)
			.claim("roles", roles)
			.issuedAt(now)
			.expiration(expiry)
			.signWith(key)
			.compact()
	}

	fun getUsernameFromToken(token: String): String? =
		parseClaims(token)?.subject

	fun validateToken(token: String): Boolean =
		try {
			parseClaims(token) != null
		} catch (_: Exception) {
			false
		}

	fun getAuthorities(token: String): List<String> {
		val claims = parseClaims(token) ?: return emptyList()
		@Suppress("UNCHECKED_CAST")
		return (claims["roles"] as? List<String>) ?: emptyList()
	}

	private fun parseClaims(token: String): Claims? =
		try {
			Jwts.parser()
				.verifyWith(key)
				.build()
				.parseSignedClaims(token)
				.payload
		} catch (_: Exception) {
			null
		}

	fun expirationSeconds(): Long = expirationMs/1000
}
