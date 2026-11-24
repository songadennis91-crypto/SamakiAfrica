package africa.samaki.marketplace.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "listings")
public class Listing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private User fisher;

    @Column(nullable = false)
    private String fishType;

    private String quality;

    @Column(nullable = false)
    private Double quantityKg;

    private Integer fishPerKg;

    @Column(nullable = false)
    private BigDecimal pricePerKg;

    @Column(nullable = false)
    private String currency = "KES";

    @Column(nullable = false)
    private String country = "Kenya";

    @Column(nullable = false)
    private String fishingGround;

    private String landingSite;

    @Column(length = 1000)
    private String terms;

    @Column(length = 2000)
    private String description;

    @Column(length = 500)
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ListingStatus status = ListingStatus.GOING_FISHING;

    private LocalDateTime createdAt = LocalDateTime.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public User getFisher() { return fisher; }
    public void setFisher(User fisher) { this.fisher = fisher; }
    
    public String getFishType() { return fishType; }
    public void setFishType(String fishType) { this.fishType = fishType; }
    
    public String getQuality() { return quality; }
    public void setQuality(String quality) { this.quality = quality; }
    
    public Double getQuantityKg() { return quantityKg; }
    public void setQuantityKg(Double quantityKg) { this.quantityKg = quantityKg; }
    
    public Integer getFishPerKg() { return fishPerKg; }
    public void setFishPerKg(Integer fishPerKg) { this.fishPerKg = fishPerKg; }
    
    public BigDecimal getPricePerKg() { return pricePerKg; }
    public void setPricePerKg(BigDecimal pricePerKg) { this.pricePerKg = pricePerKg; }
    
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
    
    public String getFishingGround() { return fishingGround; }
    public void setFishingGround(String fishingGround) { this.fishingGround = fishingGround; }
    
    public String getLandingSite() { return landingSite; }
    public void setLandingSite(String landingSite) { this.landingSite = landingSite; }
    
    public String getTerms() { return terms; }
    public void setTerms(String terms) { this.terms = terms; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public ListingStatus getStatus() { return status; }
    public void setStatus(ListingStatus status) { this.status = status; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    // Helper methods for presentation and financial calculations
    public BigDecimal getTotalPrice() {
        if (pricePerKg == null || quantityKg == null) return BigDecimal.ZERO;
        return pricePerKg.multiply(BigDecimal.valueOf(quantityKg));
    }

    public String getEffectiveImageUrl() {
        if (imageUrl != null && !imageUrl.isBlank()) {
            return imageUrl;
        }
        String lower = (fishType != null) ? fishType.toLowerCase() : "";
        if (lower.contains("tilapia")) {
            return "https://images.unsplash.com/photo-1544551763-46a013bb70d5?auto=format&fit=crop&w=800&q=80";
        } else if (lower.contains("perch") || lower.contains("mbuta")) {
            return "https://images.unsplash.com/photo-1534447677768-be436bb09401?auto=format&fit=crop&w=800&q=80";
        } else if (lower.contains("snapper") || lower.contains("red")) {
            return "https://images.unsplash.com/photo-1524704654690-b56c05c78a00?auto=format&fit=crop&w=800&q=80";
        } else if (lower.contains("catfish") || lower.contains("kamongo") || lower.contains("mumi")) {
            return "https://images.unsplash.com/photo-1510137452627-77b31278f244?auto=format&fit=crop&w=800&q=80";
        } else if (lower.contains("omena") || lower.contains("dagaa") || lower.contains("sardine")) {
            return "https://images.unsplash.com/photo-1615141982883-c7ad0e69fd62?auto=format&fit=crop&w=800&q=80";
        } else if (lower.contains("tuna") || lower.contains("kingfish")) {
            return "https://images.unsplash.com/photo-1509722747041-616f39b57569?auto=format&fit=crop&w=800&q=80";
        } else if (lower.contains("lobster") || lower.contains("prawn") || lower.contains("shrimp")) {
            return "https://images.unsplash.com/photo-1565680018434-b513d5e5fd47?auto=format&fit=crop&w=800&q=80";
        }
        return "https://images.unsplash.com/photo-1544551763-46a013bb70d5?auto=format&fit=crop&w=800&q=80";
    }

    public String getStatusBadgeClass() {
        if (status == null) return "badge-default";
        switch (status) {
            case READY: return "badge-ready";
            case CAUGHT: return "badge-caught";
            case GOING_FISHING: return "badge-going";
            case SOLD: return "badge-sold";
            default: return "badge-default";
        }
    }

    public String getStatusLabel() {
        if (status == null) return "Unknown";
        switch (status) {
            case READY: return "Ready for Pickup";
            case CAUGHT: return "Fresh Catch Caught";
            case GOING_FISHING: return "Going Fishing (Pre-order)";
            case SOLD: return "Sold Out";
            default: return status.name();
        }
    }
}
