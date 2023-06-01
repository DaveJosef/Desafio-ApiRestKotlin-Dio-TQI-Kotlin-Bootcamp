package me.dio.credit.application.system.dto.response

import me.dio.credit.application.system.entity.Customer
import org.springframework.hateoas.RepresentationModel
import java.math.BigDecimal

class CustomerView(
    val firstName: String,
    val lastName: String,
    val cpf: String,
    val income: BigDecimal,
    val email: String,
    val zipCode: String,
    val street: String,
    val id: Long?
) : RepresentationModel<CustomerView>() {

    constructor(customer: Customer): this(
        firstName = customer.firstName,
        lastName = customer.lastName,
        cpf = customer.cpf,
        income = customer.income,
        email = customer.email,
        zipCode = customer.address.zipCode,
        street = customer.address.street,
        id = customer.id,
    )
}