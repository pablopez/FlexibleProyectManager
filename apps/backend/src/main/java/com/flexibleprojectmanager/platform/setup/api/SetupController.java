package com.flexibleprojectmanager.platform.setup.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import com.flexibleprojectmanager.platform.setup.application.SetupService;
import com.flexibleprojectmanager.platform.setup.api.SetupDtos.InitializeSetupRequest;
import com.flexibleprojectmanager.platform.setup.api.SetupDtos.InstallationResponse;
import com.flexibleprojectmanager.platform.setup.api.SetupDtos.OrganizationSummary;
import com.flexibleprojectmanager.platform.setup.api.SetupDtos.SetupAdministratorResponse;
import com.flexibleprojectmanager.platform.setup.api.SetupDtos.SetupInitializationResponse;
import com.flexibleprojectmanager.platform.setup.api.SetupDtos.SetupStatusResponse;

@RestController
@RequestMapping("/api/v1/setup")
public class SetupController {
    private final SetupService setupService;

    public SetupController(SetupService setupService) {
        this.setupService = setupService;
    }

    @GetMapping("/status")
    public SetupStatusResponse status() {
        return new SetupStatusResponse(setupService.isInitialized());
    }

    @PostMapping("/initialize")
    public ResponseEntity<SetupInitializationResponse> initialize(@Valid @RequestBody InitializeSetupRequest request) {
        SetupService.SetupResult result = setupService.initialize(new SetupService.SetupCommand(
                request.organization().name(), request.installation().name(), request.administrator().email(),
                request.administrator().displayName(), request.administrator().password()));
        return ResponseEntity.status(201).body(new SetupInitializationResponse(
                true,
                new OrganizationSummary(result.organizationId(), result.organizationName()),
                new InstallationResponse(result.installationId(), result.organizationId(), result.installationName(),
                        result.platform(), result.applicationVersion(), result.installationStatus(), result.installationCreatedAt(), null),
                new SetupAdministratorResponse(result.administratorId(), result.administratorEmail(), result.administratorDisplayName())));
    }
}
