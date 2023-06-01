package me.dio.credit.application.system.controller

import com.fasterxml.jackson.databind.ObjectMapper
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import me.dio.credit.application.system.dto.request.CreditDTO
import me.dio.credit.application.system.dto.request.CustomerDTO
import me.dio.credit.application.system.dto.request.CustomerUpdateDTO
import me.dio.credit.application.system.entity.Address
import me.dio.credit.application.system.entity.Credit
import me.dio.credit.application.system.entity.Customer
import me.dio.credit.application.system.repository.CreditRepository
import me.dio.credit.application.system.repository.CustomerRepository
import me.dio.credit.application.system.service.impl.CustomerService
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@ContextConfiguration
@ExtendWith(MockKExtension::class)
class CreditResourceTest {

    @MockK
    lateinit var creditRepositoryMocked: CreditRepository

    @Autowired private lateinit var creditRepository: CreditRepository
    @Autowired private lateinit var customerRepository: CustomerRepository

    @Autowired private lateinit var mockMvc: MockMvc

    @Autowired private lateinit var objectMapper: ObjectMapper

    @BeforeEach
    fun setup() {
        creditRepository.deleteAll()
        customerRepository.deleteAll()
    }

    @AfterEach
    fun tearDown() {
        creditRepository.deleteAll()
        customerRepository.deleteAll()
    }

    @Test
    fun `should create credit and return 201 status`() {
        //given
        val customerSaved: Customer = customerRepository.save(buildCustomer())
        val creditDTO: CreditDTO = buildCreditDTO(customerId = customerSaved.id!!)
        val creditCreate = objectMapper.writeValueAsString(creditDTO)
        //when
        //then
        mockMvc.perform(
            MockMvcRequestBuilders.post(URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(creditCreate)
        )
            .andExpect(MockMvcResultMatchers.status().isCreated)
    }

    @Test
    fun `should return 400 status when trying to create credit and customer does not exist`() {
        //given
        //val invalidCustomerId: Long = 2L
        val creditDTO: CreditDTO = buildCreditDTO()
        val creditCreate: String = objectMapper.writeValueAsString(creditDTO)
        //when
        //then
            mockMvc.perform(
                MockMvcRequestBuilders
                    .post(URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(creditCreate)
            )
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(MockMvcResultMatchers.jsonPath("$.title").value("Bad Request! Consult the documentation"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.timestamp").exists())
            .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(400))
            .andExpect(MockMvcResultMatchers.jsonPath("$.exception").value("class me.dio.credit.application.system.exception.BusinessException"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.details[*]").isNotEmpty)
            .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `should find all credits related to given customer id and return 200 status`() {
        //given
        val customerSaved: Customer = customerRepository.save(buildCustomer())
        val creditSaved: Credit = creditRepository.save(buildCreditDTO(customerId = customerSaved.id!!).toEntity())
        //when
        //then
        mockMvc.perform(MockMvcRequestBuilders.get(URL).param("customerId", "${creditSaved.customer?.id}"))
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].creditCode").value("${creditSaved.creditCode}"))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].creditValue").value("2000.0"))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].numberOfInstallments").value("4"))
            .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `should find by creditCode if customer is the proprietary and return 200 status`() {
        //given
        val customerSaved: Customer = customerRepository.save(buildCustomer())
        val creditSaved: Credit = creditRepository.save(buildCreditDTO(customerId = customerSaved.id!!).toEntity())
        //when
        //then
        mockMvc.perform(MockMvcRequestBuilders.get("$URL/${creditSaved.creditCode}").param("customerId", "${creditSaved.customer?.id}"))
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andDo(MockMvcResultHandlers.print())
            .andExpect(MockMvcResultMatchers.jsonPath("$.creditCode").value("${creditSaved.creditCode}"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.creditValue").value("2000.0"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.numberOfInstallment").value("4"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("IN_PROGRESS"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.emailCustomer").value(customerSaved.email))
            .andExpect(MockMvcResultMatchers.jsonPath("$.incomeCustomer").value(customerSaved.income))
    }

    @Test
    fun `should return 400 status when trying to find by creditCode and customer is not the proprietary`() {
        //given
        val invalidCustomerId: Long = 2L
        val customerSaved: Customer = customerRepository.save(buildCustomer())
        val creditSaved: Credit = creditRepository.save(buildCreditDTO(customerId = customerSaved.id!!).toEntity())
        //when
        //then
        mockMvc.perform(
            MockMvcRequestBuilders
                .get("$URL/${creditSaved.creditCode}").param("customerId", "$invalidCustomerId")
        )
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(MockMvcResultMatchers.jsonPath("$.title").value("Bad Request! Consult the documentation"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.timestamp").exists())
            .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(400))
            .andExpect(MockMvcResultMatchers.jsonPath("$.exception").value("class java.lang.IllegalArgumentException"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.details[*]").isNotEmpty)
            .andDo(MockMvcResultHandlers.print())
    }

    @Test
    fun `should return 400 status when trying to find by creditCode and credit does not exist`() {
        //given
        val invalidCreditCode: UUID = UUID.randomUUID()
        val customerSaved: Customer = customerRepository.save(buildCustomer())
        //when
        //then
        mockMvc.perform(
            MockMvcRequestBuilders
                .get("$URL/${invalidCreditCode}").param("customerId", "${customerSaved.id}")
        )
            .andExpect(MockMvcResultMatchers.status().isBadRequest)
            .andExpect(MockMvcResultMatchers.jsonPath("$.title").value("Bad Request! Consult the documentation"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.timestamp").exists())
            .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(400))
            .andExpect(MockMvcResultMatchers.jsonPath("$.exception").value("class me.dio.credit.application.system.exception.BusinessException"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.details[*]").isNotEmpty)
            .andDo(MockMvcResultHandlers.print())
    }

    private fun buildCreditDTO(
        creditValue: BigDecimal = BigDecimal.valueOf(2000.0),
        dayOfFirstInstallment: LocalDate = LocalDate.now().plusMonths(2),
        numberOfInstallments: Int = 4,
        customerId: Long = 1L
    ) = CreditDTO(
        creditValue = creditValue,
        dayFirstOfInstallment = dayOfFirstInstallment,
        numberOfInstallments = numberOfInstallments,
        customerId = customerId
    )

    companion object {
        const val URL: String = "/api/credits"
        fun buildCustomer(
            firstName: String = "Cami",
            lastName: String = "Cavalcante",
            cpf: String = "28475934625",
            email: String = "david@gmail.com",
            password: String = "12345",
            zipCode: String = "12345",
            street: String = "Rua da Cami",
            income: BigDecimal = BigDecimal.valueOf(1000.0),
            id: Long = 1L
        ) = Customer(
            firstName = firstName,
            lastName = lastName,
            cpf = cpf,
            email = email,
            password = password,
            address = Address(
                zipCode = zipCode,
                street = street,
            ),
            income = income,
            id = id
        )
    }
}