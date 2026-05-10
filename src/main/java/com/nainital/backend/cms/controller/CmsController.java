package com.nainital.backend.cms.controller;

import com.nainital.backend.catalog.model.Banner;
import com.nainital.backend.catalog.repository.BannerRepository;
import com.nainital.backend.catalog.model.StoreCategory;
import com.nainital.backend.cms.dto.CmsSectionDto;
import com.nainital.backend.cms.model.CmsSection;
import com.nainital.backend.cms.service.CmsService;
import com.nainital.backend.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class CmsController {

    private final CmsService cmsService;
    private final BannerRepository bannerRepo;

    // ─── Public endpoints ─────────────────────────────────────────────────────

    @GetMapping("/api/cms/sections")
    public ResponseEntity<ApiResponse<List<CmsSectionDto>>> getActiveSections(
            @RequestParam(required = false) String category) {
        return ResponseEntity.ok(ApiResponse.ok(cmsService.getActiveSections(category)));
    }

    // ─── Admin: CMS sections ──────────────────────────────────────────────────

    @GetMapping("/api/admin/cms/sections")
    public ResponseEntity<ApiResponse<List<CmsSection>>> adminGetAll() {
        return ResponseEntity.ok(ApiResponse.ok(cmsService.getAllSections()));
    }

    @PostMapping("/api/admin/cms/sections")
    public ResponseEntity<ApiResponse<CmsSection>> adminCreate(@RequestBody CmsSection section) {
        return ResponseEntity.ok(ApiResponse.ok("Created", cmsService.save(section)));
    }

    @PutMapping("/api/admin/cms/sections/{id}")
    public ResponseEntity<ApiResponse<CmsSection>> adminUpdate(
            @PathVariable String id, @RequestBody CmsSection section) {
        return ResponseEntity.ok(ApiResponse.ok("Updated", cmsService.update(id, section)));
    }

    @PatchMapping("/api/admin/cms/sections/{id}/toggle")
    public ResponseEntity<ApiResponse<CmsSection>> adminToggle(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.ok(cmsService.toggle(id)));
    }

    @DeleteMapping("/api/admin/cms/sections/{id}")
    public ResponseEntity<ApiResponse<Void>> adminDelete(@PathVariable String id) {
        cmsService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("Deleted", null));
    }

    // ─── Admin: Banner CRUD ───────────────────────────────────────────────────

    @GetMapping("/api/admin/banners")
    public ResponseEntity<ApiResponse<List<Banner>>> adminGetBanners() {
        return ResponseEntity.ok(ApiResponse.ok(bannerRepo.findAll()));
    }

    @PostMapping("/api/admin/banners")
    public ResponseEntity<ApiResponse<Banner>> adminCreateBanner(@RequestBody Banner banner) {
        return ResponseEntity.ok(ApiResponse.ok("Created", bannerRepo.save(banner)));
    }

    @PutMapping("/api/admin/banners/{id}")
    public ResponseEntity<ApiResponse<Banner>> adminUpdateBanner(
            @PathVariable String id, @RequestBody Banner banner) {
        banner.setId(id);
        return ResponseEntity.ok(ApiResponse.ok("Updated", bannerRepo.save(banner)));
    }

    @PatchMapping("/api/admin/banners/{id}/toggle")
    public ResponseEntity<ApiResponse<Banner>> adminToggleBanner(@PathVariable String id) {
        Banner b = bannerRepo.findById(id).orElseThrow(() -> new IllegalArgumentException("Banner not found"));
        b.setActive(!b.isActive());
        return ResponseEntity.ok(ApiResponse.ok(bannerRepo.save(b)));
    }

    @DeleteMapping("/api/admin/banners/{id}")
    public ResponseEntity<ApiResponse<Void>> adminDeleteBanner(@PathVariable String id) {
        bannerRepo.deleteById(id);
        return ResponseEntity.ok(ApiResponse.ok("Deleted", null));
    }
}
