package me.dio.credit.application.system.controller

import jakarta.validation.Valid
import me.dio.credit.application.system.dto.request.CustomerDTO
import me.dio.credit.application.system.dto.request.CustomerUpdateDTO
import me.dio.credit.application.system.dto.response.CustomerView
import me.dio.credit.application.system.entity.Customer
import me.dio.credit.application.system.service.impl.CustomerService
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn
import org.springframework.hateoas.server.mvc.linkTo
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/customers")
class CustomerResource (
    private val customerService: CustomerService
) {

        @PostMapping
        fun saveCustomer(@RequestBody @Valid customerDTO: CustomerDTO): ResponseEntity<CustomerView> {
            val savedCustomer = this.customerService.save(customerDTO.toEntity());
            val hateoasResponse: CustomerView = CustomerView(savedCustomer)
            hateoasResponse.add(linkTo<CustomerResource> { methodOn(CustomerResource::class.java).findById(savedCustomer.id!!) }.withRel("Find Customer by Id"))
            hateoasResponse.add(linkTo<CustomerResource> { methodOn(CustomerResource::class.java).updateCustomer(savedCustomer.id!!, CustomerUpdateDTO(
                firstName = savedCustomer.firstName,
                lastName = savedCustomer.lastName,
                income = savedCustomer.income,
                zipCode = savedCustomer.address.zipCode,
                street = savedCustomer.address.street))
            }.withRel("Update Customer"))
            return ResponseEntity.status(HttpStatus.CREATED).body(hateoasResponse)
        }

        @GetMapping("/{id}")
        fun findById(@PathVariable id: Long): ResponseEntity<CustomerView> {
            val customer: Customer = this.customerService.findById(id)
            val hateoasResponse: CustomerView = CustomerView(customer)
            hateoasResponse.add(linkTo<CustomerResource> { methodOn(CustomerResource::class.java).findById(id) }.withSelfRel())
            return ResponseEntity.status(HttpStatus.OK).body(hateoasResponse)
        }

        @ResponseStatus(HttpStatus.NO_CONTENT)
        @DeleteMapping("/{id}")
        fun delete(@PathVariable id: Long) = this.customerService.delete(id)

        @PatchMapping
        fun updateCustomer(
                @RequestParam(value = "customerId") id: Long,
                @RequestBody customerUpdateDTO: CustomerUpdateDTO
        ) : ResponseEntity<CustomerView> {
            val customer: Customer = this.customerService.findById(id);
            val customerToUpdate: Customer = customerUpdateDTO.toEntity(customer)
            val customerUpdated: Customer = this.customerService.save(customerToUpdate)
            val hateoasResponse: CustomerView = CustomerView(customerUpdated)
            hateoasResponse.add(linkTo<CustomerResource> { methodOn(CustomerResource::class.java).updateCustomer(id, customerUpdateDTO) }.withSelfRel())
            hateoasResponse.add(linkTo<CustomerResource> { methodOn(CustomerResource::class.java).findById(id) }.withRel("Find Customer by Id"))
            return ResponseEntity.status(HttpStatus.OK).body(hateoasResponse)
        }
}