package africa.samaki.marketplace.model;

import java.util.*;

public class AfricanFisheries {

    public static class CountryData {
        private final String name;
        private final String code;
        private final String flag;
        private final String region;
        private final List<String> fishingGrounds;
        private final List<String> landingSites;
        private final List<String> keySpecies;

        public CountryData(String name, String code, String flag, String region,
                           List<String> fishingGrounds, List<String> landingSites, List<String> keySpecies) {
            this.name = name;
            this.code = code;
            this.flag = flag;
            this.region = region;
            this.fishingGrounds = fishingGrounds;
            this.landingSites = landingSites;
            this.keySpecies = keySpecies;
        }

        public String getName() { return name; }
        public String getCode() { return code; }
        public String getFlag() { return flag; }
        public String getRegion() { return region; }
        public List<String> getFishingGrounds() { return fishingGrounds; }
        public List<String> getLandingSites() { return landingSites; }
        public List<String> getKeySpecies() { return keySpecies; }
    }

    private static final List<CountryData> COUNTRIES = new ArrayList<>();
    private static final Map<String, CountryData> COUNTRY_MAP = new HashMap<>();

    static {
        // ================= EAST AFRICA =================
        add("Kenya", "KE", "\uD83C\uDDF0\uD83C\uDDEA", "East Africa",
                List.of("Lake Victoria", "Indian Ocean", "Lake Turkana", "Lake Naivasha", "Lake Baringo"),
                List.of("Mbita Point Pier", "Dunga Beach (Kisumu)", "Malindi Old Port", "Old Port Mombasa", "Shimoni Jetty", "Kalokol Pier", "Lamu Jetty"),
                List.of("Nile Tilapia", "Nile Perch (Mbuta)", "Red Snapper", "African Catfish", "Omena / Dagaa", "Yellowfin Tuna", "Kingfish"));

        add("Tanzania", "TZ", "\uD83C\uDDF9\uD83C\uDDFF", "East Africa",
                List.of("Lake Victoria", "Lake Tanganyika", "Lake Nyasa (Malawi)", "Indian Ocean", "Zanzibar Channel", "Mafia Island Waters"),
                List.of("Mwanza South Harbor", "Dar es Salaam Fish Market", "Tanga Port", "Kigoma Port", "Bagamoyo Pier", "Chake Chake (Pemba)", "Malindi Zanzibar"),
                List.of("Nile Perch", "Tilapia", "Lake Tanganyika Sardines (Kapenta/Dagaa)", "Red Snapper", "Yellowfin Tuna", "Giant Tiger Prawns", "Lobster"));

        add("Uganda", "UG", "\uD83C\uDDFA\uD83C\uDDEC", "East Africa",
                List.of("Lake Victoria", "Lake Kyoga", "Lake Albert", "Lake Edward", "Lake George", "River Nile Basin"),
                List.of("Ggaba Landing Site (Kampala)", "Kasenyi Pier (Entebbe)", "Butiaba Pier (Lake Albert)", "Jinja Pier", "Rwenshama Landing Site"),
                List.of("Nile Perch", "Nile Tilapia", "Mukene (Silver Cyprinid)", "African Catfish (Semutundu)", "Tigerfish", "Bagrus Catfish"));

        add("Rwanda", "RW", "\uD83C\uDDF7\uD83C\uDDFC", "East Africa",
                List.of("Lake Kivu", "Lake Muhazi", "Lake Mugesera"),
                List.of("Gisenyi (Rubavu) Pier", "Kibuye (Karongi) Pier", "Cyangugu (Rusizi) Pier", "Muhazi Eastern Landing"),
                List.of("Isambaza (Lake Kivu Sardine)", "Nile Tilapia", "African Catfish"));

        add("Burundi", "BI", "\uD83C\uDDE7\uD83C\uDDEE", "East Africa",
                List.of("Lake Tanganyika", "Lake Cohoha", "Lake Rweru"),
                List.of("Port de Bujumbura", "Rumonge Fishery Port", "Nyanza-Lac Landing Pier"),
                List.of("Ndakala (Tanganyika Sardine)", "Mukeke", "Tilapia", "Lates stappersii"));

        add("Somalia", "SO", "\uD83C\uDDF8\uD83C\uDDF4", "East Africa",
                List.of("Indian Ocean (Somali Basin)", "Gulf of Aden"),
                List.of("Mogadishu Port", "Kismayo Fishing Wharf", "Berbera Port", "Bosaso Fishery Jetty", "Eyl Beach"),
                List.of("Yellowfin Tuna", "Swordfish", "Spanish Mackerel", "Spiny Lobster", "Red Snapper", "Grouper"));

        add("Ethiopia", "ET", "\uD83C\uDDEA\uD83C\uDDF9", "East Africa",
                List.of("Lake Tana", "Lake Ziway", "Lake Chamo", "Lake Awassa", "Lake Abaya"),
                List.of("Bahir Dar Pier", "Ziway Fish Market Wharf", "Arba Minch Pier", "Hawassa Lakeside Market"),
                List.of("Nile Tilapia", "African Catfish", "Barbus", "Nile Perch"));

        add("Djibouti", "DJ", "\uD83C\uDDE9\uD83C\uDDEF", "East Africa",
                List.of("Gulf of Tadjoura", "Red Sea", "Bab-el-Mandeb Strait"),
                List.of("Port de Pêche de Djibouti", "Obock Landing Pier", "Tadjoura Waterfront"),
                List.of("Kingfish", "Yellowfin Tuna", "Emperor", "Grouper", "Barracuda"));

        add("Eritrea", "ER", "\uD83C\uDDEA\uD83C\uDDF7", "East Africa",
                List.of("Red Sea", "Dahlak Archipelago Waters"),
                List.of("Massawa Port", "Assab Fishing Pier", "Dahlak Kebir Jetty"),
                List.of("Red Sea Snapper", "Spanish Mackerel", "Grouper", "Prawns", "Lizardfish"));

        add("Seychelles", "SC", "\uD83C\uDDF8\uD83C\uDDE8", "East Africa",
                List.of("Western Indian Ocean (Mahé Plateau)", "Amirantes Bank"),
                List.of("Victoria Fishing Port (Mahé)", "Baie Sainte Anne (Praslin)", "La Passe (La Digue)"),
                List.of("Yellowfin Tuna", "Skipjack Tuna", "Red Snapper (Bourgeois)", "Jobfish", "Grouper"));

        add("Mauritius", "MU", "\uD83C\uDDF2\uD83C\uDDFA", "East Africa",
                List.of("Western Indian Ocean", "Nazareth Bank", "Saya de Malha Bank"),
                List.of("Port Louis Fishing Port", "Trou Fanfaron Basin", "Mahebourg Waterfront", "Grand Baie"),
                List.of("Yellowfin Tuna", "Dorado (Mahi-Mahi)", "Wahoo", "Red Snapper", "Capitaine"));

        add("Madagascar", "MG", "\uD83C\uDDF2\uD83C\uDDEC", "East Africa",
                List.of("Mozambique Channel", "Indian Ocean (Eastern Coast)"),
                List.of("Port de Toamasina (Tamatave)", "Port de Mahajanga (Majunga)", "Toliara Wharf", "Antsiranana Bay", "Nosy Be Pier"),
                List.of("Tiger Prawns (Camarón)", "Yellowfin Tuna", "Spiny Lobster", "Grouper", "Sea Cucumber", "Trevally"));

        add("Comoros", "KM", "\uD83C\uDDF0\uD83C\uDDF2", "East Africa",
                List.of("Mozambique Channel", "Indian Ocean"),
                List.of("Port de Moroni (Grande Comore)", "Port de Mutsamudu (Anjouan)", "Fomboni Beach (Mohéli)"),
                List.of("Yellowfin Tuna", "Skipjack", "Sailfish", "Wahoo", "Reef Fish"));

        add("South Sudan", "SS", "\uD83C\uDDF8\uD83C\uDDF8", "East Africa",
                List.of("Sudd Wetlands", "White Nile River", "Bahr el Ghazal Basin"),
                List.of("Juba Nile River Pier", "Bor Riverfront Pier", "Malakal Fishery Wharf"),
                List.of("Nile Tilapia", "Nile Perch", "African Catfish", "Heterotis niloticus"));

        // ================= WEST AFRICA =================
        add("Nigeria", "NG", "\uD83C\uDDF3\uD83C\uDDEC", "West Africa",
                List.of("Gulf of Guinea (Atlantic Ocean)", "Niger Delta Waters", "Lake Chad Basin", "Lagos Lagoon", "Niger & Benue Rivers"),
                List.of("Victoria Island Fishery Wharf (Lagos)", "Port Harcourt Marine Base", "Warri Waterfront Pier", "Calabar Marina", "Epe Lagoon Fish Market"),
                List.of("Atlantic Croaker", "Giant Tiger Prawns", "Catfish (Clarias)", "Tilapia", "Bonga Shad", "Barracuda", "Sole"));

        add("Ghana", "GH", "\uD83C\uDDEC\uD83C\uDDED", "West Africa",
                List.of("Gulf of Guinea (Atlantic Ocean)", "Lake Volta"),
                List.of("Tema Fishing Harbour", "Jamestown Fish Landing (Accra)", "Elmina Fishery Pier", "Sekondi-Takoradi Wharf", "Yeji Pier (Lake Volta)"),
                List.of("Red Snapper", "Cassava Croaker", "Volta Tilapia", "Skipjack Tuna", "Sardinella (Herring)", "Mackerel", "Shrimp"));

        add("Senegal", "SN", "\uD83C\uDDF8\uD83C\uDDF3", "West Africa",
                List.of("Atlantic Ocean", "Senegal River", "Saloum Delta", "Casamance River"),
                List.of("Soumbédioune Beach (Dakar)", "Port de Pêche de Saint-Louis", "Mbour Fishery Pier", "Kayar Landing Beach", "Joal-Fadiouth Wharf"),
                List.of("Thiof (White Grouper)", "Sardinella", "Octopus", "Cuttlefish", "Sole", "Red Mullet", "Shrimp"));

        add("Côte d'Ivoire", "CI", "\uD83C\uDDE8\uD83C\uDDEE", "West Africa",
                List.of("Gulf of Guinea (Atlantic Ocean)", "Lake Kossou", "Ébrié Lagoon"),
                List.of("Port de Pêche d'Abidjan", "Port de San-Pédro", "Sassandra Fishing Pier", "Grand-Bassam Landing Site"),
                List.of("Yellowfin Tuna", "Skipjack", "Sardinella", "Croaker (Capitaine de Mer)", "Lagoon Tilapia", "Shrimp"));

        add("Cameroon", "CM", "\uD83C\uDDE8\uD83C\uDDF2", "West Africa",
                List.of("Gulf of Guinea (Atlantic Ocean)", "Wouri Estuary", "Lake Chad Basin", "Sanaga River"),
                List.of("Youpwè Fishing Pier (Douala)", "Limbe Down Beach", "Kribi Fishery Wharf", "Idenau Beach Pier"),
                List.of("Cameroon Ghost Shrimp", "Atlantic Croaker", "Catfish", "Tilapia", "Bonga Shad", "Threadfin (Polydactylus)"));

        add("Sierra Leone", "SL", "\uD83C\uDDF8\uD83C\uDDF1", "West Africa",
                List.of("Atlantic Ocean", "Sherbro River Estuary", "Sierra Leone River Estuary"),
                List.of("Tombo Fishing Beach (Freetown)", "Goderich Landing Pier", "Bonthe Island Wharf", "Shenge Landing Site"),
                List.of("Bonga Shad", "Herring", "Snapper", "Ladyfish", "Barracuda", "Crayfish / Shrimp"));

        add("Liberia", "LR", "\uD83C\uDDF1\uD83C\uDDF7", "West Africa",
                List.of("Atlantic Ocean", "Mesurado River", "Saint Paul River"),
                List.of("West Point Fishery Wharf (Monrovia)", "Buchanan Fishing Pier", "Harper Port Pier", "Greenville Wharf"),
                List.of("Atlantic Croaker", "Cavalla (Jack Mackerel)", "Red Snapper", "Barracuda", "Lobster", "Shrimp"));

        add("The Gambia", "GM", "\uD83C\uDDEC\uD83C\uDDF2", "West Africa",
                List.of("Atlantic Ocean", "River Gambia Estuary"),
                List.of("Tanji Fishing Beach", "Bakau Fish Pier", "Gunjur Fishery Landing", "Brikama Wharf", "Banjul Fishing Port"),
                List.of("Ladyfish", "Red Snapper", "Bonga Shad", "Sardinella", "Butterfish", "Tiger Shrimp"));

        add("Benin", "BJ", "\uD83C\uDDE7\uD83C\uDDEF", "West Africa",
                List.of("Gulf of Guinea (Atlantic Ocean)", "Lake Nokoué", "Porto-Novo Lagoon"),
                List.of("Port de Pêche de Cotonou", "Ganvié Water Market Pier", "Grand-Popo Beach Wharf"),
                List.of("Croaker", "Tilapia (Akpavi)", "Catfish", "Sardinella", "Crabs", "Lagoon Shrimp"));

        add("Togo", "TG", "\uD83C\uDDF9\uD83C\uDDEC", "West Africa",
                List.of("Gulf of Guinea (Atlantic Ocean)", "Lake Togo"),
                List.of("Nouveau Port de Pêche de Lomé", "Aného Beach Wharf", "Togoville Pier"),
                List.of("Sardinella", "Mackerel", "Sea Bream", "Tilapia", "Catfish", "Shrimp"));

        add("Guinea", "GN", "\uD83C\uDDEC\uD83C\uDDF3", "West Africa",
                List.of("Atlantic Ocean", "Rio Nuñez Estuary"),
                List.of("Port de Boulbinet (Conakry)", "Port de Kamsar", "Port de Boffa", "Téminètaye Landing Pier"),
                List.of("Croaker", "Sole", "Cuttlefish", "Grouper", "Snapper", "White Shrimp"));

        add("Guinea-Bissau", "GW", "\uD83C\uDDEC\uD83C\uDDFC", "West Africa",
                List.of("Atlantic Ocean", "Bijagós Archipelago Waters", "Geba River"),
                List.of("Port de Pêche de Bissau (Pidjiguiti)", "Bubaque Island Wharf", "Cacheu Pier"),
                List.of("Red Snapper", "Sole", "Octopus", "Shrimp", "Cuttlefish", "Croaker"));

        add("Mauritania", "MR", "\uD83C\uDDF2\uD83C\uDDF7", "West Africa",
                List.of("Atlantic Ocean (Banc d'Arguin)", "Cap Blanc Waters", "Senegal River Border"),
                List.of("Port Autonome de Nouadhibou", "Marché de Poisson de Nouakchott Pier", "Baie du Lévrier"),
                List.of("Octopus (Poulpe)", "Cuttlefish", "Sardines", "Hake", "Sea Bream", "Horse Mackerel", "Lobster"));

        add("Cape Verde", "CV", "\uD83C\uDDE8\uD83C\uDDF8", "West Africa",
                List.of("Atlantic Ocean (Cape Verde Archipelago)", "São Vicente Shelf"),
                List.of("Porto Grande (Mindelo, São Vicente)", "Cais da Praia (Santiago)", "Palmeira Pier (Sal)", "Sal Rei (Boa Vista)"),
                List.of("Yellowfin Tuna", "Skipjack", "Bigeye Tuna", "Wahoo (Serra)", "Mackerel", "Spiny Lobster"));

        add("Mali", "ML", "\uD83C\uDDF2\uD83C\uDDF1", "West Africa",
                List.of("Niger River Inland Delta", "Lake Débo", "Sankarani River / Sélingué Dam"),
                List.of("Port de Mopti", "Ségou River Pier", "Koulikoro Port", "Sélingué Fishery Landing"),
                List.of("Capitaine (Nile Perch)", "Tilapia", "African Catfish (Clarias)", "Heterotis niloticus"));

        add("Niger", "NE", "\uD83C\uDDF3\uD83C\uDDEA", "West Africa",
                List.of("Niger River Basin", "Lake Chad Basin (Western Fringe)"),
                List.of("Niamey Riverfront Wharf", "Gaya Fishery Pier", "Diffa Lakefront Landing"),
                List.of("Nile Tilapia", "African Catfish", "Capitaine", "Labeo"));

        add("Burkina Faso", "BF", "\uD83C\uDDE7\uD83C\uDDEB", "West Africa",
                List.of("Lake Kompienga", "Lake Bagré", "Mouhoun (Black Volta) River"),
                List.of("Kompienga Fishery Landing Pier", "Bagré Dam Pier", "Sourou Valley Fishery"),
                List.of("Tilapia", "African Catfish", "Heterotis", "Clarias"));

        // ================= NORTH AFRICA =================
        add("Egypt", "EG", "\uD83C\uDDF8\uD83C\uDDEC", "North Africa",
                List.of("Mediterranean Sea", "Red Sea", "Lake Nasser", "River Nile Basin", "Lake Manzala", "Lake Burullus"),
                List.of("Anfoushi Fishing Port (Alexandria)", "Port Said Fishery Wharf", "Hurghada Red Sea Marina", "Aswan High Dam Pier", "Damietta Port", "Suez Fishing Pier"),
                List.of("European Sea Bass (Qarous)", "Gilthead Sea Bream (Dourada)", "Red Sea Grouper (Nagil)", "Nile Tilapia (Bolti)", "Mullet (Bouri)", "Red Sea Prawns"));

        add("Morocco", "MA", "\uD83C\uDDF2\uD83C\uDDE6", "North Africa",
                List.of("Atlantic Ocean (Canary Current Upwelling)", "Mediterranean Sea", "Strait of Gibraltar"),
                List.of("Port d'Agadir", "Port de Casablanca", "Port de Dakhla", "Port de Safi", "Port de Tan-Tan", "Port de Tanger Med", "Essaouira"),
                List.of("European Sardines (Sardina pilchardus)", "Atlantic Mackerel", "Anchovy", "Octopus", "Hake", "Sea Bream", "Swordfish"));

        add("Tunisia", "TN", "\uD83C\uDDF9\uD83C\uDDF3", "North Africa",
                List.of("Mediterranean Sea", "Gulf of Gabès", "Gulf of Tunis", "Gulf of Hammamet"),
                List.of("Port de Pêche de Sfax", "Port de Bizerte", "Port de Kelibia", "Port de Mahdia", "Port de Tabarka", "Zarzis Fishing Pier"),
                List.of("Bluefin Tuna", "Sea Bream (Dorade)", "Sea Bass (Loup de Mer)", "Royal Red Shrimp", "Octopus", "Sardines"));

        add("Algeria", "DZ", "\uD83C\uDDF9\uD83C\uDDE7", "North Africa",
                List.of("Mediterranean Sea (Algerian Basin)"),
                List.of("Port de Pêche d'Alger", "Port d'Oran", "Port d'Annaba", "Port de Bou Haroun", "Port de Ghazaouet", "Béjaïa Port"),
                List.of("Sardines", "Swordfish (Espadon)", "Bluefin Tuna", "Hake (Merlu)", "Red Mullet (Rouget)", "Prawns (Crevettes)"));

        add("Libya", "LY", "\uD83C\uDDF1\uD83C\uDDFE", "North Africa",
                List.of("Mediterranean Sea (Gulf of Sirte)"),
                List.of("Tripoli Fishing Harbour", "Benghazi Port Marina", "Misrata Fishery Pier", "Al Khoms Fishing Port"),
                List.of("Bluefin Tuna", "Grouper (Cherne)", "Sea Bream", "Swordfish", "Red Mullet", "Shrimp"));

        add("Sudan", "SD", "\uD83C\uDDF8\uD83C\uDDE9", "North Africa",
                List.of("Red Sea", "River Nile Basin", "Lake Nubia"),
                List.of("Port Sudan Fishing Pier", "Suakin Historic Harbor", "Wadi Halfa Pier (Lake Nubia)"),
                List.of("Coral Trout / Grouper (Najil)", "Spanish Mackerel", "Emperor", "Nile Tilapia", "Nile Perch"));

        // ================= SOUTHERN AFRICA =================
        add("South Africa", "ZA", "\uD83C\uDDFF\uD83C\uDDE6", "Southern Africa",
                List.of("Atlantic Ocean (Benguela Upwelling)", "Indian Ocean (Agulhas Current)", "Cape of Good Hope Marine Shelf"),
                List.of("Cape Town Harbour", "Hout Bay Fishing Harbour", "Durban Bay Pier", "Port Elizabeth (Gqeberha) Port", "Saldanha Bay", "Mossel Bay Pier"),
                List.of("Cape Hake (Merluccius capensis)", "Kingklip", "Snoek", "Cape Rock Lobster (Kreef)", "Yellowtail", "Pilchards", "Squid (Chokka)"));

        add("Namibia", "NA", "\uD83C\uDDF3\uD83C\uDDE6", "Southern Africa",
                List.of("Atlantic Ocean (Benguela Marine Ecosystem)"),
                List.of("Walvis Bay Fishing Port", "Lüderitz Harbour Pier"),
                List.of("Cape Hake (Deep Sea)", "Monkfish", "Horse Mackerel", "Orange Roughy", "Pilchards", "Rock Lobster", "Cape Snoek"));

        add("Mozambique", "MZ", "\uD83C\uDDF2\uD83C\uDDFF", "Southern Africa",
                List.of("Mozambique Channel (Indian Ocean)", "Lake Cahora Bassa", "Lake Niassa (Malawi)"),
                List.of("Porto de Pesca de Maputo", "Porto da Beira", "Baía de Nacala", "Porto de Pemba", "Songo Pier (Cahora Bassa)", "Angoche Pier"),
                List.of("Giant Tiger Prawns", "White Prawns", "Yellowfin Tuna", "Kapenta (Cahora Bassa Sardine)", "Red Snapper", "Crayfish", "Grouper"));

        add("Angola", "AO", "\uD83C\uDDE6\uD83C\uDDF4", "Southern Africa",
                List.of("Atlantic Ocean (Angola Current & Benguela Front)"),
                List.of("Ilha de Luanda Fishing Pier", "Porto do Lobito", "Baía Farta (Benguela)", "Porto do Namibe", "Soyo Port Pier"),
                List.of("Horse Mackerel (Carapau)", "Sardinella", "Cape Hake", "Red Snapper (Cachucho)", "Deep Sea Crab", "Shrimp"));

        add("Malawi", "MW", "\uD83C\uDDF2\uD83C\uDDFC", "Southern Africa",
                List.of("Lake Malawi (Lake Nyasa)", "Lake Malombe", "Shire River Basin"),
                List.of("Nkhata Bay Landing Pier", "Mangochi Boma Beach", "Salima Senga Bay Pier", "Nkhotakota Wharf", "Cape Maclear Fishery"),
                List.of("Chambo (Oreochromis karongae)", "Usipa (Lake Sardine)", "Mpasa (Lake Salmon)", "Kampango Catfish", "Ndunduma"));

        add("Zambia", "ZM", "\uD83C\uDDFF\uD83C\uDDF2", "Southern Africa",
                List.of("Lake Kariba", "Lake Tanganyika (Southern Tip)", "Lake Bangweulu", "Lake Mweru", "Kafue River Basin"),
                List.of("Siavonga Pier (Lake Kariba)", "Mpulungu Harbour (Lake Tanganyika)", "Samfya Beach (Lake Bangweulu)", "Nchelenge Pier (Lake Mweru)"),
                List.of("Kapenta (Tanganyika/Kariba Sardine)", "Bukabuka (Lates mariae)", "Bream / Tilapia", "Tigerfish", "Catfish (Mulamba)"));

        add("Zimbabwe", "ZW", "\uD83C\uDDFF\uD83C\uDDFC", "Southern Africa",
                List.of("Lake Kariba", "Zambezi River Basin", "Lake Chivero"),
                List.of("Kariba Town Bream Bay Pier", "Binga Fishery Wharf", "Msuna Zambezi Camp Pier", "Andora Harbour"),
                List.of("Kapenta (Limnothrissa miodon)", "Kariba Bream (Tilapia)", "Tigerfish", "Vundu Catfish", "Barbel"));

        add("Botswana", "BW", "\uD83C\uDDE7\uD83C\uDDFC", "Southern Africa",
                List.of("Okavango Delta", "Chobe River", "Lake Ngami"),
                List.of("Maun Waterfront Landing", "Kasane Chobe River Pier", "Sehitwa (Lake Ngami)"),
                List.of("Three-Spot Tilapia", "Sharptooth Catfish", "Tigerfish", "Nembwe (Yellow-Belly Bream)"));

        // ================= CENTRAL AFRICA =================
        add("Democratic Republic of Congo", "CD", "\uD83C\uDDE8\uD83C\uDDF9", "Central Africa",
                List.of("Lake Tanganyika", "Lake Albert", "Lake Kivu", "Congo River Basin", "Atlantic Ocean (Muanda Coast)"),
                List.of("Port de Kalemie (Lake Tanganyika)", "Port d'Uvira", "Muanda Atlantic Fishery Wharf", "Goma Pier (Lake Kivu)", "Port de Mbandaka"),
                List.of("Mikebuka (Giant Cichlid)", "Ndakala (Tanganyika Sardine)", "Congo River Capitaine", "Tilapia", "Electric Catfish"));

        add("Republic of the Congo", "CG", "\uD83C\uDDE8\uD83C\uDDEC", "Central Africa",
                List.of("Atlantic Ocean", "Congo River Basin", "Kouilou River"),
                List.of("Port de Pêche de Pointe-Noire", "Beach de Brazzaville Pier", "Makotipoko Riverfront"),
                List.of("Atlantic Croaker", "Sardinella", "Threadfin", "Capitaine", "Congo River Catfish", "Shrimp"));

        add("Gabon", "GA", "\uD83C\uDDEC\uD83C\uDDF6", "Central Africa",
                List.of("Atlantic Ocean", "Komo Estuary", "Ogooué River Basin"),
                List.of("Port-Môle (Libreville)", "Port de Pêche de Port-Gentil", "Lambarene River Wharf"),
                List.of("Capitaine (Threadfin)", "Red Snapper", "Yellowfin Tuna", "Sole", "Barracuda", "Giant Coastal Prawns"));

        add("Equatorial Guinea", "GQ", "\uD83C\uDDEC\uD83C\uDDF6", "Central Africa",
                List.of("Atlantic Ocean (Gulf of Guinea)", "Bioko Island Shelf"),
                List.of("Puerto de Malabo (Bioko)", "Puerto de Bata (Continental)", "Luba Pier"),
                List.of("Yellowfin Tuna", "Red Snapper", "Atlantic Croaker", "Spanish Mackerel", "Shrimp"));

        add("São Tomé and Príncipe", "ST", "\uD83C\uDDF8\uD83C\uDDF9", "Central Africa",
                List.of("Gulf of Guinea (Atlantic Ocean)"),
                List.of("Praia Gambôa (São Tomé)", "Porto de Santo António (Príncipe)", "Neves Fishing Pier"),
                List.of("Flying Fish (Peixe Voador)", "Yellowfin Tuna", "Wahoo", "Sailfish", "Swordfish", "Grouper"));

        add("Chad", "TD", "\uD83C\uDDF9\uD83C\uDDE9", "Central Africa",
                List.of("Lake Chad", "Chari River", "Logone River"),
                List.of("Bol Pier (Lake Chad)", "N'Djamena Chari Riverfront", "Mani Fishery Landing"),
                List.of("Capitaine (Nile Perch)", "Nile Tilapia", "African Catfish (Clarias)", "Heterotis"));

        add("Central African Republic", "CF", "\uD83C\uDDE8\uD83C\uDDF1", "Central Africa",
                List.of("Ubangi River", "Sangha River", "Chari River Basin"),
                List.of("Port Amont de Bangui", "Port de Mobaye", "Nola Riverfront Pier"),
                List.of("Ubangi Capitaine", "Catfish", "Tilapia", "Distichodus"));
    }

    private static void add(String name, String code, String flag, String region,
                            List<String> grounds, List<String> landingSites, List<String> keySpecies) {
        CountryData data = new CountryData(name, code, flag, region, grounds, landingSites, keySpecies);
        COUNTRIES.add(data);
        COUNTRY_MAP.put(name.toLowerCase(), data);
    }

    public static List<CountryData> getAllCountries() {
        return Collections.unmodifiableList(COUNTRIES);
    }

    public static List<String> getAllCountryNames() {
        List<String> names = new ArrayList<>();
        for (CountryData c : COUNTRIES) {
            names.add(c.getName());
        }
        return names;
    }

    public static CountryData getCountry(String name) {
        if (name == null) return null;
        return COUNTRY_MAP.get(name.trim().toLowerCase());
    }

    public static List<String> getGroundsForCountry(String name) {
        CountryData c = getCountry(name);
        return (c != null) ? c.getFishingGrounds() : List.of("Lake Victoria", "Indian Ocean", "Atlantic Ocean", "River Basin");
    }

    public static List<String> getLandingSitesForCountry(String name) {
        CountryData c = getCountry(name);
        return (c != null) ? c.getLandingSites() : List.of("Commercial Fishery Pier", "Central Dock");
    }

    public static List<String> getMajorBasins() {
        return List.of(
                "Lake Victoria",
                "Western Indian Ocean",
                "Atlantic Ocean (Gulf of Guinea)",
                "Lake Tanganyika",
                "Atlantic Ocean (Benguela Current)",
                "Mediterranean Sea",
                "Lake Malawi / Nyasa",
                "Red Sea",
                "Lake Turkana",
                "Lake Chad",
                "Lake Volta",
                "Lake Kariba"
        );
    }
}
