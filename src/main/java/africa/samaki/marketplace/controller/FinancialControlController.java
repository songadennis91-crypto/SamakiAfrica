package africa.samaki.marketplace.controller;

import africa.samaki.marketplace.model.*;
import africa.samaki.marketplace.repository.UserRepository;
import africa.samaki.marketplace.service.FinancialControlService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/admin/financial-control")
@PreAuthorize("hasRole('OWNER')")
public class FinancialControlController {

    private final FinancialControlService financialService;
    private final UserRepository userRepository;

    public FinancialControlController(FinancialControlService financialService, UserRepository userRepository) {
        this.financialService = financialService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public String index(
            @RequestParam(defaultValue = "subscriptions") String tab,
            @RequestParam(required = false) String q,
            Principal principal,
            Model model) {

        User currentUser = userRepository.findByEmail(principal.getName()).orElse(null);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("activeTab", tab);
        model.addAttribute("africanCountries", AfricanFisheries.getAllCountries());

        // 1. Subscription packages
        model.addAttribute("subscriptionPackages", financialService.getAllPackages());
        model.addAttribute("newPackage", new SubscriptionPackage());

        // 2. Marketplace Commissions (Pan-African Base)
        model.addAttribute("baseCommissions", financialService.getBaseCommissionSettings());
        model.addAttribute("newCommission", new CommissionSetting());

        // 3. Service Charges & AI
        List<PlatformFeeSetting> serviceCharges = financialService.getFeesByCategory("SERVICE_CHARGE");
        serviceCharges.addAll(financialService.getFeesByCategory("LOGISTICS"));
        serviceCharges.addAll(financialService.getFeesByCategory("AI_PREMIUM"));
        model.addAttribute("serviceCharges", serviceCharges);
        model.addAttribute("newFee", new PlatformFeeSetting());

        // 4. Advertising Pricing
        model.addAttribute("advertisingCharges", financialService.getFeesByCategory("ADVERTISING"));

        // 5. Country Overrides
        model.addAttribute("countryOverrides", financialService.getCountryOverrides());

        // 6. Audit History
        model.addAttribute("auditLogs", financialService.getAuditLogs(q));
        model.addAttribute("auditQuery", q);

        return "admin/financial-control";
    }

    // ==========================================
    // 1. SUBSCRIPTIONS ACTIONS
    // ==========================================

    @PostMapping("/subscriptions/save")
    public String saveSubscription(
            @ModelAttribute("pkg") SubscriptionPackage pkg,
            @RequestParam(required = false) String reason,
            Principal principal,
            RedirectAttributes redirectAttributes) {
        try {
            financialService.savePackage(pkg, reason, principal.getName());
            redirectAttributes.addFlashAttribute("successMessage", "Subscription package '" + pkg.getName() + "' saved successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error saving subscription package: " + e.getMessage());
        }
        return "redirect:/admin/financial-control?tab=subscriptions";
    }

    @PostMapping("/subscriptions/{id}/toggle")
    public String toggleSubscription(
            @PathVariable Long id,
            @RequestParam(required = false) String reason,
            Principal principal,
            RedirectAttributes redirectAttributes) {
        try {
            boolean active = financialService.togglePackageStatus(id, reason, principal.getName());
            redirectAttributes.addFlashAttribute("successMessage", "Subscription package status updated to: " + (active ? "ACTIVE" : "INACTIVE"));
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating package status: " + e.getMessage());
        }
        return "redirect:/admin/financial-control?tab=subscriptions";
    }

    // ==========================================
    // 2. COMMISSIONS ACTIONS
    // ==========================================

    @PostMapping("/commissions/save")
    public String saveCommission(
            @ModelAttribute("commission") CommissionSetting setting,
            @RequestParam(required = false) String reason,
            Principal principal,
            RedirectAttributes redirectAttributes) {
        try {
            financialService.saveCommissionSetting(setting, reason, principal.getName());
            redirectAttributes.addFlashAttribute("successMessage", "Commission rate for '" + setting.getCategoryLabel() + "' (" + setting.getCountryCode() + ") updated to " + setting.getRatePercentage() + "%.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error saving commission setting: " + e.getMessage());
        }
        return "redirect:/admin/financial-control?tab=commissions";
    }

    @PostMapping("/commissions/{id}/toggle")
    public String toggleCommission(
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "commissions") String returnTab,
            @RequestParam(required = false) String reason,
            Principal principal,
            RedirectAttributes redirectAttributes) {
        try {
            boolean active = financialService.toggleCommissionStatus(id, reason, principal.getName());
            redirectAttributes.addFlashAttribute("successMessage", "Commission status updated to: " + (active ? "ACTIVE" : "INACTIVE"));
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating commission status: " + e.getMessage());
        }
        return "redirect:/admin/financial-control?tab=" + returnTab;
    }

    // ==========================================
    // 3. COUNTRY OVERRIDES ACTIONS
    // ==========================================

    @PostMapping("/country-pricing/save")
    public String saveCountryPricing(
            @ModelAttribute("countrySetting") CommissionSetting setting,
            @RequestParam(required = false) String reason,
            Principal principal,
            RedirectAttributes redirectAttributes) {
        try {
            financialService.saveCommissionSetting(setting, reason, principal.getName());
            redirectAttributes.addFlashAttribute("successMessage", "Country-specific rate for " + setting.getCountryCode() + " saved at " + setting.getRatePercentage() + "%.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error saving country-specific pricing: " + e.getMessage());
        }
        return "redirect:/admin/financial-control?tab=country-pricing";
    }

    // ==========================================
    // 4. FEES & ADVERTISING ACTIONS
    // ==========================================

    @PostMapping("/fees/save")
    public String saveFee(
            @ModelAttribute("fee") PlatformFeeSetting fee,
            @RequestParam(defaultValue = "fees") String returnTab,
            @RequestParam(required = false) String reason,
            Principal principal,
            RedirectAttributes redirectAttributes) {
        try {
            financialService.saveFee(fee, reason, principal.getName());
            redirectAttributes.addFlashAttribute("successMessage", "Fee setting '" + fee.getName() + "' updated to " + fee.getAmount() + " " + fee.getCurrency() + " (" + fee.getBillingUnit() + ").");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error saving fee setting: " + e.getMessage());
        }
        return "redirect:/admin/financial-control?tab=" + returnTab;
    }

    @PostMapping("/fees/{id}/toggle")
    public String toggleFee(
            @PathVariable Long id,
            @RequestParam(defaultValue = "fees") String returnTab,
            @RequestParam(required = false) String reason,
            Principal principal,
            RedirectAttributes redirectAttributes) {
        try {
            boolean active = financialService.toggleFeeStatus(id, reason, principal.getName());
            redirectAttributes.addFlashAttribute("successMessage", "Fee status updated to: " + (active ? "ACTIVE" : "INACTIVE"));
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating fee status: " + e.getMessage());
        }
        return "redirect:/admin/financial-control?tab=" + returnTab;
    }

    // ==========================================
    // 5. SERVER-SIDE CALCULATION API
    // ==========================================

    @GetMapping("/api/calculate-commission")
    @ResponseBody
    public ResponseEntity<FinancialControlService.CommissionCalculationResult> calculateCommissionApi(
            @RequestParam String category,
            @RequestParam(defaultValue = "Kenya") String country,
            @RequestParam BigDecimal amount) {
        FinancialControlService.CommissionCalculationResult result = financialService.calculateCommission(category, country, amount);
        return ResponseEntity.ok(result);
    }
}
