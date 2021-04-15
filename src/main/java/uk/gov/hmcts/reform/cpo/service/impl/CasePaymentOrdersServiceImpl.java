package uk.gov.hmcts.reform.cpo.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.cpo.data.CasePaymentOrderEntity;
import uk.gov.hmcts.reform.cpo.domain.CasePaymentOrder;
import uk.gov.hmcts.reform.cpo.exception.CasePaymentOrderCouldNotBeFoundException;
import uk.gov.hmcts.reform.cpo.exception.CasePaymentOrdersFilterException;
import uk.gov.hmcts.reform.cpo.payload.UpdateCasePaymentOrderRequest;
import uk.gov.hmcts.reform.cpo.repository.CasePaymentOrderQueryFilter;
import uk.gov.hmcts.reform.cpo.repository.CasePaymentOrdersRepository;
import uk.gov.hmcts.reform.cpo.service.CasePaymentOrdersService;
import uk.gov.hmcts.reform.cpo.service.mapper.CasePaymentOrderMapper;
import uk.gov.hmcts.reform.cpo.validators.ValidationError;

import javax.transaction.Transactional;

@Service
public class CasePaymentOrdersServiceImpl implements CasePaymentOrdersService {

    @Autowired
    private final CasePaymentOrderMapper casePaymentOrderMapper;
    @Autowired
    private final CasePaymentOrdersRepository casePaymentOrdersRepository;

    public CasePaymentOrdersServiceImpl(CasePaymentOrderMapper casePaymentOrderMapper,
                                        CasePaymentOrdersRepository casePaymentOrdersRepository) {
        this.casePaymentOrderMapper = casePaymentOrderMapper;
        this.casePaymentOrdersRepository = casePaymentOrdersRepository;
    }


    @Transactional
    @Override
    public CasePaymentOrder updateCasePaymentOrder(UpdateCasePaymentOrderRequest request) {
        throw new UnsupportedOperationException("Implement me: see CPO-6");
    }


    @Override
    public Page<CasePaymentOrder> getCasePaymentOrders(final CasePaymentOrderQueryFilter casePaymentOrderQueryFilter) {

        try {
            final Page<CasePaymentOrderEntity> casePaymentOrderEntities;
            final Pageable pageRequest = casePaymentOrderQueryFilter.getPageRequest();
            if (casePaymentOrderQueryFilter.isFindByCaseIdQuery()) {
                casePaymentOrderEntities = casePaymentOrdersRepository.findByCaseIdIn(
                    casePaymentOrderQueryFilter.getListOfLongCasesIds(), pageRequest);
            } else {
                casePaymentOrderEntities = casePaymentOrdersRepository.findByIdIn(
                    casePaymentOrderQueryFilter.getListUUID(),
                    pageRequest
                );
            }
            return getPageOfCasePaymentOrder(casePaymentOrderEntities);
        } catch (IllegalArgumentException exception) {
            throw new CasePaymentOrdersFilterException(ValidationError.CPO_PAGE_ERROR);
        }
    }

    private Page<CasePaymentOrder> getPageOfCasePaymentOrder(Page<CasePaymentOrderEntity> casePaymentOrderEntities) {

        if (casePaymentOrderEntities.isEmpty()) {
            throw new CasePaymentOrderCouldNotBeFoundException(ValidationError.CPO_NOT_FOUND);
        }
        return casePaymentOrderEntities.map(casePaymentOrderEntity ->
                                                casePaymentOrderMapper.toDomainModel(casePaymentOrderEntity)
        );
    }
}
