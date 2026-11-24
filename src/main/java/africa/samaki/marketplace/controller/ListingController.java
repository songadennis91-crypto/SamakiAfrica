package africa.samaki.marketplace.controller;

import africa.samaki.marketplace.model.*;
import africa.samaki.marketplace.repository.ListingRepository;
import africa.samaki.marketplace.repository.UserRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class ListingController {

    private final ListingRepository listingRepository;
    private final UserRepository userRepository;

    public ListingController(ListingRepository listingRepository, UserRepository userRepository) {
        this.listingRepository = listingRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/listings")
    public String browse(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String quality,
            @RequestParam(required = false) String country,
            @RequestParam(required = false) String fishingGround,
            @RequestParam(required = false) ListingStatus status,
            @RequestParam(required = false) String fishType,
            Principal principal,
            Model model) {

        List<Listing> listings = (q == null || q.isBlank())
                ? listingRepository.findByStatusNotOrderByCreatedAtDesc(ListingStatus.SOLD)
                : listingRepository.search(q.trim(), ListingStatus.SOLD);

        // Apply additional filter criteria if provided
        if (quality != null && !quality.isBlank() && !quality.equalsIgnoreCase("ALL")) {
            listings = listings.stream()
                    .filter(l -> l.getQuality() != null && l.getQuality().equalsIgnoreCase(quality))
                    .collect(Collectors.toList());
        }

        if (country != null && !country.isBlank() && !country.equalsIgnoreCase("ALL")) {
            listings = listings.stream()
                    .filter(l -> l.getCountry() != null && l.getCountry().equalsIgnoreCase(country))
                    .collect(Collectors.toList());
        }

        if (fishingGround != null && !fishingGround.isBlank() && !fishingGround.equalsIgnoreCase("ALL")) {
            listings = listings.stream()
                    .filter(l -> l.getFishingGround() != null && l.getFishingGround().toLowerCase().contains(fishingGround.toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (status != null) {
            listings = listings.stream()
                    .filter(l -> l.getStatus() == status)
                    .collect(Collectors.toList());
        }

        if (fishType != null && !fishType.isBlank() && !fishType.equalsIgnoreCase("ALL")) {
            listings = listings.stream()
                    .filter(l -> l.getFishType() != null && l.getFishType().toLowerCase().contains(fishType.toLowerCase()))
                    .collect(Collectors.toList());
        }

        model.addAttribute("listings", listings);
        model.addAttribute("q", q);
        model.addAttribute("selectedQuality", quality);
        model.addAttribute("selectedCountry", country);
        model.addAttribute("selectedGround", fishingGround);
        model.addAttribute("selectedStatus", status != null ? status.name() : "");
        model.addAttribute("selectedFishType", fishType);
        model.addAttribute("totalCount", listings.size());
        model.addAttribute("africanCountries", AfricanFisheries.getAllCountries());
        model.addAttribute("majorBasins", AfricanFisheries.getMajorBasins());

        if (principal != null) {
            userRepository.findByEmail(principal.getName()).ifPresent(u -> {
                model.addAttribute("currentUser", u);
            });
        }

        return "listings";
    }

    @GetMapping("/listings/{id}")
    public String detail(@PathVariable Long id, Principal principal, Model model) {
        Listing listing = listingRepository.findById(id).orElse(null);
        if (listing == null) {
            return "redirect:/listings";
        }
        model.addAttribute("listing", listing);

        // Get related catches (same fish type or same landing site or country)
        List<Listing> related = listingRepository.findByStatusNotOrderByCreatedAtDesc(ListingStatus.SOLD).stream()
                .filter(l -> !l.getId().equals(id) && (l.getFishType().equalsIgnoreCase(listing.getFishType()) || l.getCountry().equalsIgnoreCase(listing.getCountry())))
                .limit(3)
                .collect(Collectors.toList());
        model.addAttribute("relatedListings", related);

        if (principal != null) {
            userRepository.findByEmail(principal.getName()).ifPresent(u -> {
                model.addAttribute("currentUser", u);
                model.addAttribute("isOwner", listing.getFisher().getId().equals(u.getId()));
            });
        } else {
            model.addAttribute("isOwner", false);
        }

        return "listing-detail";
    }

    @GetMapping("/listings/new")
    public String newForm(Principal principal, Model model) {
        User user = userRepository.findByEmail(principal.getName()).orElseThrow();
        if (user.getRole() != Role.FISHER) return "redirect:/dashboard";
        Listing listing = new Listing();
        listing.setCountry(user.getCountry() != null ? user.getCountry() : "Kenya");
        listing.setCurrency("KES");
        model.addAttribute("listing", listing);
        model.addAttribute("isEdit", false);
        model.addAttribute("currentUser", user);
        model.addAttribute("africanCountries", AfricanFisheries.getAllCountries());
        model.addAttribute("majorBasins", AfricanFisheries.getMajorBasins());
        return "listing-form";
    }

    @PostMapping("/listings")
    public String create(@ModelAttribute("listing") Listing listing, Principal principal) {
        User user = userRepository.findByEmail(principal.getName()).orElseThrow();
        if (user.getRole() != Role.FISHER) return "redirect:/dashboard";
        listing.setFisher(user);
        listingRepository.save(listing);
        return "redirect:/my-listings";
    }

    @GetMapping("/listings/{id}/edit")
    public String editForm(@PathVariable Long id, Principal principal, Model model) {
        User user = userRepository.findByEmail(principal.getName()).orElseThrow();
        Listing listing = listingRepository.findById(id).orElseThrow();
        if (!listing.getFisher().getId().equals(user.getId()) && user.getRole() != Role.ADMIN) {
            return "redirect:/my-listings";
        }
        model.addAttribute("listing", listing);
        model.addAttribute("isEdit", true);
        model.addAttribute("currentUser", user);
        model.addAttribute("africanCountries", AfricanFisheries.getAllCountries());
        model.addAttribute("majorBasins", AfricanFisheries.getMajorBasins());
        return "listing-form";
    }

    @PostMapping("/listings/{id}/edit")
    public String update(@PathVariable Long id, @ModelAttribute("listing") Listing updatedListing, Principal principal) {
        User user = userRepository.findByEmail(principal.getName()).orElseThrow();
        Listing existing = listingRepository.findById(id).orElseThrow();
        if (!existing.getFisher().getId().equals(user.getId()) && !user.isAdmin()) {
            return "redirect:/my-listings";
        }

        existing.setFishType(updatedListing.getFishType());
        existing.setQuality(updatedListing.getQuality());
        existing.setQuantityKg(updatedListing.getQuantityKg());
        existing.setFishPerKg(updatedListing.getFishPerKg());
        existing.setPricePerKg(updatedListing.getPricePerKg());
        existing.setCurrency(updatedListing.getCurrency());
        existing.setCountry(updatedListing.getCountry());
        existing.setFishingGround(updatedListing.getFishingGround());
        existing.setLandingSite(updatedListing.getLandingSite());
        existing.setTerms(updatedListing.getTerms());
        existing.setDescription(updatedListing.getDescription());
        existing.setImageUrl(updatedListing.getImageUrl());
        existing.setStatus(updatedListing.getStatus());

        listingRepository.save(existing);
        return user.isAdmin() ? "redirect:/dashboard" : "redirect:/my-listings";
    }

    @PostMapping("/listings/{id}/delete")
    public String delete(@PathVariable Long id, Principal principal) {
        User user = userRepository.findByEmail(principal.getName()).orElseThrow();
        Listing listing = listingRepository.findById(id).orElseThrow();
        if (listing.getFisher().getId().equals(user.getId()) || user.isAdmin()) {
            listingRepository.delete(listing);
        }
        return user.isAdmin() ? "redirect:/dashboard" : "redirect:/my-listings";
    }

    @GetMapping("/my-listings")
    public String mine(Principal principal, Model model) {
        User user = userRepository.findByEmail(principal.getName()).orElseThrow();
        List<Listing> listings = listingRepository.findByFisherOrderByCreatedAtDesc(user);
        model.addAttribute("listings", listings);
        model.addAttribute("statuses", ListingStatus.values());
        model.addAttribute("currentUser", user);

        // Stats calculation
        long activeCount = listings.stream().filter(l -> l.getStatus() != ListingStatus.SOLD).count();
        double totalKg = listings.stream().mapToDouble(l -> l.getQuantityKg() != null ? l.getQuantityKg() : 0.0).sum();
        model.addAttribute("activeCount", activeCount);
        model.addAttribute("totalKg", totalKg);

        return "my-listings";
    }

    @PostMapping("/listings/{id}/status")
    public String updateStatus(@PathVariable Long id, @RequestParam ListingStatus status, Principal principal) {
        User user = userRepository.findByEmail(principal.getName()).orElseThrow();
        Listing listing = listingRepository.findById(id).orElseThrow();
        if (listing.getFisher().getEmail().equals(principal.getName()) || user.isAdmin()) {
            listing.setStatus(status);
            listingRepository.save(listing);
        }
        return user.isAdmin() ? "redirect:/dashboard" : "redirect:/my-listings";
    }
}
