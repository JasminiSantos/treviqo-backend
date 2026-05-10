package br.pucpr.authserver.exception

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class ApiExceptionHandler {

	private val log = LoggerFactory.getLogger(javaClass)

	@ExceptionHandler(ResourceNotFoundException::class)
	fun handleNotFound(ex: ResourceNotFoundException): ResponseEntity<ProblemDetail> {
		log.warn("Recurso não encontrado: {}", ex.message)
		val body = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.message ?: "Não encontrado")
		body.title = "Recurso não encontrado"
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body)
	}

	@ExceptionHandler(BusinessRuleException::class)
	fun handleBusiness(ex: BusinessRuleException): ResponseEntity<ProblemDetail> {
		log.warn("Regra de negócio violada: {}", ex.message)
		val body = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.message ?: "Requisição inválida")
		body.title = "Regra de negócio"
		return ResponseEntity.badRequest().body(body)
	}

	@ExceptionHandler(MethodArgumentNotValidException::class)
	fun handleValidation(ex: MethodArgumentNotValidException): ResponseEntity<ProblemDetail> {
		val msg = ex.bindingResult.fieldErrors.joinToString("; ") { "${it.field}: ${it.defaultMessage}" }
		log.warn("Validação falhou: {}", msg)
		val body = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, msg)
		body.title = "Validação"
		return ResponseEntity.badRequest().body(body)
	}

	@ExceptionHandler(Exception::class)
	fun handleGeneric(ex: Exception): ResponseEntity<ProblemDetail> {
		log.error("Erro não tratado", ex)
		val body = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno")
		body.title = "Erro interno"
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body)
	}
}
