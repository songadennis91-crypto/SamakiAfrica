package africa.samaki.marketplace.config;

import africa.samaki.marketplace.model.*;
import africa.samaki.marketplace.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ListingRepository listingRepository;
    private final PasswordEncoder passwordEncoder;
    private final SubscriptionPackageRepository subscriptionPackageRepository;
    private final CommissionSettingRepository commissionSettingRepository;
    private final PlatformFeeSettingRepository platformFeeSettingRepository;
    private final FinancialAuditLogRepository auditLogRepository;
    private final SecurityAuditLogRepository securityAuditLogRepository;

    public DataInitializer(UserRepository userRepository,
                           ListingRepository listingRepository,
                           PasswordEncoder passwordEncoder,
                           SubscriptionPackageRepository subscriptionPackageRepository,
                           CommissionSettingRepository commissionSettingRepository,
                           PlatformFeeSettingRepository platformFeeSettingRepository,
                           FinancialAuditLogRepository auditLogRepository,
                           SecurityAuditLogRepository securityAuditLogRepository) {
        this.userRepository = userRepository;
        this.listingRepository = listingRepository;
        this.passwordEncoder = passwordEncoder;
        this.subscriptionPackageRepository = subscriptionPackageRepository;
        this.commissionSettingRepository = commissionSettingRepository;
        this.platformFeeSettingRepository = platformFeeSettingRepository;
        this.auditLogRepository = auditLogRepository;
        this.securityAuditLogRepository = securityAuditLogRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        // 1. Seed Platform Owner account if not existing
        if (userRepository.findByEmail("owner@samaki.africa").isEmpty()) {
            User owner = new User();
            owner.setFullName("Dr. Dennis Songah (Platform Owner)");
            owner.setEmail("owner@samaki.africa");
            owner.setPhone("+254700000000");
            owner.setPassword(passwordEncoder.encode("password123"));
            owner.setRole(Role.OWNER);
            owner.setCountry("Kenya");
            userRepository.save(owner);
        }

        // 2. Seed System Developer account if not existing (Independent technical developer credentials)
        if (userRepository.findByEmail("developer@samaki.africa").isEmpty()) {
            User developer = new User();
            developer.setFullName("Eng. Alex Kiprop (Lead System Developer)");
            developer.setEmail("developer@samaki.africa");
            developer.setPhone("+254700000099");
            developer.setPassword(passwordEncoder.encode("password123"));
            developer.setRole(Role.SYSTEM_DEVELOPER);
            developer.setCountry("Kenya");
            userRepository.save(developer);
        }

        // 3. Seed Base Marketplace Users and Catch Listings if database empty
        if (userRepository.count() <= 2) { // only owner/developer or empty
            // Seed Fisher Kenya
            User fKenya = new User();
            fKenya.setFullName("Captain Juma Omondi");
            fKenya.setEmail("fisher.kenya@samaki.africa");
            fKenya.setPhone("+254712345678");
            fKenya.setPassword(passwordEncoder.encode("password123"));
            fKenya.setRole(Role.FISHER);
            fKenya.setCountry("Kenya");
            userRepository.save(fKenya);

            // Seed Fisher Tanzania
            User fTanzania = new User();
            fTanzania.setFullName("Mzee Hassan Ali");
            fTanzania.setEmail("hassan@samaki.africa");
            fTanzania.setPhone("+255784112233");
            fTanzania.setPassword(passwordEncoder.encode("password123"));
            fTanzania.setRole(Role.FISHER);
            fTanzania.setCountry("Tanzania");
            userRepository.save(fTanzania);

            // Seed Fisher Nigeria
            User fNigeria = new User();
            fNigeria.setFullName("Chief Emeka Okafor");
            fNigeria.setEmail("emeka@samaki.africa");
            fNigeria.setPhone("+2348031122334");
            fNigeria.setPassword(passwordEncoder.encode("password123"));
            fNigeria.setRole(Role.FISHER);
            fNigeria.setCountry("Nigeria");
            userRepository.save(fNigeria);

            // Seed Fisher Senegal
            User fSenegal = new User();
            fSenegal.setFullName("El Hadj Amadou Diop");
            fSenegal.setEmail("diop@samaki.africa");
            fSenegal.setPhone("+221776554433");
            fSenegal.setPassword(passwordEncoder.encode("password123"));
            fSenegal.setRole(Role.FISHER);
            fSenegal.setCountry("Senegal");
            userRepository.save(fSenegal);

            // Seed Fisher Egypt
            User fEgypt = new User();
            fEgypt.setFullName("Captain Tarek Mansour");
            fEgypt.setEmail("tarek@samaki.africa");
            fEgypt.setPhone("+201023456789");
            fEgypt.setPassword(passwordEncoder.encode("password123"));
            fEgypt.setRole(Role.FISHER);
            fEgypt.setCountry("Egypt");
            userRepository.save(fEgypt);

            // Seed Fisher South Africa
            User fSA = new User();
            fSA.setFullName("Pieter Van Der Merwe");
            fSA.setEmail("pieter@samaki.africa");
            fSA.setPhone("+27821122334");
            fSA.setPassword(passwordEncoder.encode("password123"));
            fSA.setRole(Role.FISHER);
            fSA.setCountry("South Africa");
            userRepository.save(fSA);

            // Seed Default Fisher & Buyer for demo
            User defaultFisher = new User();
            defaultFisher.setFullName("Captain Dennis Omondi");
            defaultFisher.setEmail("fisher@samaki.africa");
            defaultFisher.setPhone("+254700000001");
            defaultFisher.setPassword(passwordEncoder.encode("password123"));
            defaultFisher.setRole(Role.FISHER);
            defaultFisher.setCountry("Kenya");
            userRepository.save(defaultFisher);

            User buyer = new User();
            buyer.setFullName("Sarah Mwangi (Lakefront Bistro)");
            buyer.setEmail("buyer@samaki.africa");
            buyer.setPhone("+254722998877");
            buyer.setPassword(passwordEncoder.encode("password123"));
            buyer.setRole(Role.BUYER);
            buyer.setCountry("Kenya");
            userRepository.save(buyer);

            // 1. Kenya - Lake Victoria Tilapia
            createListing(defaultFisher, "Fresh Lake Victoria Tilapia", "Fresh", 500.0, 2, new BigDecimal("380.00"), "KES",
                    "Kenya", "Lake Victoria", "Mbita Point Pier, Homa Bay", ListingStatus.READY,
                    "Ice-packed in insulated cooler containers. M-Pesa or Cash on inspection.",
                    "Prime dawn catch hauled off Rusinga Island deep channels. Clear eyes, firm silver scales, vacuum ice-packed.",
                    "https://images.unsplash.com/photo-1544551763-46a013bb70d5?auto=format&fit=crop&w=800&q=80");

            // 2. Kenya - Nile Perch Fillet
            createListing(fKenya, "Jumbo Nile Perch Fillet (Mbuta)", "Fresh", 850.0, 1, new BigDecimal("540.00"), "KES",
                    "Kenya", "Lake Victoria", "Dunga Beach Pier, Kisumu", ListingStatus.CAUGHT,
                    "Cold-chain refrigerated van delivery to Nairobi and Kisumu. Minimum order 30kg.",
                    "Thick-cut white meat Nile Perch caught in deep benthic channels. High Omega-3 profile, ideal for grilling and frying.",
                    "https://images.unsplash.com/photo-1534447677768-be436bb09401?auto=format&fit=crop&w=800&q=80");

            // 3. Tanzania - Lake Tanganyika Sardines (Kapenta)
            createListing(fTanzania, "Lake Tanganyika Sardines (Kapenta / Ndakala)", "Dried", 1400.0, 120, new BigDecimal("220.00"), "KES",
                    "Tanzania", "Lake Tanganyika", "Kigoma Port Pier", ListingStatus.READY,
                    "Moisture-sealed 25kg sacks. East African cross-border transit documents ready.",
                    "Clean, mesh-rack sun-dried Kapenta from the pristine deep waters of Lake Tanganyika. 100% sand-free and crisp.",
                    "https://images.unsplash.com/photo-1615141982883-c7ad0e69fd62?auto=format&fit=crop&w=800&q=80");

            // 4. Nigeria - Gulf of Guinea Tiger Prawns
            createListing(fNigeria, "Atlantic Giant Tiger Prawns", "Fresh", 320.0, 15, new BigDecimal("1250.00"), "KES",
                    "Nigeria", "Gulf of Guinea (Atlantic Ocean)", "Victoria Island Fishery Wharf, Lagos", ListingStatus.READY,
                    "Flash-chilled in flake-ice seawater. Same-day delivery to Lagos, Abuja and international air freight.",
                    "Wild-caught jumbo oceanic tiger prawns harvested from the outer continental shelf of the Gulf of Guinea. Sweet, firm, succulent meat.",
                    "https://images.unsplash.com/photo-1565680018434-b513d5e5fd47?auto=format&fit=crop&w=800&q=80");

            // 5. Senegal - Thiof (White Grouper)
            createListing(fSenegal, "Senegalese Thiof (White Grouper)", "Fresh", 420.0, 1, new BigDecimal("750.00"), "KES",
                    "Senegal", "Atlantic Ocean", "Soumbédioune Beach, Dakar", ListingStatus.READY,
                    "Seawater slurry boxes. Air freight and coastal cold-truck delivery ready.",
                    "National culinary pride of Senegal. Premium line-caught white grouper (Epinephelus aeneus) from the Atlantic shelf. Delicate white flakes, exceptional taste.",
                    "https://images.unsplash.com/photo-1524704654690-b56c05c78a00?auto=format&fit=crop&w=800&q=80");

            // 6. Egypt - Mediterranean Sea Bass
            createListing(fEgypt, "Mediterranean Sea Bass (Qarous)", "Fresh", 600.0, 2, new BigDecimal("680.00"), "KES",
                    "Egypt", "Mediterranean Sea", "Anfoushi Fishing Port, Alexandria", ListingStatus.READY,
                    "Blast-chilled at 0C in insulated export crates. Direct wholesale dispatch.",
                    "Wild Mediterranean sea bass caught along the Alexandria coastal shelf. Silvery skin, firm texture, prized across North African and Mediterranean culinary markets.",
                    "https://images.unsplash.com/photo-1544551763-46a013bb70d5?auto=format&fit=crop&w=800&q=80");

            // 7. South Africa - Cape Hake
            createListing(fSA, "Cape Hake (Merluccius capensis)", "Frozen", 950.0, 1, new BigDecimal("490.00"), "KES",
                    "South Africa", "Atlantic Ocean (Benguela Current)", "Cape Town Harbour", ListingStatus.READY,
                    "Blast frozen at -25C at sea. Export documentation and HACCP certified.",
                    "Deep-sea wild harvest from the cold, nutrient-rich Benguela upwelling system off the Cape of Good Hope. Lean, flaky white fish.",
                    "https://images.unsplash.com/photo-1509722747041-616f39b57569?auto=format&fit=crop&w=800&q=80");

            // 8. Kenya - Indian Ocean Red Snapper
            createListing(defaultFisher, "Indian Ocean Red Snapper", "Fresh", 280.0, 1, new BigDecimal("690.00"), "KES",
                    "Kenya", "Indian Ocean", "Malindi Old Port, Kilifi", ListingStatus.READY,
                    "Chilled seawater slurry containers. Air freight to Wilson Airport, Nairobi or coastal collection at Malindi jetty.",
                    "Line-caught wild Red Snapper from outer coral drop-offs of the Malindi marine shelf. Vivid red sheen, prized by luxury coastal resorts.",
                    "https://images.unsplash.com/photo-1524704654690-b56c05c78a00?auto=format&fit=crop&w=800&q=80");
        }

        // 3. Seed Subscription Packages if not present
        if (subscriptionPackageRepository.count() == 0) {
            seedSubscriptionPackage("FREE_FISHER", "Free Starter Artisanal",
                    "For artisanal small-scale fishers to list fresh catches with standard marketplace visibility.",
                    "Artisanal Fishers & Smallholders",
                    new BigDecimal("0.00"), new BigDecimal("0.00"), "KES",
                    "• Up to 3 active live catch listings\n• Standard landing site discovery\n• Direct buyer SMS inquiries\n• Daily basin market price index",
                    true, null, null, null, 0);

            seedSubscriptionPackage("PRO_FISHER", "Artisanal Master Fisher",
                    "Full professional suite for commercial artisanal captains and boat owners.",
                    "Master Fishers & Boat Owners",
                    new BigDecimal("1500.00"), new BigDecimal("15000.00"), "KES",
                    "• Unlimited active fish catch listings\n• Priority homepage showcase\n• Direct cold-chain truck dispatch\n• Real-time basin market alerts\n• Verified Fisher blue badge\n• Zero transaction fee on first 3 catches each month",
                    true, new BigDecimal("1200.00"), new BigDecimal("20.00"), LocalDateTime.now().plusMonths(3), 14);

            seedSubscriptionPackage("FLEET_COMMERCIAL", "Commercial Fleet & Cooperative",
                    "Engineered for offshore trawlers, aquaculture cage farms, and cooperative societies.",
                    "Industrial Trawlers & Co-ops",
                    new BigDecimal("8500.00"), new BigDecimal("85000.00"), "KES",
                    "• Multi-vessel catch management\n• Bulk auction & forward harvest contracts\n• Multi-port landing site broadcast\n• Dedicated cold-chain logistics broker\n• Export phytosanitary assistance\n• Real-time sonar basin analytics API",
                    true, null, null, null, 30);

            seedSubscriptionPackage("ENTERPRISE_BUYER", "Wholesale Procurement & Exporter",
                    "Built for commercial restaurant chains, supermarkets, fishmonger wholesalers, and overseas exporters.",
                    "Wholesale Buyers & Exporters",
                    new BigDecimal("18000.00"), new BigDecimal("180000.00"), "KES",
                    "• Direct pier-to-kitchen cold chain route planning\n• Automated escrow payment protection\n• Guaranteed grade-A catch allocation\n• HACCP & phytosanitary batch provenance\n• Custom credit & net-30 invoice settlement\n• 24/7 Priority platform support",
                    true, null, null, null, 14);
        }

        // 4. Seed Commission Settings if not present
        if (commissionSettingRepository.count() == 0) {
            // Pan-African Base Rates (ALL)
            seedCommission("FISH_MARKETPLACE", "Fish Catch Marketplace", "ALL", new BigDecimal("3.50"),
                    new BigDecimal("20.00"), new BigDecimal("15000.00"), "KES", "Standard Pan-African fish catch sales commission");
            seedCommission("EQUIPMENT_MARKETPLACE", "Fishing Gear & Vessels", "ALL", new BigDecimal("4.00"),
                    new BigDecimal("100.00"), new BigDecimal("50000.00"), "KES", "Outboard engines, boats, nets & gear");
            seedCommission("SUPPLIER_COMMISSION", "Aquaculture Feed & Fingerlings", "ALL", new BigDecimal("3.00"),
                    new BigDecimal("50.00"), new BigDecimal("25000.00"), "KES", "Hatchery fingerlings, extruded pellets, and ice slurry");
            seedCommission("COLD_CHAIN_LOGISTICS", "Cold-Chain Logistics & Riders", "ALL", new BigDecimal("5.00"),
                    new BigDecimal("50.00"), new BigDecimal("10000.00"), "KES", "Refrigerated van and rider delivery booking commission");
            seedCommission("OTHER_TRANSACTIONS", "Ancillary Maritime Services", "ALL", new BigDecimal("2.50"),
                    new BigDecimal("20.00"), new BigDecimal("10000.00"), "KES", "Other ancillary marketplace transactions");

            // Country-Specific Overrides
            seedCommission("FISH_MARKETPLACE", "Fish Catch Marketplace", "Kenya", new BigDecimal("3.00"),
                    new BigDecimal("20.00"), new BigDecimal("12000.00"), "KES", "Kenya domestic Lake Victoria & Coast preferential rate");
            seedCommission("FISH_MARKETPLACE", "Fish Catch Marketplace", "Tanzania", new BigDecimal("3.20"),
                    new BigDecimal("20.00"), new BigDecimal("12000.00"), "KES", "Tanzania Lake Victoria & Tanganyika rate");
            seedCommission("FISH_MARKETPLACE", "Fish Catch Marketplace", "Uganda", new BigDecimal("3.20"),
                    new BigDecimal("20.00"), new BigDecimal("12000.00"), "KES", "Uganda Lake Victoria & Albert rate");
            seedCommission("FISH_MARKETPLACE", "Fish Catch Marketplace", "Nigeria", new BigDecimal("4.00"),
                    new BigDecimal("50.00"), new BigDecimal("30000.00"), "KES", "Nigeria Gulf of Guinea & Niger Delta commercial rate");
            seedCommission("FISH_MARKETPLACE", "Fish Catch Marketplace", "Senegal", new BigDecimal("3.50"),
                    new BigDecimal("30.00"), new BigDecimal("20000.00"), "KES", "Senegal Atlantic artisanal catch rate");
            seedCommission("FISH_MARKETPLACE", "Fish Catch Marketplace", "Egypt", new BigDecimal("3.80"),
                    new BigDecimal("50.00"), new BigDecimal("25000.00"), "KES", "Egypt Mediterranean & Nile aquaculture rate");
            seedCommission("FISH_MARKETPLACE", "Fish Catch Marketplace", "South Africa", new BigDecimal("4.50"),
                    new BigDecimal("100.00"), new BigDecimal("40000.00"), "KES", "South Africa Benguela marine fishery commercial rate");
        }

        // 5. Seed Platform Fees if not present
        if (platformFeeSettingRepository.count() == 0) {
            seedFee("SERVICE_CHARGE", "FEATURED_LISTING_FEE", "Featured Catch Listing (7 Days)",
                    "Highlight catch on the marketplace header banner and search results", new BigDecimal("350.00"), "KES", "PER_POST");
            seedFee("SERVICE_CHARGE", "TOP_SEARCH_BOOST", "Landing Site Search Boost (3 Days)",
                    "Boost listing to the top of the selected landing site's directory", new BigDecimal("200.00"), "KES", "PER_POST");
            seedFee("SERVICE_CHARGE", "ESCROW_ASSURANCE_FEE", "Escrow Payment Protection Guarantee",
                    "Secures buyer payment in trust until fish is weighed and inspected at pier", new BigDecimal("150.00"), "KES", "PER_ORDER");
            seedFee("LOGISTICS", "DELIVERY_ARRANGE_FEE", "Cold-Chain Logistics Arrangement",
                    "Dispatches certified refrigerated van or insulated motorbike with ice pack", new BigDecimal("300.00"), "KES", "PER_ORDER");
            seedFee("AI_PREMIUM", "AI_MARKET_INTELLIGENCE", "Samaki AI Pricing Forecast & Catch Advisory",
                    "Predictive AI forecasting for landing pier spot prices and weather conditions", new BigDecimal("950.00"), "KES", "PER_MONTH");
            seedFee("ADVERTISING", "AD_BANNER_HERO", "Homepage Hero Banner Ad (Daily)",
                    "Full-width hero carousel promotional display on marketplace homepage", new BigDecimal("1200.00"), "KES", "PER_DAY");
            seedFee("ADVERTISING", "AD_SPONSORED_BRAND", "Equipment Supplier Sponsored Tile (Monthly)",
                    "Featured tile in the Fishing Gear & Feed directory for commercial suppliers", new BigDecimal("7500.00"), "KES", "PER_MONTH");
        }

        // 6. Seed Baseline Audit Log if empty
        if (auditLogRepository.count() == 0) {
            FinancialAuditLog initLog = new FinancialAuditLog(
                    "INITIALIZATION",
                    "ALL_MODULES",
                    "SYSTEM_INIT",
                    "Unconfigured",
                    "Dynamic Database-Driven Financial Model Active",
                    "System initialized initial baseline non-hardcoded pricing and commission framework",
                    "owner@samaki.africa",
                    "PAN-AFRICAN (ALL)"
            );
            auditLogRepository.save(initLog);
        }

        // 7. Seed Baseline Security Audit Log if empty
        if (securityAuditLogRepository.count() == 0) {
            SecurityAuditLog secInitLog = new SecurityAuditLog(
                    "SECURITY_SUBSYSTEM_INIT",
                    "SYSTEM",
                    "owner@samaki.africa",
                    "127.0.0.1",
                    "SUCCESS",
                    "Admin Security & Password Management module initialized with separate Owner & Developer roles."
            );
            securityAuditLogRepository.save(secInitLog);
        }
    }

    private void seedSubscriptionPackage(String code, String name, String desc, String target,
                                         BigDecimal monthly, BigDecimal annual, String currency,
                                         String benefits, boolean active, BigDecimal promo,
                                         BigDecimal discount, LocalDateTime expiry, int trialDays) {
        SubscriptionPackage p = new SubscriptionPackage();
        p.setCode(code);
        p.setName(name);
        p.setDescription(desc);
        p.setTierTarget(target);
        p.setMonthlyPrice(monthly);
        p.setAnnualPrice(annual);
        p.setCurrency(currency);
        p.setBenefits(benefits);
        p.setActive(active);
        p.setPromoPrice(promo);
        p.setDiscountPercentage(discount);
        p.setPromoExpiryDate(expiry);
        p.setFreeTrialDays(trialDays);
        subscriptionPackageRepository.save(p);
    }

    private void seedCommission(String category, String label, String country, BigDecimal rate,
                                BigDecimal min, BigDecimal max, String currency, String notes) {
        CommissionSetting c = new CommissionSetting();
        c.setCategory(category);
        c.setCategoryLabel(label);
        c.setCountryCode(country);
        c.setRatePercentage(rate);
        c.setMinAmount(min);
        c.setMaxAmount(max);
        c.setCurrency(currency);
        c.setActive(true);
        c.setNotes(notes);
        c.setUpdatedBy("owner@samaki.africa");
        commissionSettingRepository.save(c);
    }

    private void seedFee(String category, String key, String name, String desc,
                         BigDecimal amount, String currency, String unit) {
        PlatformFeeSetting f = new PlatformFeeSetting();
        f.setFeeCategory(category);
        f.setFeeKey(key);
        f.setName(name);
        f.setDescription(desc);
        f.setAmount(amount);
        f.setCurrency(currency);
        f.setBillingUnit(unit);
        f.setActive(true);
        f.setUpdatedBy("owner@samaki.africa");
        platformFeeSettingRepository.save(f);
    }

    private void createListing(User fisher, String fishType, String quality, Double quantityKg, Integer fishPerKg,
                               BigDecimal pricePerKg, String currency, String country, String ground, String site,
                               ListingStatus status, String terms, String desc, String img) {
        Listing l = new Listing();
        l.setFisher(fisher);
        l.setFishType(fishType);
        l.setQuality(quality);
        l.setQuantityKg(quantityKg);
        l.setFishPerKg(fishPerKg);
        l.setPricePerKg(pricePerKg);
        l.setCurrency(currency);
        l.setCountry(country);
        l.setFishingGround(ground);
        l.setLandingSite(site);
        l.setStatus(status);
        l.setTerms(terms);
        l.setDescription(desc);
        l.setImageUrl(img);
        listingRepository.save(l);
    }
}
