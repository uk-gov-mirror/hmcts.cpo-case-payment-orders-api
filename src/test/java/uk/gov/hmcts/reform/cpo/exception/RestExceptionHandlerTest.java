package uk.gov.hmcts.reform.cpo.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import uk.gov.hmcts.reform.BaseTest;
import uk.gov.hmcts.reform.TestIdamConfiguration;
import uk.gov.hmcts.reform.cpo.ApplicationParams;
import uk.gov.hmcts.reform.cpo.config.SecurityConfiguration;
import uk.gov.hmcts.reform.cpo.controllers.CasePaymentOrdersController;
import uk.gov.hmcts.reform.cpo.payload.UpdateCasePaymentOrderRequest;
import uk.gov.hmcts.reform.cpo.security.JwtGrantedAuthoritiesConverter;
import uk.gov.hmcts.reform.cpo.service.impl.CasePaymentOrdersServiceImpl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.cpo.controllers.CasePaymentOrdersController.CASE_PAYMENT_ORDERS_PATH;


@WebMvcTest(controllers = CasePaymentOrdersController.class,
    includeFilters = @ComponentScan.Filter(type = ASSIGNABLE_TYPE, classes = MapperConfig.class),
    excludeFilters = @ComponentScan.Filter(type = ASSIGNABLE_TYPE, classes =
        {SecurityConfiguration.class, JwtGrantedAuthoritiesConverter.class}))
@AutoConfigureMockMvc(addFilters = false)
@ImportAutoConfiguration(TestIdamConfiguration.class)
class RestExceptionHandlerTest implements BaseTest {

    @Autowired
    protected MockMvc mockMvc;

    @MockBean
    protected CasePaymentOrdersServiceImpl service;

    @MockBean
    protected ApplicationParams applicationParams;


    @Autowired
    protected ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @DisplayName("should return correct response when CaseIdOrderReferenceUniqueConstraint is thrown")
    @Test
    void shouldReturnCaseIdOrderReferenceUniqueConstraintResponse() throws Exception {

        // GIVEN
        UpdateCasePaymentOrderRequest request = createUpdateCasePaymentOrderRequest();
        String myUniqueExceptionMessage = "CaseID/OrgRef not unique";
        CaseIdOrderReferenceUniqueConstraintException expectedException =
            new CaseIdOrderReferenceUniqueConstraintException(myUniqueExceptionMessage);

        /// WHEN
        setupMockServiceToThrowException(expectedException);
        ResultActions result =  this.mockMvc.perform(put(CASE_PAYMENT_ORDERS_PATH)
                                                         .contentType(MediaType.APPLICATION_JSON)
                                                         .content(objectMapper.writeValueAsString(request)));

        // THEN
        assertHttpErrorResponse(result, HttpStatus.CONFLICT, expectedException.getMessage());

    }

    @DisplayName("should return correct response when CasePaymentOrderCouldNotBeFound is thrown")
    @Test
    void shouldReturnCasePaymentOrderCouldNotBeFoundResponse() throws Exception {

        // GIVEN
        UpdateCasePaymentOrderRequest request = createUpdateCasePaymentOrderRequest();
        String myUniqueExceptionMessage = "CPO could not be found";
        CasePaymentOrderCouldNotBeFoundException expectedException =
            new CasePaymentOrderCouldNotBeFoundException(myUniqueExceptionMessage);

        /// WHEN
        setupMockServiceToThrowException(expectedException);
        ResultActions result =  this.mockMvc.perform(put(CASE_PAYMENT_ORDERS_PATH)
                                                         .contentType(MediaType.APPLICATION_JSON)
                                                         .content(objectMapper.writeValueAsString(request)));

        // THEN
        assertHttpErrorResponse(result, HttpStatus.NOT_FOUND, expectedException.getMessage());

    }

    private void setupMockServiceToThrowException(Exception expectedException) {
        // configure chosen mock service to throw exception when controller is run
        given(service.updateCasePaymentOrder(any(UpdateCasePaymentOrderRequest.class))).willThrow(expectedException);
    }

    private void assertHttpErrorResponse(ResultActions result,
                                         HttpStatus expectedStatus,
                                         String expectedMessage) throws Exception {

        result
            .andExpect(status().is(expectedStatus.value()))
            .andExpect(jsonPath("$.status").value(expectedStatus.value()))
            .andExpect(jsonPath("$.error").value(expectedStatus.getReasonPhrase()))
            .andExpect(jsonPath("$.message").value(expectedMessage));
    }

}
