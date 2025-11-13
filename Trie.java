import java.util.*;

/**
 * Trie Data Structure for Symptom Auto-complete
 * 
 * Purpose: Efficiently store and search symptoms with prefix matching
 * 
 * Time Complexities:
 * - Insert: O(m) where m = length of symptom string
 * - Search: O(m) where m = length of symptom string
 * - StartsWith (Prefix Search): O(p) where p = length of prefix
 * - GetSuggestions: O(p + n) where p = prefix length, n = number of matching symptoms
 * 
 * Space Complexity: O(ALPHABET_SIZE * N * M) where N = number of symptoms, M = average length
 *                   In practice: O(26 * N * M) for lowercase English letters
 */
public class Trie {
    private TrieNode root;
    
    /**
     * Constructor initializes empty Trie
     * Time Complexity: O(1)
     */
    public Trie() {
        root = new TrieNode();
    }
    
    /**
     * Insert a symptom into the Trie
     * Time Complexity: O(m) where m = length of symptom
     * Space Complexity: O(m) in worst case (all new nodes)
     * 
     * @param symptom The symptom string to insert
     */
    public void insert(String symptom) {
        TrieNode current = root;
        String lowerSymptom = symptom.toLowerCase();
        
        // Traverse/create path for each character
        for (int i = 0; i < lowerSymptom.length(); i++) {
            char ch = lowerSymptom.charAt(i);
            
            // Skip non-alphabetic characters
            if (!Character.isLetter(ch)) continue;
            
            int index = ch - 'a';
            
            if (current.children[index] == null) {
                current.children[index] = new TrieNode();
            }
            current = current.children[index];
        }
        
        current.isEndOfWord = true;
        current.symptom = symptom; // Store original symptom
    }
    
    /**
     * Search for exact symptom match
     * Time Complexity: O(m) where m = length of symptom
     * 
     * @param symptom The symptom to search for
     * @return true if symptom exists, false otherwise
     */
    public boolean search(String symptom) {
        TrieNode node = searchNode(symptom.toLowerCase());
        return node != null && node.isEndOfWord;
    }
    
    /**
     * Check if any symptom starts with given prefix
     * Time Complexity: O(p) where p = length of prefix
     * 
     * @param prefix The prefix to check
     * @return true if prefix exists, false otherwise
     */
    public boolean startsWith(String prefix) {
        return searchNode(prefix.toLowerCase()) != null;
    }
    
    /**
     * Helper method to find node at end of given string
     * Time Complexity: O(m) where m = length of string
     * 
     * @param str The string to search for
     * @return TrieNode at end of string, or null if not found
     */
    private TrieNode searchNode(String str) {
        TrieNode current = root;
        
        for (int i = 0; i < str.length(); i++) {
            char ch = str.charAt(i);
            
            if (!Character.isLetter(ch)) continue;
            
            int index = ch - 'a';
            
            if (current.children[index] == null) {
                return null;
            }
            current = current.children[index];
        }
        
        return current;
    }
    
    /**
     * Get all symptom suggestions for a given prefix
     * Time Complexity: O(p + n) where p = prefix length, n = number of suggestions
     * Space Complexity: O(n) for storing suggestions
     * 
     * @param prefix The prefix to get suggestions for
     * @return List of matching symptoms
     */
    public List<String> getSuggestions(String prefix) {
        List<String> suggestions = new ArrayList<>();
        TrieNode node = searchNode(prefix.toLowerCase());
        
        if (node == null) {
            return suggestions;
        }
        
        // DFS to collect all symptoms with this prefix
        collectSymptoms(node, suggestions);
        return suggestions;
    }
    
    /**
     * DFS helper to collect all symptoms from a node
     * Time Complexity: O(n) where n = number of symptoms in subtree
     * 
     * @param node Current node
     * @param symptoms List to collect symptoms
     */
    private void collectSymptoms(TrieNode node, List<String> symptoms) {
        if (node == null) return;
        
        if (node.isEndOfWord) {
            symptoms.add(node.symptom);
        }
        
        for (int i = 0; i < 26; i++) {
            if (node.children[i] != null) {
                collectSymptoms(node.children[i], symptoms);
            }
        }
    }
    
    /**
     * Get total number of symptoms in Trie
     * Time Complexity: O(N * M) where N = symptoms, M = avg length
     * 
     * @return Count of symptoms
     */
    public int size() {
        return countSymptoms(root);
    }
    
    private int countSymptoms(TrieNode node) {
        if (node == null) return 0;
        
        int count = node.isEndOfWord ? 1 : 0;
        
        for (int i = 0; i < 26; i++) {
            count += countSymptoms(node.children[i]);
        }
        
        return count;
    }
}
