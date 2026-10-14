package com.erp.mdl.report;

import com.erp.common.print.ReportColumn;
import com.erp.common.print.ReportParams;
import com.erp.common.print.ReportProvider;
import com.erp.common.print.ReportResult;
import com.erp.mdl.dto.LookupTypeByOwnerSearchRequest;
import com.erp.mdl.dto.OwnerGroupResponse;
import com.erp.mdl.service.LookupTypeService;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * SCR-MDL-002 printed: every owner group of {@code LookupTypeService.browseByOwner}, same permission, flattened
 * group by group with the owner as the first column. The browse is not paged.
 */
@Component
@RequiredArgsConstructor
public class TypeRegistryReport implements ReportProvider {

    private static final List<ReportColumn> COLUMNS = List.of(
        ReportColumn.text("ownerModule", "الوحدة المالكة", "Owner module"),
        ReportColumn.text("key", "المفتاح", "Key"),
        ReportColumn.text("nameAr", "الاسم (عربي)", "Name (Arabic)"),
        ReportColumn.text("nameEn", "الاسم (إنجليزي)", "Name (English)"));

    private final LookupTypeService lookupTypeService;

    @Override
    public String code() {
        return "MDL_TYPE_REGISTRY";
    }

    @Override
    public String titleAr() {
        return "سجل أنواع اللوكب حسب المالك";
    }

    @Override
    public String titleEn() {
        return "Lookup-type registry by owner";
    }

    @Override
    public ReportResult run(ReportParams params) {
        List<OwnerGroupResponse> groups = lookupTypeService.browseByOwner(
            LookupTypeByOwnerSearchRequest.builder().filters(params.filters()).build()).getData();
        List<Map<String, Object>> rows = new ArrayList<>();
        for (OwnerGroupResponse group : groups) {
            String code = group.getOwnerModuleCode() == null ? "" : group.getOwnerModuleCode().trim();
            String owner = code.isEmpty() ? params.localized("وحدة مالكة غير محدَّدة", "Unspecified owner module") : code;
            if (group.getTypes() != null) {
                group.getTypes().forEach(type -> rows.add(ReportResult.row(
                    "ownerModule", owner,
                    "key", type.getKey(),
                    "nameAr", type.getNameAr(),
                    "nameEn", type.getNameEn())));
            }
        }
        return new ReportResult(COLUMNS, rows);
    }
}
