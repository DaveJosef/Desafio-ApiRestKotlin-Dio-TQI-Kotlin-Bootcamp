package me.dio.credit.application.system.exception

class BusinessException(override val message: String?) : RuntimeException(message)
