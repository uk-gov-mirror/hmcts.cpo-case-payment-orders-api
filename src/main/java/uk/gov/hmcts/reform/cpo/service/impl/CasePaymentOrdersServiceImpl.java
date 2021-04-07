package uk.gov.hmcts.reform.cpo.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.cpo.data.CasePaymentOrderEntity;
import uk.gov.hmcts.reform.cpo.domain.CasePaymentOrder;
import uk.gov.hmcts.reform.cpo.exception.CasePaymentOrdersFilterException;
import uk.gov.hmcts.reform.cpo.repository.CasePaymentOrderQueryFilter;
import uk.gov.hmcts.reform.cpo.repository.CasePaymentOrdersRepository;
import uk.gov.hmcts.reform.cpo.service.CasePaymentOrdersService;
import uk.gov.hmcts.reform.cpo.service.mapper.CasePaymentOrderMapper;

import java.util.List;

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

    @Override
    public Page<CasePaymentOrder> getCasePaymentOrders(final CasePaymentOrderQueryFilter casePaymentOrderQueryFilter) {
        final Page<CasePaymentOrderEntity> casePaymentOrderEntities;
        if (casePaymentOrderQueryFilter.isItAnEmptyCriteria()) {
            return Page.empty();
        }
        validateCasePaymentOrderQueryFilter(casePaymentOrderQueryFilter);
        final PageRequest pageRequest = casePaymentOrderQueryFilter.getPageRequest();
        if (casePaymentOrderQueryFilter.isACasesIdQuery()) {
            casePaymentOrderEntities = casePaymentOrdersRepository.findByCaseIdIn(
                casePaymentOrderQueryFilter.getListOfLongCasesIds(),pageRequest);
        } else {
            casePaymentOrderEntities = casePaymentOrdersRepository.findByIdIn(
                casePaymentOrderQueryFilter.getListUUID(),
                pageRequest
            );
        }
        return getPageOfCasePaymentOrder(casePaymentOrderEntities,pageRequest);
    }

    private void validateCasePaymentOrderQueryFilter(final CasePaymentOrderQueryFilter casePaymentOrderQueryFilter) {
        if (casePaymentOrderQueryFilter.isAnIdsAndCasesIdQuery()) {
            throw new CasePaymentOrdersFilterException(
                "case payment orders cannot be filtered by both id and case id.");
        }
    }

    private Page<CasePaymentOrder> getPageOfCasePaymentOrder(Page<CasePaymentOrderEntity> casePaymentOrderEntities,
                                                             PageRequest pageRequest) {

        final List<CasePaymentOrder> casePaymentOrders =
            casePaymentOrderMapper.map(casePaymentOrderEntities.getContent());

        return new PageImpl<>(casePaymentOrders, pageRequest,
                                              casePaymentOrderEntities.getTotalElements());
    }
}
