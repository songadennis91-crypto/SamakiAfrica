package africa.samaki.marketplace.controller;

import africa.samaki.marketplace.model.AfricanFisheries;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/fisheries")
public class AfricanFisheriesApiController {

    @GetMapping("/countries")
    public List<AfricanFisheries.CountryData> getAllCountries() {
        return AfricanFisheries.getAllCountries();
    }

    @GetMapping("/grounds")
    public Map<String, Object> getGroundsAndLandingSites(@RequestParam String country) {
        AfricanFisheries.CountryData data = AfricanFisheries.getCountry(country);
        Map<String, Object> response = new HashMap<>();
        if (data != null) {
            response.put("country", data.getName());
            response.put("flag", data.getFlag());
            response.put("region", data.getRegion());
            response.put("fishingGrounds", data.getFishingGrounds());
            response.put("landingSites", data.getLandingSites());
            response.put("keySpecies", data.getKeySpecies());
        } else {
            response.put("country", country);
            response.put("fishingGrounds", List.of("Coastal Waters", "Inland Lake", "River Basin"));
            response.put("landingSites", List.of("Main Fishery Pier", "Landing Beach"));
            response.put("keySpecies", List.of("Fresh Fish"));
        }
        return response;
    }

    @GetMapping("/basins")
    public List<String> getMajorBasins() {
        return AfricanFisheries.getMajorBasins();
    }
}
