package br.pucpr.authserver.security

import jakarta.servlet.FilterChain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.http.HttpHeaders
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.security.core.context.SecurityContextHolder
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@ExtendWith(MockitoExtension::class)
class JwtAuthenticationFilterTest {

	@Mock
	private lateinit var jwtTokenProvider: JwtTokenProvider

	@Mock
	private lateinit var filterChain: FilterChain

	private lateinit var filter: JwtAuthenticationFilter

	@BeforeEach
	fun setup() {
		SecurityContextHolder.clearContext()
		filter = JwtAuthenticationFilter(jwtTokenProvider)
	}

	@AfterEach
	fun tearDown() {
		SecurityContextHolder.clearContext()
	}

	@Test
	fun `com Bearer valido define autenticacao no contexto`() {
		val token = "valid.jwt.here"
		whenever(jwtTokenProvider.validateToken(token)).thenReturn(true)
		whenever(jwtTokenProvider.getUsernameFromToken(token)).thenReturn("admin")
		whenever(jwtTokenProvider.getAuthorities(token)).thenReturn(listOf("ROLE_STAFF"))

		val request = MockHttpServletRequest()
		request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer $token")
		val response = MockHttpServletResponse()

		filter.doFilter(request, response, filterChain)

		verify(filterChain).doFilter(request, response)
		val auth = SecurityContextHolder.getContext().authentication
		assertNotNull(auth)
		assertEquals("admin", auth.name)
		assertEquals(1, auth.authorities.size)
		assertEquals("ROLE_STAFF", auth.authorities.first().authority)
	}

	@Test
	fun `sem header Authorization nao define autenticacao`() {
		val request = MockHttpServletRequest()
		val response = MockHttpServletResponse()

		filter.doFilter(request, response, filterChain)

		verify(filterChain).doFilter(request, response)
		assertNull(SecurityContextHolder.getContext().authentication)
	}

	@Test
	fun `Bearer com token invalido nao define autenticacao`() {
		whenever(jwtTokenProvider.validateToken("bad")).thenReturn(false)

		val request = MockHttpServletRequest()
		request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer bad")
		val response = MockHttpServletResponse()

		filter.doFilter(request, response, filterChain)

		verify(filterChain).doFilter(request, response)
		assertNull(SecurityContextHolder.getContext().authentication)
		verify(jwtTokenProvider, never()).getUsernameFromToken(any())
	}

	@Test
	fun `Authorization Basic e ignorado`() {
		val request = MockHttpServletRequest()
		request.addHeader(HttpHeaders.AUTHORIZATION, "Basic YWRtaW46YWRtaW4=")
		val response = MockHttpServletResponse()

		filter.doFilter(request, response, filterChain)

		verify(jwtTokenProvider, never()).validateToken(any())
		assertNull(SecurityContextHolder.getContext().authentication)
	}
}
