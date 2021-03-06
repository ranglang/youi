package io.youi

import io.youi.http.HttpStatus

object ValidationError {
  val General: Int = 0
  val RequestParsing: Int = 1
  val Internal: Int = 2
}

case class ValidationError(message: String,
                           code: Int = ValidationError.General,
                           status: HttpStatus = HttpStatus.InternalServerError)