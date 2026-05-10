package br.pucpr.authserver.auth

import br.pucpr.authserver.security.JwtTokenProvider
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.AuthenticationException
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
@Tag(name = "auth-controller")
class AuthController(
	private val authenticationManager: AuthenticationManager,
	private val jwtTokenProvider: JwtTokenProvider,
) {

	@PostMapping("/login")
	@Operation(summary = "Login", description = "Credenciais válidas retornam JWT (Bearer) para uso no header Authorization.")
	fun login(@Valid @RequestBody body: LoginRequest): ResponseEntity<LoginResponse> =
		try {
			val auth = authenticationManager.authenticate(
				UsernamePasswordAuthenticationToken(body.username.trim(), body.password),
			)
			val token = jwtTokenProvider.generateToken(auth.name, auth.authorities)
			ResponseEntity.ok(
				LoginResponse(
					accessToken = token,
					expiresIn = jwtTokenProvider.expirationSeconds(),
				),
			)
		} catch (_: AuthenticationException) {
			ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
		}
}
