package com.erp.security.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * API-SEC-022 response node — one Page nested into the caller's navigation menu tree by
 * parentPageFk. {@code viewGrantedFl} is the explicit accessibility indicator named by
 * RULE-SEC-018: {@code true} when the caller directly holds {@code PERM_<pageCode>_VIEW}
 * (RULE-SEC-017); {@code false} when the node is present only as a non-clickable structural
 * label so a directly-granted descendant stays reachable (RULE-SEC-018 orphan branch).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Navigation menu tree node for the current user - عنصر شجرة قائمة التنقل للمستخدم الحالي")
public class MenuNodeResponse {

    @Schema(description = "Page unique identifier - المعرف الفريد للشاشة", example = "1")
    private Long id;

    @Schema(description = "Unique page code - رمز الشاشة الفريد", example = "SEC_ROLES")
    private String pageCode;

    @Schema(description = "Page name (Arabic) - اسم الشاشة بالعربية", example = "الأدوار")
    private String nameAr;

    @Schema(description = "Page name (English) - اسم الشاشة بالإنجليزية", example = "Roles")
    private String nameEn;

    @Schema(description = "True when the caller directly holds VIEW on this page; false when the node is a non-clickable structural label only (RULE-SEC-018) - صحيح إذا امتلك المستخدم صلاحية العرض مباشرة؛ خاطئ إذا كان العنصر تسمية هيكلية فقط", example = "true")
    private Boolean viewGrantedFl;

    @Schema(description = "Nested child menu nodes - عناصر القائمة الفرعية المتداخلة")
    private List<MenuNodeResponse> children;
}
