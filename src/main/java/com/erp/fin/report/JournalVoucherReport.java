package com.erp.fin.report;

import com.erp.common.print.ReportParams;
import com.erp.common.print.TemplateReportProvider;
import com.erp.fin.dto.JournalEntryResponse;
import com.erp.fin.service.FinLookupValidationService;
import com.erp.fin.service.JournalEntryService;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** One journal entry printed as a voucher (the journal template, one entry): gated by {@code JournalEntryService.read}. */
@Component
@RequiredArgsConstructor
public class JournalVoucherReport implements TemplateReportProvider {

    private final JournalEntryService journalEntryService;
    private final FinReportLabels labels;

    @Override
    public String code() {
        return "FIN_JOURNAL_VOUCHER";
    }

    @Override
    public String titleAr() {
        return "قيد يومية";
    }

    @Override
    public String titleEn() {
        return "Journal voucher";
    }

    @Override
    public String templatePath() {
        return "fin/journal-entries";
    }

    @Override
    public Map<String, Object> parameters(ReportParams params) {
        JournalEntryResponse entry = journalEntryService.read(params.requiredLong("entryId")).getData();
        return FinPrintTemplates.parameters(params,
            List.of(params.localized("القيد: ", "Entry: ") + entry.getDocNo()),
            "ENTRY_IDS", String.valueOf(entry.getJournalEntryPk()),
            "JOURNAL_TYPE_LABELS", labels.map(FinLookupValidationService.KEY_JOURNAL_TYPE, params),
            "STATUS_LABELS", labels.map(FinLookupValidationService.KEY_JOURNAL_STATUS, params));
    }
}
