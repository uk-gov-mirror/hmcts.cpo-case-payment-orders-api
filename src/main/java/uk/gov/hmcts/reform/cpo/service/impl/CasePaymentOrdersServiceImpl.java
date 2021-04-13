package uk.gov.hmcts.reform.cpo.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.cpo.data.CasePaymentOrderEntity;
import uk.gov.hmcts.reform.cpo.domain.CasePaymentOrder;
import uk.gov.hmcts.reform.cpo.exception.CasePaymentOrdersQueryException;
import uk.gov.hmcts.reform.cpo.payload.UpdateCasePaymentOrderRequest;
import uk.gov.hmcts.reform.cpo.repository.CasePaymentOrderQueryFilter;
import uk.gov.hmcts.reform.cpo.repository.CasePaymentOrdersRepository;
import uk.gov.hmcts.reform.cpo.service.CasePaymentOrdersService;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static uk.gov.hmcts.reform.cpo.validators.ValidationError.CANNOT_DELETE_USING_IDS_AND_CASE_IDS;

@Service
public class CasePaymentOrdersServiceImpl implements CasePaymentOrdersService {

    private static final Logger LOG = LoggerFactory.getLogger(CasePaymentOrdersServiceImpl.class);

    public static final String AUDIT_ENTRY_DELETION_ERROR = "Exception thrown when deleting audit entry for case "
                                                            + "payment orders '{}'. Unwanted previous versions of the"
                                                            + " case payment orders may remain";

    private final CasePaymentOrdersRepository casePaymentOrdersRepository;

    @Autowired
    public CasePaymentOrdersServiceImpl(CasePaymentOrdersRepository casePaymentOrdersRepository) {
        this.casePaymentOrdersRepository = casePaymentOrdersRepository;
    }

    @Override
    public Page<CasePaymentOrderEntity> getCasePaymentOrders(
        final CasePaymentOrderQueryFilter casePaymentOrderQueryFilter) {

        if (casePaymentOrderQueryFilter.isItAnEmptyCriteria()) {
            return Page.empty();
        }
        validateCasePaymentOrderQueryFilter(casePaymentOrderQueryFilter);

        final PageRequest pageRequest = getPageRequest(casePaymentOrderQueryFilter);
        if (casePaymentOrderQueryFilter.isACasesIdQuery()) {
            return casePaymentOrdersRepository.findByCaseIdIn(
                casePaymentOrderQueryFilter.getListOfLongCasesIds(),
                pageRequest
            );
        } else {
            return casePaymentOrdersRepository.findByIdIn(
                casePaymentOrderQueryFilter.getListUUID(),
                pageRequest
            );
        }
    }

    @Transactional
    @Override
    public CasePaymentOrder updateCasePaymentOrder(UpdateCasePaymentOrderRequest request) {
        throw new UnsupportedOperationException("Implement me: see CPO-6");
    }

    @Override
    public void deleteCasePaymentOrders(CasePaymentOrderQueryFilter casePaymentOrderQueryFilter) {
        validateCasePaymentOrderQueryFilter(casePaymentOrderQueryFilter);

        if (casePaymentOrderQueryFilter.isACasesIdQuery()) {
            deleteCasePaymentOrdersByCaseIds(casePaymentOrderQueryFilter.getListOfLongCasesIds());
        } else {
            deleteCasePaymentOrdersByIds(casePaymentOrderQueryFilter.getListUUID());
        }
    }

    private void deleteCasePaymentOrdersByIds(List<UUID> ids) {
        casePaymentOrdersRepository.deleteByUuids(ids);
        try {
            casePaymentOrdersRepository.deleteAuditEntriesByUuids(ids);
        } catch (Exception e) {
            LOG.error(AUDIT_ENTRY_DELETION_ERROR, ids);
        }
    }

    private void deleteCasePaymentOrdersByCaseIds(List<Long> caseIds) {
        casePaymentOrdersRepository.deleteByCaseIds(caseIds);
        try {
            casePaymentOrdersRepository.deleteAuditEntriesByCaseIds(caseIds);
        } catch (Exception e) {
            LOG.error(AUDIT_ENTRY_DELETION_ERROR, caseIds);
        }
    }

    private PageRequest getPageRequest(CasePaymentOrderQueryFilter casePaymentOrderQueryFilter) {
        final List<Sort.Order> orders = new ArrayList<>();
        orders.add(new Sort.Order(Sort.Direction.ASC, CasePaymentOrderQueryFilter.CASES_ID));
        orders.add(new Sort.Order(Sort.Direction.ASC, CasePaymentOrderQueryFilter.ORDER_REFERENCE));
        return PageRequest.of(
            casePaymentOrderQueryFilter.getPageNumber(),
            casePaymentOrderQueryFilter.getPageSize(),
            Sort.by(orders)
        );
    }

    private void validateCasePaymentOrderQueryFilter(final CasePaymentOrderQueryFilter casePaymentOrderQueryFilter) {
        if (casePaymentOrderQueryFilter.isAnIdsAndCasesIdQuery()) {
            throw new CasePaymentOrdersQueryException(
                    CANNOT_DELETE_USING_IDS_AND_CASE_IDS);
        }
    }
}
