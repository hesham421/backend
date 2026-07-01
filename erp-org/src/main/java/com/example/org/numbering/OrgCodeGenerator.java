package com.example.org.numbering;

import com.example.org.repository.OrgBranchRepository;
import com.example.org.repository.OrgCostCenterRepository;
import com.example.org.repository.OrgDepartmentRepository;
import com.example.org.repository.OrgLegalEntityRepository;
import com.example.org.repository.OrgLocationSiteRepository;
import com.example.org.repository.OrgProfitCenterRepository;
import com.example.org.repository.OrgRegionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Generates ORG module business codes (LE-NNNNN, BR-..., etc.).
 *
 * TODO: XM-NE-1 DEFERRED — replace with NumberingEngine (1.6) REST client when READY.
 * Current impl uses DB sequence counters as a local fallback.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrgCodeGenerator {

    private final OrgLegalEntityRepository legalEntityRepository;
    private final OrgBranchRepository branchRepository;
    private final OrgRegionRepository regionRepository;
    private final OrgDepartmentRepository departmentRepository;
    private final OrgCostCenterRepository costCenterRepository;
    private final OrgProfitCenterRepository profitCenterRepository;
    private final OrgLocationSiteRepository locationSiteRepository;

    public String generateLegalEntityCode() {
        return generate("LE", legalEntityRepository.count() + 1);
    }

    public String generateBranchCode(String leCode) {
        long seq = branchRepository.count() + 1;
        return generateWithParent("BR", leCode, seq);
    }

    public String generateRegionCode(String leCode) {
        long seq = regionRepository.count() + 1;
        return generateWithParent("RG", leCode, seq);
    }

    public String generateDepartmentCode(String branchCode) {
        long seq = departmentRepository.count() + 1;
        return generateWithParent("DEP", branchCode, seq);
    }

    public String generateCostCenterCode(String branchCode) {
        long seq = costCenterRepository.count() + 1;
        return generateWithParent("CC", branchCode, seq);
    }

    public String generateProfitCenterCode(String leCode) {
        long seq = profitCenterRepository.count() + 1;
        return generateWithParent("PC", leCode, seq);
    }

    public String generateLocationSiteCode(String branchCode) {
        long seq = locationSiteRepository.count() + 1;
        return generateWithParent("LS", branchCode, seq);
    }

    private String generate(String prefix, long seq) {
        return String.format("%s-%05d", prefix, seq);
    }

    private String generateWithParent(String prefix, String parentCode, long seq) {
        return String.format("%s-%s-%05d", prefix, parentCode, seq);
    }
}
