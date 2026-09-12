package com.erp.fin.mapper;

import com.erp.fin.dto.RuleLineCreateRequest;
import com.erp.fin.dto.RuleLineResponse;
import com.erp.fin.entity.EventTypeRule;
import com.erp.fin.entity.RuleLine;
import org.springframework.stereotype.Component;

/**
 * Manual mapper for ENT-FIN-010 (RuleLine) — build-create-mapper, no MapStruct. Child shape:
 * {@code toEntity} takes the resolved parent {@code EventTypeRule} and the service-assigned
 * {@code lineNo} as compile-time-safe parameters (A.4.2).
 */
@Component
public class RuleLineMapper {

    public RuleLine toEntity(RuleLineCreateRequest request, EventTypeRule parent, Integer lineNo) {
        if (request == null) {
            return null;
        }
        return RuleLine.builder()
            .eventTypeRule(parent)
            .lineNo(lineNo)
            .accountDerivationTypeCode(request.getAccountDerivationTypeCode())
            .accountDerivationValue(request.getAccountDerivationValue())
            .amountSourceTypeCode(request.getAmountSourceTypeCode())
            .amountSourceValue(request.getAmountSourceValue())
            .directionCode(request.getDirectionCode())
            .distributionTypeCode(request.getDistributionTypeCode())
            .isRemainderFl(request.getIsRemainderFl() != null
                ? request.getIsRemainderFl() : Boolean.FALSE)
            .build();
    }

    public RuleLineResponse toResponse(RuleLine entity) {
        if (entity == null) {
            return null;
        }
        return RuleLineResponse.builder()
            .ruleLinePk(entity.getRuleLinePk())
            .eventTypeRuleId(entity.getEventTypeRule() == null
                ? null : entity.getEventTypeRule().getEventTypeRulePk())
            .lineNo(entity.getLineNo())
            .accountDerivationTypeCode(entity.getAccountDerivationTypeCode())
            .accountDerivationValue(entity.getAccountDerivationValue())
            .amountSourceTypeCode(entity.getAmountSourceTypeCode())
            .amountSourceValue(entity.getAmountSourceValue())
            .directionCode(entity.getDirectionCode())
            .distributionTypeCode(entity.getDistributionTypeCode())
            .isRemainderFl(Boolean.TRUE.equals(entity.getIsRemainderFl()))
            .createdAt(entity.getCreatedAt())
            .build();
    }
}
