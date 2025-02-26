import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.ArrayList;
import java.util.List;

public class TextSummarizerGUI extends JFrame {
    private JTextArea inputArea, outputArea;
    private JButton summarizeButton;
    
    public TextSummarizerGUI() {
        setTitle("Text Summarizer");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // Create a main panel with vertical BoxLayout.
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        
        // Create the input text area with a scroll pane and set a larger preferred size.
        inputArea = new JTextArea();
        JScrollPane inputScroll = new JScrollPane(inputArea);
        inputScroll.setBorder(BorderFactory.createTitledBorder("Input Text"));
        inputScroll.setPreferredSize(new Dimension(600, 200));
        
        // Create the output text area for key sentences (read-only) with a scroll pane and set a larger preferred size.
        outputArea = new JTextArea();
        outputArea.setEditable(false);
        JScrollPane outputScroll = new JScrollPane(outputArea);
        outputScroll.setBorder(BorderFactory.createTitledBorder("Key Sentences"));
        outputScroll.setPreferredSize(new Dimension(600, 200));
        
        // Create the summarize button and place it in its own panel.
        summarizeButton = new JButton("Summarize");
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(summarizeButton);
        // Restrict the button panel to the size of the button.
        Dimension buttonSize = summarizeButton.getPreferredSize();
        buttonPanel.setMaximumSize(new Dimension(buttonSize.width, buttonSize.height));
        
        // Add the components to the main panel.
        mainPanel.add(inputScroll);
        mainPanel.add(buttonPanel);
        mainPanel.add(outputScroll);
        
        // Add the main panel to the frame.
        add(mainPanel);
        
        // Set up the action listener to perform summarization when the button is clicked.
        summarizeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String text = inputArea.getText();
                // Here we request the top 15 frequent words to drive the selection.
                String summary = summarizeText(text, 15);
                outputArea.setText(summary);
            }
        });
        
        pack(); // Adjust the frame to the preferred sizes.
        setSize(600, 500); // Optionally, set an overall size.
    }
    
    /**
     * This method implements the summarization algorithm.
     * It divides the text into sentences, removes stop words,
     * counts word frequencies, selects the top frequent words, and 
     * then picks the best sentence for each top word.
     * @param text The input text to summarize.
     * @param numTopWords The number of top frequent words to consider (e.g., 15).
     * @return A bullet-point list of key sentences.
     */
    private String summarizeText(String text, int numTopWords) {
        if (text == null || text.isEmpty()) return "";
        
        // Step 1: Divide the text into sentences.
        String[] sentences = text.split("(?<=[.!?])\\s+");
        
        // Step 2: Clean the text for word frequency analysis.
        // Convert to lowercase and remove punctuation (keeping only letters and whitespace).
        String cleanedText = text.toLowerCase().replaceAll("[^a-zA-Z\\s]", " ");
        String[] words = cleanedText.split("\\s+");
        
        // Step 3: Define a set of stop words.
        Set<String> stopWords = new HashSet<>(Arrays.asList(
            "the", "and", "is", "in", "at", "of", "a", "an", "to", "it",
            "for", "on", "with", "as", "by", "this", "that", "from", "i", 
            "you", "he", "she", "they", "we", "but", "or", "if", "so", "are", 
            "was", "were", "be", "been", "being", "am", "do", "does", "did"
        ));
        
        // Step 4: Count frequency of non-stop words.
        Map<String, Integer> wordFreq = new HashMap<>();
        for (String word : words) {
            word = word.trim();
            if (word.isEmpty() || stopWords.contains(word)) continue;
            wordFreq.put(word, wordFreq.getOrDefault(word, 0) + 1);
        }
        
        // Step 5: Get the top frequent words.
        // We convert the word frequency map to a list of entries and sort it in descending order.
        List<Map.Entry<String, Integer>> freqList = new ArrayList<>(wordFreq.entrySet());
        Collections.sort(freqList, new Comparator<Map.Entry<String, Integer>>() {
            public int compare(Map.Entry<String, Integer> e1, Map.Entry<String, Integer> e2) {
                return e2.getValue() - e1.getValue();
            }
        });
        int topCount = Math.min(numTopWords, freqList.size());
        List<String> topWords = new ArrayList<>();
        for (int i = 0; i < topCount; i++) {
            topWords.add(freqList.get(i).getKey());
        }
        
        // Step 6: For each top word, select the best sentence in which it appears.
        // "Best" is defined as the sentence with the highest sum of frequencies of its non-stop words.
        Set<String> selectedSentences = new LinkedHashSet<>();
        for (String topWord : topWords) {
            double bestScore = 0;
            String bestSentence = null;
            for (String sentence : sentences) {
                // Clean each sentence for scoring purposes.
                String cleanedSentence = sentence.toLowerCase().replaceAll("[^a-zA-Z\\s]", " ");
                if (!cleanedSentence.contains(topWord)) continue;
                // Calculate sentence score by summing word frequencies.
                double score = 0;
                String[] sentenceWords = cleanedSentence.split("\\s+");
                for (String w : sentenceWords) {
                    w = w.trim();
                    if (w.isEmpty() || stopWords.contains(w)) continue;
                    score += wordFreq.getOrDefault(w, 0);
                }
                if (score > bestScore) {
                    bestScore = score;
                    bestSentence = sentence.trim();
                }
            }
            if (bestSentence != null) {
                selectedSentences.add(bestSentence);
            }
        }
        
        // Step 7: Build the bullet list from the selected sentences.
        StringBuilder bulletSummary = new StringBuilder();
        for (String sentence : selectedSentences) {
            bulletSummary.append("• ").append(sentence).append("\n");
        }
        
        return bulletSummary.toString().trim();
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable(){
            public void run(){
                new TextSummarizerGUI().setVisible(true);
            }
        });
    }
}
