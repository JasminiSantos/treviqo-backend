package br.pucpr.authserver.security

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.provisioning.InMemoryUserDetailsManager
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.HttpStatusEntryPoint
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
class SecurityConfig {

	@Bean
	fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

	@Bean
	fun userDetailsService(encoder: PasswordEncoder): UserDetailsService {
		val admin = User.builder()
			.username("admin")
			.password(encoder.encode("admin"))
			.roles("STAFF")
			.build()
		return InMemoryUserDetailsManager(admin)
	}

	@Bean
	fun authenticationManager(configuration: AuthenticationConfiguration): AuthenticationManager =
		configuration.authenticationManager

	@Bean
	fun corsConfigurationSource(): CorsConfigurationSource {
		val c = CorsConfiguration()
		c.allowedOriginPatterns = listOf("*")
		c.allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
		c.allowedHeaders = listOf("*")
		c.exposedHeaders = listOf("Authorization")
		val source = UrlBasedCorsConfigurationSource()
		source.registerCorsConfiguration("/**", c)
		return source
	}

	@Bean
	fun securityFilterChain(
		http: HttpSecurity,
		jwtAuthenticationFilter: JwtAuthenticationFilter,
		corsConfigurationSource: CorsConfigurationSource,
	): SecurityFilterChain {
		http
			.csrf { it.disable() }
			.cors { it.configurationSource(corsConfigurationSource) }
			.sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
			.exceptionHandling {
				it.authenticationEntryPoint(HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
			}
			.authorizeHttpRequests { auth ->
				auth.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
				auth.requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
				auth.requestMatchers(
					"/swagger-ui/**",
					"/swagger-ui.html",
					"/v3/api-docs",
					"/v3/api-docs/**",
					"/v3/api-docs.yaml",
					"/webjars/**",
				).permitAll()
				auth.requestMatchers(HttpMethod.GET, "/api/trips/**").permitAll()
				auth.requestMatchers(
					HttpMethod.POST,
					"/api/trips",
					"/api/trips/*/expenses",
					"/api/trips/*/destinations",
					"/api/trips/*/documents",
				).authenticated()
				auth.requestMatchers(HttpMethod.DELETE, "/api/trips/*/expenses/*").authenticated()
				auth.requestMatchers(HttpMethod.PUT, "/api/trips/**").authenticated()
				auth.requestMatchers(HttpMethod.DELETE, "/api/trips/**").authenticated()
				auth.anyRequest().denyAll()
			}
			.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)
		return http.build()
	}
}
