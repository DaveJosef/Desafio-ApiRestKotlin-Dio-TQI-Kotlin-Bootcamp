package me.dio.credit.application.system.controller

import jakarta.validation.Valid
import me.dio.credit.application.system.dto.request.CreditDTO
import me.dio.credit.application.system.dto.response.CreditView
import me.dio.credit.application.system.dto.response.CreditViewList
import me.dio.credit.application.system.entity.Credit
import me.dio.credit.application.system.service.impl.CreditService
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn
import org.springframework.hateoas.server.mvc.linkTo
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID
import java.util.stream.Collectors

@RestController
@RequestMapping("/api/credits")
class CreditResource(
    private val creditService: CreditService
) {

    @PostMapping
    fun saveCredit(@RequestBody @Valid creditDTO: CreditDTO): ResponseEntity<String> {
        val credit: Credit = this.creditService.save(creditDTO.toEntity())
        return ResponseEntity.status(HttpStatus.CREATED)
            .body("Credit ${credit.creditCode} - customer ${credit.customer?.email} saved!")
    }

    @GetMapping
    fun findAllByCustomerId(@RequestParam(value = "customerId") customerId: Long): ResponseEntity<List<CreditViewList>> {
        val creditViewList: List<CreditViewList> = this.creditService.findAllByCustomer(customerId)
            .stream()
            .map { credit: Credit -> CreditViewList(credit) }
            .map { creditViewList: CreditViewList -> creditViewList.add(linkTo<CreditResource> { methodOn(CreditResource::class.java).findAllByCustomerId(customerId) }.withSelfRel()) }
            .map { creditViewList: CreditViewList -> creditViewList.add(linkTo<CreditResource> { methodOn(CreditResource::class.java).findByCreditCode(customerId, creditViewList.creditCode) }.withRel("Find Credit by creditCode")) }
            .collect(Collectors.toList())
        return ResponseEntity.status(HttpStatus.OK).body(creditViewList)
    }

    @GetMapping("/{creditCode}")
    fun findByCreditCode(
        @RequestParam(value = "customerId") customerId: Long,
        @PathVariable creditCode: UUID
    ): ResponseEntity<CreditView> {
        val credit: Credit = this.creditService.findByCreditCode(customerId, creditCode)
        val hateoasResponse: CreditView = CreditView(credit)
        hateoasResponse.add(linkTo<CreditResource> { methodOn(CreditResource::class.java).findByCreditCode(customerId, creditCode) }.withSelfRel())
        hateoasResponse.add(linkTo<CreditResource> { methodOn(CreditResource::class.java).findAllByCustomerId(customerId) }.withRel("Find All by customerId"))
        return ResponseEntity.status(HttpStatus.OK).body(hateoasResponse)
    }
}