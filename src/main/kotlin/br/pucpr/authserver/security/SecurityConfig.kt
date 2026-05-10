package br.pucpr.authserver.security

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.provisioning.InMemoryUserDetailsManager
import org.springframework.security.web.SecurityFilterChain

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
	fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
		http
			.csrf { it.disable() }
			.sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
			.authorizeHttpRequests { auth ->
				auth.requestMatchers(
					"/swagger-ui/**",
					"/swagger-ui.html",
					// "/v3/api-docs/**" não casa com o path exato "/v3/api-docs" em algumas versões
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
			.httpBasic(Customizer.withDefaults())
		return http.build()
	}
}
