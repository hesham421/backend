package com.erp.security.controller;

import com.erp.common.web.ApiResponse;
import com.erp.common.web.OperationCode;
import com.erp.security.dto.MenuItemDto;
import com.erp.security.service.MenuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * User menu retrieval only — since SEC_MENU_ITEM removal, admin menu management lives in PageController.
 */
@RestController
@RequestMapping("/api/menu")
@RequiredArgsConstructor
@Tag(name = "Menu Management", description = "APIs for user menu retrieval based on permissions")
public class MenuController {

    private final MenuService menuService;
    private final OperationCode operationCode;

    @GetMapping("/user-menu")
    @Operation(
        summary = "Get user menu based on permissions", 
        description = "Returns menu tree structure for current logged-in user. Shows only pages where user has VIEW permission."
    )
    public ResponseEntity<ApiResponse<java.util.List<MenuItemDto>>> getUserMenu() {
        return operationCode.craftResponse(menuService.getUserMenu());
    }

    @GetMapping("/user-menu/{userId}")
    @Operation(
        summary = "Get menu for specific user (Admin)", 
        description = "Admin: View menu structure for any user. Useful for debugging permission issues."
    )
    public ResponseEntity<ApiResponse<java.util.List<MenuItemDto>>> getUserMenuById(@PathVariable Long userId) {
        return operationCode.craftResponse(menuService.getUserMenu(userId));
    }
}
