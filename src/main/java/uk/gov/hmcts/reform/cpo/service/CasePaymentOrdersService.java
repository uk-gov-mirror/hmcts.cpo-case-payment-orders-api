package uk.gov.hmcts.reform.cpo.service;

import org.springframework.data.domain.Page;
import uk.gov.hmcts.reform.cpo.domain.CasePaymentOrder;
import uk.gov.hmcts.reform.cpo.repository.CasePaymentOrderQueryFilter;

public interface CasePaymentOrdersService {
    Page<CasePaymentOrder> getCasePaymentOrders(CasePaymentOrderQueryFilter casePaymentOrderQueryFilter);
}
