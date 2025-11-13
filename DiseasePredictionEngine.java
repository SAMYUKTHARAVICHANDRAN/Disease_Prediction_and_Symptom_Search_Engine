import java.util.*;

/**
 * Disease Prediction Engine
 *
 * Combines Trie (autocomplete) + Graph (relationships) + PageRank (ranking)
 * to predict diseases based on user-entered symptoms.
 */
public class DiseasePredictionEngine {
    private Trie symptomTrie;
    private Graph diseaseGraph;
    private Map<String, Double> pageRankScores;
    private Map<String, DiseaseInfo> diseaseDatabase;

    /**
     * DiseaseInfo class stores disease details
     */
    public static class DiseaseInfo {
        String name;
        Set<String> symptoms;
        String description;

        public DiseaseInfo(String name, Set<String> symptoms, String description) {
            this.name = name;
            this.symptoms = symptoms;
            this.description = description;
        }
    }

    /**
     * DiseasePrediction class stores prediction results
     */
    public static class DiseasePrediction {
        String disease;
        double confidence;
        int matchedSymptoms;
        int totalSymptoms;
        String description;

        public DiseasePrediction(String disease, double confidence, int matched,
                                 int total, String description) {
            this.disease = disease;
            this.confidence = confidence;
            this.matchedSymptoms = matched;
            this.totalSymptoms = total;
            this.description = description;
        }

        @Override
        public String toString() {
            return String.format("%s (%.1f%% confidence) - %d/%d symptoms matched\n%s",
                disease, confidence * 100, matchedSymptoms, totalSymptoms, description);
        }
    }

    /**
     * Constructor initializes the engine
     */
    public DiseasePredictionEngine() {
        symptomTrie = new Trie();
        diseaseGraph = new Graph();
        diseaseDatabase = new HashMap<>();

        // Initialize disease database
        initializeDiseaseData();

        // Build Trie for autocomplete
        buildSymptomTrie();

        // Build Graph and calculate PageRank
        buildDiseaseGraph();
        pageRankScores = diseaseGraph.pageRank(20, 0.85);

        // Normalize PageRank scores to 0-1 range
        normalizePageRankScores();
    }

    /**
     * **UPDATED:** Initialize disease database with the larger dataset from index.html
     */
    private void initializeDiseaseData() {
        addDisease("Common Cold", new HashSet<>(Arrays.asList("cough", "sore throat", "runny nose", "sneezing")), "A mild viral infection of the nose and throat.");
        addDisease("Flu", new HashSet<>(Arrays.asList("fever", "chills", "body ache", "cough", "fatigue")), "A viral infection causing fever and body aches.");
        addDisease("COVID-19", new HashSet<>(Arrays.asList("fever", "cough", "fatigue", "loss of taste", "loss of smell")), "Respiratory illness caused by SARS-CoV-2 virus.");
        addDisease("Migraine", new HashSet<>(Arrays.asList("headache", "nausea", "sensitivity to light", "vomiting")), "Severe headache often accompanied by nausea and light sensitivity.");
        addDisease("Gastroenteritis", new HashSet<>(Arrays.asList("diarrhea", "vomiting", "abdominal pain", "fever")), "Inflammation of the stomach and intestines.");
        addDisease("Pneumonia", new HashSet<>(Arrays.asList("cough", "fever", "chest pain", "shortness of breath")), "Infection causing inflammation of the lungs.");
        addDisease("Asthma", new HashSet<>(Arrays.asList("wheezing", "shortness of breath", "chest tightness", "cough")), "Chronic lung condition causing breathing difficulties.");
        addDisease("Allergy", new HashSet<>(Arrays.asList("sneezing", "runny nose", "itchy eyes", "rash")), "Immune reaction to allergens like pollen or dust.");
        addDisease("Bronchitis", new HashSet<>(Arrays.asList("cough", "mucus", "fatigue", "chest discomfort")), "Inflammation of the bronchial tubes.");
        addDisease("Sinusitis", new HashSet<>(Arrays.asList("facial pain", "nasal congestion", "headache", "cough")), "Swelling of the sinuses causing congestion and pain.");
        addDisease("Strep Throat", new HashSet<>(Arrays.asList("sore throat", "fever", "swollen glands", "headache")), "Bacterial throat infection.");
        addDisease("UTI", new HashSet<>(Arrays.asList("painful urination", "frequent urination", "fever")), "Urinary tract infection.");
        addDisease("Hypertension", new HashSet<>(Arrays.asList("headache", "dizziness", "chest pain")), "High blood pressure condition.");
        addDisease("Diabetes", new HashSet<>(Arrays.asList("increased thirst", "frequent urination", "fatigue", "blurred vision")), "Chronic condition affecting blood sugar levels.");
        addDisease("Anxiety", new HashSet<>(Arrays.asList("restlessness", "rapid heartbeat", "fatigue", "difficulty concentrating")), "Mental health disorder causing excessive worry.");
        addDisease("Chickenpox", new HashSet<>(Arrays.asList("itchy rash", "fever", "fatigue")), "Viral infection causing rash and fever.");
        addDisease("Malaria", new HashSet<>(Arrays.asList("fever", "chills", "sweating", "headache")), "Mosquito-borne parasitic infection.");
        addDisease("Dengue", new HashSet<>(Arrays.asList("high fever", "headache", "muscle pain", "rash")), "Viral infection transmitted by mosquitoes.");
        addDisease("Tuberculosis", new HashSet<>(Arrays.asList("persistent cough", "weight loss", "fever", "night sweats")), "Bacterial infection affecting the lungs.");
        addDisease("Hepatitis B", new HashSet<>(Arrays.asList("fatigue", "nausea", "abdominal pain", "jaundice")), "Viral liver infection.");
        addDisease("Measles", new HashSet<>(Arrays.asList("fever", "rash", "cough", "runny nose")), "Highly contagious viral infection.");
        addDisease("Food Poisoning", new HashSet<>(Arrays.asList("nausea", "vomiting", "diarrhea", "abdominal pain")), "Illness from contaminated food.");
        addDisease("Hypothyroidism", new HashSet<>(Arrays.asList("fatigue", "weight gain", "cold intolerance", "dry skin")), "Underactive thyroid gland.");
        addDisease("Hyperthyroidism", new HashSet<>(Arrays.asList("weight loss", "rapid heartbeat", "nervousness", "sweating")), "Overactive thyroid gland.");
        addDisease("Depression", new HashSet<>(Arrays.asList("persistent sadness", "loss of interest", "fatigue", "sleep problems")), "Mood disorder causing prolonged sadness.");
        addDisease("Ear Infection", new HashSet<>(Arrays.asList("ear pain", "hearing loss", "fever", "fluid drainage")), "Infection of the middle or outer ear.");
        addDisease("Conjunctivitis", new HashSet<>(Arrays.asList("red eyes", "itchy eyes", "tearing", "discharge")), "Inflammation of the eye’s conjunctiva.");
        addDisease("Tonsillitis", new HashSet<>(Arrays.asList("sore throat", "fever", "difficulty swallowing", "swollen tonsils")), "Inflammation of the tonsils.");
        addDisease("Cold Sores", new HashSet<>(Arrays.asList("blisters", "tingling", "itching", "pain")), "Herpes simplex virus infection around lips.");
        addDisease("Shingles", new HashSet<>(Arrays.asList("painful rash", "blisters", "itching", "burning sensation")), "Reactivation of the chickenpox virus.");
        addDisease("Laryngitis", new HashSet<>(Arrays.asList("hoarseness", "loss of voice", "sore throat", "cough")), "Inflammation of the voice box.");
        addDisease("Eczema", new HashSet<>(Arrays.asList("itchy skin", "redness", "dry patches")), "Chronic skin condition causing inflammation.");
        addDisease("Psoriasis", new HashSet<>(Arrays.asList("red patches", "scaly skin", "itching")), "Autoimmune skin disorder causing thick skin patches.");
        addDisease("Acne", new HashSet<>(Arrays.asList("pimples", "blackheads", "whiteheads", "redness")), "Skin condition causing clogged pores and pimples.");
        addDisease("Arthritis", new HashSet<>(Arrays.asList("joint pain", "stiffness", "swelling")), "Inflammation of joints causing pain and stiffness.");
        addDisease("Osteoporosis", new HashSet<>(Arrays.asList("weak bones", "fractures", "loss of height")), "Condition causing bones to become weak and brittle.");
        addDisease("Back Pain", new HashSet<>(Arrays.asList("lower back pain", "stiffness", "muscle ache")), "Pain in the back muscles or spine.");
        addDisease("Obesity", new HashSet<>(Arrays.asList("weight gain", "fatigue", "shortness of breath")), "Excess body fat affecting health.");
        addDisease("Anemia", new HashSet<>(Arrays.asList("fatigue", "pale skin", "shortness of breath", "dizziness")), "Low red blood cell count causing weakness.");
        addDisease("Vitamin D Deficiency", new HashSet<>(Arrays.asList("bone pain", "muscle weakness", "fatigue")), "Lack of vitamin D affecting bone health.");
        addDisease("Vitamin B12 Deficiency", new HashSet<>(Arrays.asList("fatigue", "numbness", "weakness", "memory problems")), "Lack of vitamin B12 causing nerve issues.");
        addDisease("Iron Deficiency", new HashSet<>(Arrays.asList("fatigue", "pale skin", "shortness of breath")), "Low iron levels affecting red blood cells.");
        addDisease("Heat Stroke", new HashSet<>(Arrays.asList("high body temperature", "dizziness", "confusion", "nausea")), "Serious heat-related illness.");
        addDisease("Hypothermia", new HashSet<>(Arrays.asList("shivering", "confusion", "slurred speech", "weak pulse")), "Dangerously low body temperature.");
        addDisease("Whooping Cough", new HashSet<>(Arrays.asList("severe cough", "runny nose", "fever", "vomiting")), "Highly contagious bacterial infection of the respiratory tract.");
        addDisease("Polio", new HashSet<>(Arrays.asList("fever", "fatigue", "muscle weakness", "paralysis")), "Viral infection affecting the nervous system.");
        addDisease("Mumps", new HashSet<>(Arrays.asList("swollen salivary glands", "fever", "headache", "muscle aches")), "Viral infection affecting salivary glands.");
        addDisease("Rubella", new HashSet<>(Arrays.asList("rash", "fever", "swollen lymph nodes")), "Viral infection causing mild rash and fever.");
        addDisease("Scarlet Fever", new HashSet<>(Arrays.asList("red rash", "fever", "sore throat")), "Bacterial infection causing rash and sore throat.");
    }

    /**
     * Add disease to database
     */
    private void addDisease(String name, Set<String> symptoms, String description) {
        diseaseDatabase.put(name, new DiseaseInfo(name, symptoms, description));
    }

    /**
     * Build Trie with all unique symptoms
     */
    private void buildSymptomTrie() {
        Set<String> allSymptoms = new HashSet<>();
        for (DiseaseInfo disease : diseaseDatabase.values()) {
            allSymptoms.addAll(disease.symptoms);
        }
        for (String symptom : allSymptoms) {
            symptomTrie.insert(symptom);
        }
        System.out.println("Trie built with " + allSymptoms.size() + " unique symptoms");
    }

    /**
     * Build disease graph with symptom relationships
     */
    private void buildDiseaseGraph() {
        for (DiseaseInfo disease : diseaseDatabase.values()) {
            diseaseGraph.addDisease(disease.name, disease.symptoms);
        }
        diseaseGraph.buildEdges();
        System.out.println("Graph built with " + diseaseDatabase.size() + " diseases");
    }

    /**
     * Normalize PageRank scores to 0-1 range
     */
    private void normalizePageRankScores() {
        if (pageRankScores.isEmpty()) return;
        double maxScore = Collections.max(pageRankScores.values());
        double minScore = Collections.min(pageRankScores.values());
        double range = maxScore - minScore;

        if (range == 0) return; // Avoid division by zero if all scores are the same

        for (String disease : pageRankScores.keySet()) {
            double normalized = (pageRankScores.get(disease) - minScore) / range;
            pageRankScores.put(disease, normalized);
        }
    }

    /**
     * Get autocomplete suggestions for symptom input
     */
    public List<String> getSymptomSuggestions(String prefix) {
        return symptomTrie.getSuggestions(prefix);
    }

    /**
     * Predict diseases based on selected symptoms
     */
    public List<DiseasePrediction> predictDiseases(Set<String> selectedSymptoms, int topN) {
        List<DiseasePrediction> predictions = new ArrayList<>();
        if (selectedSymptoms == null || selectedSymptoms.isEmpty()) {
            return predictions;
        }

        // Calculate score for each disease
        for (DiseaseInfo disease : diseaseDatabase.values()) {
            int matchCount = 0;
            for (String symptom : selectedSymptoms) {
                if (disease.symptoms.contains(symptom.toLowerCase())) {
                    matchCount++;
                }
            }

            if (matchCount == 0) continue; // Skip diseases with no matches

            // Calculate symptom match score (0-1)
            double symptomScore = (double) matchCount / selectedSymptoms.size();

            // Get PageRank score (0-1)
            double pageRank = pageRankScores.getOrDefault(disease.name, 0.0);

            // Combined confidence score: 70% symptom match + 30% PageRank
            double confidence = (0.7 * symptomScore) + (0.3 * pageRank);

            predictions.add(new DiseasePrediction(
                disease.name,
                confidence,
                matchCount,
                selectedSymptoms.size(),
                disease.description
            ));
        }

        // Sort by confidence (descending)
        predictions.sort((a, b) -> Double.compare(b.confidence, a.confidence));

        // Return top N predictions
        return predictions.subList(0, Math.min(topN, predictions.size()));
    }

    /**
     * Main method for testing
     */
    public static void main(String[] args) {
        System.out.println("=== Disease Prediction Engine ===\n");
        DiseasePredictionEngine engine = new DiseasePredictionEngine();

        // Test autocomplete
        System.out.println("\n--- Testing Autocomplete ---");
        String prefix = "fev";
        List<String> suggestions = engine.getSymptomSuggestions(prefix);
        System.out.println("Suggestions for '" + prefix + "': " + suggestions);

        // Test disease prediction
        System.out.println("\n--- Testing Disease Prediction ---");
        Set<String> symptoms = new HashSet<>(Arrays.asList(
            "fever", "cough", "fatigue"
        ));
        System.out.println("Selected symptoms: " + symptoms);

        List<DiseasePrediction> predictions = engine.predictDiseases(symptoms, 5);
        System.out.println("\nTop 5 Predictions:");
        for (int i = 0; i < predictions.size(); i++) {
            System.out.println("\n" + (i + 1) + ". " + predictions.get(i));
        }
    }
}