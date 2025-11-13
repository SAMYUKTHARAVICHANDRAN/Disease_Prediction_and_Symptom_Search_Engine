/**
 * TrieNode class for building the Trie data structure
 * Each node represents a character in the symptom string
 * 
 * Space Complexity: O(ALPHABET_SIZE) per node = O(26) = O(1)
 */
public class TrieNode {
    private static final int ALPHABET_SIZE = 26;
    public TrieNode[] children;
    public boolean isEndOfWord;
    public String symptom; // Store complete symptom at leaf nodes
    
    /**
     * Constructor initializes the children array
     * Time Complexity: O(1)
     * Space Complexity: O(ALPHABET_SIZE) = O(26) = O(1)
     */
    public TrieNode() {
        children = new TrieNode[ALPHABET_SIZE];
        isEndOfWord = false;
        symptom = null;
    }
}
