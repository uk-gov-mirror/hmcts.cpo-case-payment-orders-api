package uk.gov.hmcts.reform.cpo.payload;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmcts.reform.cpo.validators.Validator;
import uk.gov.hmcts.reform.cpo.validators.annotation.ValidCaseId;
import uk.gov.hmcts.reform.cpo.validators.annotation.ValidCpoId;
import uk.gov.hmcts.reform.cpo.validators.ValidationError;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@AllArgsConstructor
@ApiModel("Update Case Payment Order Request")
public class UpdateCasePaymentOrderRequest {

    @NotEmpty(message = ValidationError.ID_REQUIRED)
    @ValidCpoId
    @ApiModelProperty(value = "Case Payment Order ID to update", required = true,
        example = "77d30e7f-ead9-4529-a499-6bf8b0f2d08e")
    private String id;

    @NotNull(message = ValidationError.EFFECTIVE_FROM_REQUIRED)
    @ApiModelProperty(value = "The date/time from which the record is valid", required = true,
        example = "2021-02-10T03:02:30Z")
    private LocalDateTime effectiveFrom;

    @NotEmpty(message = ValidationError.CASE_ID_REQUIRED)
    @ValidCaseId
    @ApiModelProperty(value = "Case Id for which the record applies", required = true, example = "2061729969689088")
    private String caseId;

    @NotEmpty(message = ValidationError.ACTION_REQUIRED)
    @ApiModelProperty(value = "Action that initiated the creation of the case payment order", required = true,
        example = "Case Submit")
    private String action;

    @NotEmpty(message = ValidationError.RESPONSIBLE_PARTY_REQUIRED)
    @ApiModelProperty(value = "Description of the party responsible for the case payment order", required = true,
        example = "Jane Doe")
    private String responsibleParty;

    @NotNull(message = ValidationError.ORDER_REFERENCE_REQUIRED)
    @Pattern(regexp = Validator.ORDER_REFERENCE_RG, message = ValidationError.ORDER_REFERENCE_INVALID)
    @ApiModelProperty(value = "Description of the Payments system order reference", required = true,
        example = "2021-11223344556")
    private String orderReference;

    @JsonIgnore
    public UUID getUUID() {
        return UUID.fromString(this.id);
    }

}
