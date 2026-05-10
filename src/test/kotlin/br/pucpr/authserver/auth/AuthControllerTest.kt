package br.pucpr.authserver.auth

import br.pucpr.authserver.security.JwtTokenProvider
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import org.springframework.http.MediaType
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean

@ExtendWith(MockitoExtension::class)
class AuthControllerTest {

	@Mock
	private lateinit var authenticationManager: AuthenticationManager

	@Mock
	private lateinit var jwtTokenProvider: JwtTokenProvider

	private lateinit var mockMvc: MockMvc

	@BeforeEach
	fun setup() {
		val validator = LocalValidatorFactoryBean().apply { afterPropertiesSet() }
		mockMvc = MockMvcBuilders
			.standaloneSetup(AuthController(authenticationManager, jwtTokenProvider))
			.setValidator(validator)
			.build()
	}

	@Test
	fun `login retorna 200 e corpo com token quando autenticacao ok`() {
		whenever(authenticationManager.authenticate(any())).thenReturn(
			UsernamePasswordAuthenticationToken(
				"admin",
				null,
				listOf(SimpleGrantedAuthority("ROLE_STAFF")),
			),
		)
		whenever(jwtTokenProvider.generateToken(eq("admin"), any())).thenReturn("signed-jwt")
		whenever(jwtTokenProvider.expirationSeconds()).thenReturn(86400L)

		mockMvc.perform(
			post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""{"username":"admin","password":"admin"}"""),
		)
			.andExpect(status().isOk)
			.andExpect(jsonPath("$.accessToken").value("signed-jwt"))
			.andExpect(jsonPath("$.tokenType").value("Bearer"))
			.andExpect(jsonPath("$.expiresIn").value(86400))
	}

	@Test
	fun `login retorna 401 quando AuthenticationManager falha`() {
		whenever(authenticationManager.authenticate(any())).thenThrow(BadCredentialsException("bad"))

		mockMvc.perform(
			post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""{"username":"admin","password":"wrong"}"""),
		)
			.andExpect(status().isUnauthorized)
	}

	@Test
	fun `login retorna 400 quando username em branco`() {
		mockMvc.perform(
			post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""{"username":"   ","password":"admin"}"""),
		)
			.andExpect(status().isBadRequest)
	}
}
