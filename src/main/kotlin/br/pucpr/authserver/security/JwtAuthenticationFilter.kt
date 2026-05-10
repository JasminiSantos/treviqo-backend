package br.pucpr.authserver.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpHeaders
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

/**
 * Extrai o header `Authorization: Bearer &lt;token&gt;`, valida o JWT e preenche o contexto de segurança.
 */
@Component
class JwtAuthenticationFilter(
	private val jwtTokenProvider: JwtTokenProvider,
) : OncePerRequestFilter() {

	override fun doFilterInternal(
		request: HttpServletRequest,
		response: HttpServletResponse,
		filterChain: FilterChain,
	) {
		val header = request.getHeader(HttpHeaders.AUTHORIZATION)
		if (header != null && header.startsWith(BEARER_PREFIX, ignoreCase = true)) {
			val token = header.substring(BEARER_PREFIX.length).trim()
			if (token.isNotEmpty() && jwtTokenProvider.validateToken(token)) {
				val username = jwtTokenProvider.getUsernameFromToken(token) ?: ""
				val authorities = jwtTokenProvider.getAuthorities(token).map { SimpleGrantedAuthority(it) }
				val auth = UsernamePasswordAuthenticationToken(username, null, authorities)
				auth.details = WebAuthenticationDetailsSource().buildDetails(request)
				SecurityContextHolder.getContext().authentication = auth
			}
		}
		filterChain.doFilter(request, response)
	}

	private companion object {
		const val BEARER_PREFIX = "Bearer "
	}
}
