package com.nainital.backend.admin;

import com.nainital.backend.admin.service.AdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminBootstrap implements ApplicationRunner {

    private final AdminService adminService;

    @Override
    public void run(ApplicationArguments args) {
        adminService.bootstrapSuperAdmin();
        log.info("Admin bootstrap complete. Default login: admin@nainital.com / Admin@123");
    }
}
