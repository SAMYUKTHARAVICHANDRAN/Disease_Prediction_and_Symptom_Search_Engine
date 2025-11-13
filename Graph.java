import java.util.*;

/**
 * Graph Data Structure for Disease-Symptom Relationships
 * 
 * Purpose: Model relationships between diseases and symptoms
 * Used for PageRank algorithm to rank disease importance
 * 
 * Structure: Adjacency List representation
 * - Each disease is a vertex
 * - Edges represent shared symptoms between diseases
 * - Edge weight = number of shared symptoms
 * 
 * Time Complexity: O(V + E) for most operations where V = vertices, E = edges
 * Space Complexity: O(V + E) for adjacency list storage
 */
public class Graph {
    private Map<String, List<Edge>> adjacencyList;
    private Map<String, Set<String>> diseaseSymptoms;
    private int vertexCount;
  
    /**
     * Edge class represents connection between diseases
     */
    public static class Edge {
        String destination;
        int weight; // Number of shared symptoms
        
        public Edge(String destination, int weight) {
            this.destination = destination;
            this.weight = weight;
        }
    }
    
    /**
     * Constructor initializes empty graph
     * Time Complexity: O(1)
     */
    public Graph() {
        adjacencyList = new HashMap<>();
        diseaseSymptoms = new HashMap<>();
        vertexCount = 0;
    }
    
    /**
     * Add a disease (vertex) to the graph
     * Time Complexity: O(1) average case for HashMap operations
     * 
     * @param disease Name of the disease
     * @param symptoms Set of symptoms for this disease
     */
    public void addDisease(String disease, Set<String> symptoms) {
        if (!adjacencyList.containsKey(disease)) {
            adjacencyList.put(disease, new ArrayList<>());
            diseaseSymptoms.put(disease, new HashSet<>(symptoms));
            vertexCount++;
        }
    }
    
    /**
     * Build edges between diseases based on shared symptoms
     * Time Complexity: O(V^2 * S) where V = diseases, S = avg symptoms per disease
     * Space Complexity: O(E) where E = number of edges created
     * 
     * This creates a weighted graph where edge weight = shared symptom count
     */
    public void buildEdges() {
        List<String> diseases = new ArrayList<>(adjacencyList.keySet());
        
        // Compare each pair of diseases
        for (int i = 0; i < diseases.size(); i++) {
            for (int j = i + 1; j < diseases.size(); j++) {
                String disease1 = diseases.get(i);
                String disease2 = diseases.get(j);
                
                // Count shared symptoms
                int sharedCount = countSharedSymptoms(disease1, disease2);
                
                if (sharedCount > 0) {
                    // Add bidirectional edges
                    adjacencyList.get(disease1).add(new Edge(disease2, sharedCount));
                    adjacencyList.get(disease2).add(new Edge(disease1, sharedCount));
                }
            }
        }
    }
    
    /**
     * Count shared symptoms between two diseases
     * Time Complexity: O(min(S1, S2)) where S1, S2 are symptom counts
     * 
     * @param disease1 First disease
     * @param disease2 Second disease
     * @return Number of shared symptoms
     */
    private int countSharedSymptoms(String disease1, String disease2) {
        Set<String> symptoms1 = diseaseSymptoms.get(disease1);
        Set<String> symptoms2 = diseaseSymptoms.get(disease2);
        
        int count = 0;
        for (String symptom : symptoms1) {
            if (symptoms2.contains(symptom)) {
                count++;
            }
        }
        return count;
    }
    
    /**
     * PageRank Algorithm Implementation
     * 
     * Purpose: Rank diseases by importance based on their connections
     * Diseases connected to many other diseases (via shared symptoms) rank higher
     * 
     * Time Complexity: O(I * V * E) where:
     *   I = number of iterations (typically 20-30)
     *   V = number of vertices (diseases)
     *   E = average edges per vertex
     * 
     * Space Complexity: O(V) for storing ranks
     * 
     * @param iterations Number of iterations to run (more = more accurate)
     * @param dampingFactor Probability of following links (typically 0.85)
     * @return Map of disease names to their PageRank scores
     */
    public Map<String, Double> pageRank(int iterations, double dampingFactor) {
        Map<String, Double> ranks = new HashMap<>();
        Map<String, Double> newRanks = new HashMap<>();
        
        // Initialize: Each disease starts with equal rank
        // Initial rank = 1.0 / number of diseases
        double initialRank = 1.0 / vertexCount;
        for (String disease : adjacencyList.keySet()) {
            ranks.put(disease, initialRank);
        }
        
        // Iterate to converge on final ranks
        for (int iter = 0; iter < iterations; iter++) {
            // Calculate new rank for each disease
            for (String disease : adjacencyList.keySet()) {
                double rank = (1 - dampingFactor) / vertexCount;
                
                // Add contributions from incoming edges
                for (String source : adjacencyList.keySet()) {
                    if (source.equals(disease)) continue;
                    
                    // Find edge from source to current disease
                    List<Edge> edges = adjacencyList.get(source);
                    int totalWeight = getTotalWeight(source);
                    
                    for (Edge edge : edges) {
                        if (edge.destination.equals(disease)) {
                            // Contribution = (source rank * edge weight) / total outgoing weight
                            rank += dampingFactor * ranks.get(source) * 
                                    ((double) edge.weight / totalWeight);
                        }
                    }
                }
                
                newRanks.put(disease, rank);
            }
            
            // Update ranks for next iteration
            ranks = new HashMap<>(newRanks);
        }
        
        return ranks;
    }
    
    /**
     * Get total weight of outgoing edges from a disease
     * Time Complexity: O(E) where E = edges from this vertex
     * 
     * @param disease Disease name
     * @return Sum of all outgoing edge weights
     */
    private int getTotalWeight(String disease) {
        int total = 0;
        for (Edge edge : adjacencyList.get(disease)) {
            total += edge.weight;
        }
        return total == 0 ? 1 : total; // Avoid division by zero
    }
    
    /**
     * Get symptoms for a disease
     * Time Complexity: O(1) average case
     * 
     * @param disease Disease name
     * @return Set of symptoms
     */
    public Set<String> getSymptoms(String disease) {
        return diseaseSymptoms.getOrDefault(disease, new HashSet<>());
    }
    
    /**
     * Get all diseases in graph
     * Time Complexity: O(1)
     * 
     * @return Set of disease names
     */
    public Set<String> getDiseases() {
        return adjacencyList.keySet();
    }
    
    /**
     * Print graph structure (for debugging)
     * Time Complexity: O(V + E)
     */
    public void printGraph() {
        for (String disease : adjacencyList.keySet()) {
            System.out.print(disease + " -> ");
            for (Edge edge : adjacencyList.get(disease)) {
                System.out.print(edge.destination + "(" + edge.weight + ") ");
            }
            System.out.println();
        }
    }
}
