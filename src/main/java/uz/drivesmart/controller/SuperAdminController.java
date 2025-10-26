package uz.drivesmart.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uz.drivesmart.dto.request.AddAdminRequest;
import uz.drivesmart.dto.response.AuthResponse;
import uz.drivesmart.service.UserService;

@Tag(name = "SuperAdmin", description = "SuperAdmin boshqaruvi API")
@RestController
@RequestMapping("/api/superadmin")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class SuperAdminController {
    private final UserService userService;

    @Operation(summary = "Yangi Admin qo‘shish", description = "SuperAdmin tomonidan yangi Admin yaratadi")
    @ApiResponse(responseCode = "200", description = "Admin muvaffaqiyatli qo‘shildi")
    @PostMapping("/admins")
    public ResponseEntity<AuthResponse> addAdmin(@Valid @RequestBody AddAdminRequest request) {
        return ResponseEntity.ok(userService.addAdmin(request));
    }
}