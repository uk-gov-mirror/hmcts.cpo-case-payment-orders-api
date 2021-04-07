package uk.gov.hmcts.reform.cpo.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.cpo.BaseTest;
import uk.gov.hmcts.reform.cpo.exception.ValidationError;
import uk.gov.hmcts.reform.cpo.payload.CreateCasePaymentOrderRequest;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.is;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;


public class CasePaymentOrdersControllerIT {

    private static final LocalDateTime EFFECTIVE_FROM = LocalDateTime.of(2021, Month.MARCH, 24,
                                                                         11, 48, 32
    );
    private static final Long CASE_ID = 6_551_341_964_128_977L;
    private static final String ACTION = "action";
    private static final String RESPONSIBLE_PARTY = "responsibleParty";
    private static final String ORDER_REFERENCE = "2021-11223344556";

    @Nested
    @DisplayName("POST /case-payment-orders")
    class CreateCasePaymentOrder extends BaseTest {

        private CreateCasePaymentOrderRequest createCasePaymentOrderRequest;

        private CreateCasePaymentOrderRequest createCasePaymentOrderRequestInvalid;

        private CreateCasePaymentOrderRequest createCasePaymentOrderRequestNull;

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @BeforeEach
        void setUp() {

            createCasePaymentOrderRequest = new CreateCasePaymentOrderRequest(EFFECTIVE_FROM, "6551341964128977",
                                                                              ACTION, RESPONSIBLE_PARTY,
                                                                              ORDER_REFERENCE
            );
            createCasePaymentOrderRequestNull = new CreateCasePaymentOrderRequest(null, null,
                                                                                  null, null,
                                                                                  null
            );

            createCasePaymentOrderRequestInvalid = new CreateCasePaymentOrderRequest(EFFECTIVE_FROM,
                                                                                     "655111964128977",
                                                                                     ACTION, RESPONSIBLE_PARTY,
                                                                                     "2021-918425346"
            );

        }

        @DisplayName("Successfully created CasePaymentOrder")
        @Test
        void shouldSuccessfullyCreateCasePaymentOrder() throws Exception {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
            this.mockMvc.perform(post("/api/case-payment-orders")
                                     .contentType(MediaType.APPLICATION_JSON)
                                     .content(objectMapper.writeValueAsString(createCasePaymentOrderRequest)))
                .andExpect(jsonPath("$.created_timestamp", is(LocalDateTime.now().format(formatter))))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.case_id", is(CASE_ID)))
                .andExpect(jsonPath("$.action", is(ACTION)))
                .andExpect(jsonPath("$.responsible_party", is(RESPONSIBLE_PARTY)))
                .andExpect(jsonPath("$.order_reference", is(ORDER_REFERENCE)))
                .andExpect(jsonPath("$.effective_from", is(EFFECTIVE_FROM.format(formatter))))
                .andExpect(jsonPath("$.created_by", is("445")));
        }

        @DisplayName("Null request fields throws errors")
        @Test
        void shouldThrowNotNullErrors() throws Exception {
            this.mockMvc.perform(post("/api/case-payment-orders")
                                     .contentType(MediaType.APPLICATION_JSON)
                                     .content(objectMapper.writeValueAsString(createCasePaymentOrderRequestNull)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.errors.length()", is(5)))
                .andExpect(jsonPath("$.errors", hasItem(ValidationError.ACTION_EMPTY)))
                .andExpect(jsonPath("$.errors", hasItem(ValidationError.ORDER_REFERENCE_EMPTY)))
                .andExpect(jsonPath("$.errors", hasItem(ValidationError.CASE_ID_EMPTY)))
                .andExpect(jsonPath("$.errors", hasItem(ValidationError.EFFECTIVE_FROM_EMPTY)))
                .andExpect(jsonPath("$.errors", hasItem(ValidationError.RESPONSIBLE_PARTY_EMPTY)));
        }

        @DisplayName("Invalid request fields throws errors")
        @Test
        void shouldThrowInvalidFormErrors() throws Exception {
            this.mockMvc.perform(post("/api/case-payment-orders")
                                     .contentType(MediaType.APPLICATION_JSON)
                                     .content(objectMapper.writeValueAsString(createCasePaymentOrderRequestInvalid)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.errors.length()", is(3)))
                .andExpect(jsonPath("$.errors", hasItem(ValidationError.CASE_ID_INVALID_LENGTH)))
                .andExpect(jsonPath("$.errors", hasItem(ValidationError.CASE_ID_INVALID)))
                .andExpect(jsonPath("$.errors", hasItem(ValidationError.ORDER_REFERENCE_INVALID)));
        }
    }
}
