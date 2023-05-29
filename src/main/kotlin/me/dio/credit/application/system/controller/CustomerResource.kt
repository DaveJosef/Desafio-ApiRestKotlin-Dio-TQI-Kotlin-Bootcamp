package me.dio.credit.application.system.controller

import jakarta.validation.Valid
import me.dio.credit.application.system.dto.request.CustomerDTO
import me.dio.credit.application.system.dto.request.CustomerUpdateDTO
import me.dio.credit.application.system.dto.response.CustomerView
import me.dio.credit.application.system.entity.Customer
import me.dio.credit.application.system.service.impl.CustomerService
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
            return ResponseEntity.status(HttpStatus.CREATED).body(CustomerView(savedCustomer))
        }

        @GetMapping("/{id}")
        fun findById(@PathVariable id: Long): ResponseEntity<CustomerView> {
            val customer: Customer = this.customerService.findById(id)
            return ResponseEntity.status(HttpStatus.OK).body(CustomerView(customer))
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
            return ResponseEntity.status(HttpStatus.OK).body(CustomerView(customerUpdated))
        }
}