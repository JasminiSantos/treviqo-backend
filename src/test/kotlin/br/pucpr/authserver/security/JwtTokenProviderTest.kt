package br.pucpr.authserver.security

import org.junit.jupiter.api.Test
import org.springframework.security.core.authority.SimpleGrantedAuthority
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class JwtTokenProviderTest {

	private val secret = "unit-test-secret-32-chars-min!!!"
	private val expirationMs = 3_600_000L

	private fun provider() = JwtTokenProvider(secret, expirationMs)

	@Test
	fun `generateToken depois validateToken e username e roles`() {
		val jwt = provider()
		val authorities = listOf(SimpleGrantedAuthority("ROLE_STAFF"))
		val token = jwt.generateToken("admin", authorities)

		assertTrue(jwt.validateToken(token))
		assertEquals("admin", jwt.getUsernameFromToken(token))
		assertEquals(listOf("ROLE_STAFF"), jwt.getAuthorities(token))
	}

	@Test
	fun `validateToken retorna false para string invalida`() {
		val jwt = provider()
		assertFalse(jwt.validateToken("not-a-jwt"))
	}

	@Test
	fun `validateToken retorna false quando assinatura foi alterada`() {
		val jwt = provider()
		val token = jwt.generateToken("admin", listOf(SimpleGrantedAuthority("ROLE_STAFF")))
		val tampered = token.dropLast(1) + (if (token.last() == 'a') 'b' else 'a')
		assertFalse(jwt.validateToken(tampered))
	}

	@Test
	fun `token de outro secret nao valida`() {
		val token = JwtTokenProvider("other-secret-32-chars-different!!", expirationMs)
			.generateToken("admin", listOf(SimpleGrantedAuthority("ROLE_STAFF")))
		assertFalse(provider().validateToken(token))
	}

	@Test
	fun `expirationSeconds reflete configuracao`() {
		assertEquals(3600L, provider().expirationSeconds())
	}

	@Test
	fun `getAuthorities retorna lista vazia para token invalido`() {
		assertEquals(emptyList(), provider().getAuthorities("x.y.z"))
	}

	@Test
	fun `getUsernameFromToken retorna null para token invalido`() {
		assertNull(provider().getUsernameFromToken("invalid"))
	}
}
