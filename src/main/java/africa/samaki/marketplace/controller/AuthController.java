package africa.samaki.marketplace.controller;

import africa.samaki.marketplace.model.AfricanFisheries;
import africa.samaki.marketplace.model.Listing;
import africa.samaki.marketplace.model.ListingStatus;
import africa.samaki.marketplace.model.Role;
import africa.samaki.marketplace.model.User;
import africa.samaki.marketplace.repository.ListingRepository;
import africa.samaki.marketplace.repository.UserLoginSessionRepository;
import africa.samaki.marketplace.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.List;

@Controller
public class AuthController {

    private final UserRepository userRepository;
    private final ListingRepository listingRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserLoginSessionRepository sessionRepository;

    public AuthController(UserRepository userRepository,
                          ListingRepository listingRepository,
                          PasswordEncoder passwordEncoder,
                          UserLoginSessionRepository sessionRepository) {
        this.userRepository = userRepository;
        this.listingRepository = listingRepository;
        this.passwordEncoder = passwordEncoder;
        this.sessionRepository = sessionRepository;
    }

    @GetMapping("/")
    public String home(Principal principal, Model model) {
        List<Listing> recentListings = listingRepository.findTop6ByStatusNotOrderByCreatedAtDesc(ListingStatus.SOLD);
        model.addAttribute("recentListings", recentListings);
        model.addAttribute("totalListings", listingRepository.count());
        model.addAttribute("majorBasins", AfricanFisheries.getMajorBasins());
        model.addAttribute("africanCountries", AfricanFisheries.getAllCountries());

        if (principal != null) {
            userRepository.findByEmail(principal.getName()).ifPresent(user -> {
                model.addAttribute("currentUser", user);
            });
        }
        return "index";
    }

    @GetMapping("/login")
    public String login(Principal principal) {
        if (principal != null) {
            return "redirect:/dashboard";
        }
        return "login";
    }

    @GetMapping("/signup")
    public String signupForm(Principal principal, Model model) {
        if (principal != null) {
            return "redirect:/dashboard";
        }
        model.addAttribute("user", new User());
        model.addAttribute("africanCountries", AfricanFisheries.getAllCountries());
        return "signup";
    }

    @PostMapping("/signup")
    public String signup(@ModelAttribute("user") User user, Model model) {
        if (user.getRole() == null || user.getRole() == Role.ADMIN) {
            model.addAttribute("error", "Please select whether you are a Fisher (Seller), Buyer or Logistics Rider.");
            model.addAttribute("africanCountries", AfricanFisheries.getAllCountries());
            return "signup";
        }
        if (userRepository.existsByPhone(user.getPhone())) {
            model.addAttribute("error", "This phone number is already registered.");
            model.addAttribute("africanCountries", AfricanFisheries.getAllCountries());
            return "signup";
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            model.addAttribute("error", "This email is already registered.");
            model.addAttribute("africanCountries", AfricanFisheries.getAllCountries());
            return "signup";
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
        return "redirect:/login?registered";
    }

    @GetMapping("/dashboard")
    public String dashboard(Principal principal, Model model) {
        User user = userRepository.findByEmail(principal.getName()).orElseThrow();
        model.addAttribute("currentUser", user);
        model.addAttribute("name", user.getFullName());
        model.addAttribute("userRole", user.getRole());
        model.addAttribute("majorBasins", AfricanFisheries.getMajorBasins());

        if (user.isAdmin()) {
            // ADMIN / OWNER / SYSTEM_DEVELOPER MASTER OVERVIEW
            model.addAttribute("isAdminView", true);
            model.addAttribute("isFisherView", false);
            model.addAttribute("isBuyerView", false);
            model.addAttribute("isSeller", false);

            long totalUsers = userRepository.count();
            long totalFishers = userRepository.countByRole(Role.FISHER);
            long totalBuyers = userRepository.countByRole(Role.BUYER);
            long totalRiders = userRepository.countByRole(Role.RIDER);
            long totalListings = listingRepository.count();
            long activeListings = listingRepository.countByStatusNot(ListingStatus.SOLD);

            List<Listing> allListings = listingRepository.findAllByOrderByCreatedAtDesc();
            double totalPlatformWeightKg = allListings.stream()
                    .filter(l -> l.getStatus() != ListingStatus.SOLD)
                    .mapToDouble(l -> l.getQuantityKg() != null ? l.getQuantityKg() : 0.0)
                    .sum();

            long activeSessionsCount = sessionRepository.findAllByActiveTrueOrderByLastAccessTimeDesc().size();

            model.addAttribute("totalUsers", totalUsers);
            model.addAttribute("totalFishers", totalFishers);
            model.addAttribute("totalBuyers", totalBuyers);
            model.addAttribute("totalRiders", totalRiders);
            model.addAttribute("totalListings", totalListings);
            model.addAttribute("activeListings", activeListings);
            model.addAttribute("totalPlatformWeightKg", totalPlatformWeightKg);
            model.addAttribute("activeSessionsCount", activeSessionsCount);
            model.addAttribute("recentGlobalListings", allListings.stream().limit(12).toList());
            model.addAttribute("allUsers", userRepository.findAllByOrderByFullNameAsc());
            model.addAttribute("statuses", ListingStatus.values());
        } else if (user.getRole() == Role.FISHER) {
            model.addAttribute("isAdminView", false);
            model.addAttribute("isFisherView", true);
            model.addAttribute("isBuyerView", false);
            model.addAttribute("isSeller", true);

            List<Listing> myListings = listingRepository.findByFisherOrderByCreatedAtDesc(user);
            long activeCatches = myListings.stream().filter(l -> l.getStatus() != ListingStatus.SOLD).count();
            double totalWeightKg = myListings.stream().mapToDouble(l -> l.getQuantityKg() != null ? l.getQuantityKg() : 0.0).sum();

            model.addAttribute("myListings", myListings);
            model.addAttribute("activeCatches", activeCatches);
            model.addAttribute("totalWeightKg", totalWeightKg);
            model.addAttribute("statuses", ListingStatus.values());
        } else {
            // Buyer / Rider view (User Side)
            model.addAttribute("isAdminView", false);
            model.addAttribute("isFisherView", false);
            model.addAttribute("isBuyerView", true);
            model.addAttribute("isSeller", false);

            List<Listing> availableCatches = listingRepository.findTop6ByStatusNotOrderByCreatedAtDesc(ListingStatus.SOLD);
            model.addAttribute("availableCatches", availableCatches);
        }

        return "dashboard";
    }
}
