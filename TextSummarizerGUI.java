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
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;

public class TextSummarizerGUI extends JFrame {
    private JTextArea inputArea, outputArea;
    private JButton summarizeButton;
    // Stores the selected key sentences for later highlighting.
    private Set<String> lastKeySentences = new LinkedHashSet<>();

    public TextSummarizerGUI() {
        setTitle("Text Summarizer");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // Create a main panel with a vertical layout.
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        
        // Create the input area with scroll pane and set a preferred size.
        inputArea = new JTextArea();
        JScrollPane inputScroll = new JScrollPane(inputArea);
        inputScroll.setBorder(BorderFactory.createTitledBorder("Input Text"));
        inputScroll.setPreferredSize(new Dimension(600, 200));
        
        // Create the output area for key sentences with scroll pane and set a preferred size.
        outputArea = new JTextArea();
        outputArea.setEditable(false);
        JScrollPane outputScroll = new JScrollPane(outputArea);
        outputScroll.setBorder(BorderFactory.createTitledBorder("Key Sentences"));
        outputScroll.setPreferredSize(new Dimension(600, 200));
        
        // Create the summarize button and add it to its own panel.
        summarizeButton = new JButton("Summarize");
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(summarizeButton);
        // Limit the button panel size to that of the button.
        Dimension buttonSize = summarizeButton.getPreferredSize();
        buttonPanel.setMaximumSize(new Dimension(buttonSize.width, buttonSize.height));
        
        // Add the components to the main panel.
        mainPanel.add(inputScroll);
        mainPanel.add(buttonPanel);
        mainPanel.add(outputScroll);
        
        // Add the main panel to the frame.
        add(mainPanel);
        
        // When the button is clicked, perform summarization and highlight key sentences.
        summarizeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String text = inputArea.getText();
                String summary = summarizeText(text, 15);
                outputArea.setText(summary);
                highlightKeySentences(lastKeySentences);
            }
        });
        
        pack(); // Adjusts frame size based on preferred sizes.
        setSize(600, 500); // Set overall window size.
    }
    
    /**
     * Implements the summarization algorithm.
     * Steps:
     *  1. Split the text into sentences.
     *  2. Clean the text (convert to lowercase, remove punctuation).
     *  3. Remove uninformative words (stop words) and count word frequencies.
     *  4. Select the top frequent words.
     *  5. For each top word, choose the sentence with the highest cumulative frequency score.
     *  6. Return these sentences as a bullet-point list.
     *
     * Also stores the selected sentences in lastKeySentences for highlighting.
     */
    private String summarizeText(String text, int numTopWords) {
        if (text == null || text.isEmpty()) return "";
        
        // 1. Split text into sentences.
        String[] sentences = text.split("(?<=[.!?])\\s+");
        
        // 2. Clean the text: convert to lowercase and remove punctuation.
        String cleanedText = text.toLowerCase().replaceAll("[^a-zA-Z\\s]", " ");
        String[] words = cleanedText.split("\\s+");
        
        // 3. Define stop words (common words that carry little meaning).
        Set<String> stopWords = new HashSet<>(Arrays.asList(
            "the", "and", "is", "in", "at", "of", "a", "an", "to", "it",
            "for", "on", "with", "as", "by", "this", "that", "from", "i", 
            "you", "he", "she", "they", "we", "but", "or", "if", "so", "are", 
            "was", "were", "be", "been", "being", "am", "do", "does", "did"
        ));
        
        // 4. Count frequencies of non-stop words.
        Map<String, Integer> wordFreq = new HashMap<>();
        for (String word : words) {
            word = word.trim();
            if (word.isEmpty() || stopWords.contains(word)) continue;
            wordFreq.put(word, wordFreq.getOrDefault(word, 0) + 1);
        }
        
        // 5. Get the top frequent words.
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
        
        // 6. For each top word, select the best sentence where it appears.
        Set<String> selectedSentences = new LinkedHashSet<>();
        for (String topWord : topWords) {
            double bestScore = 0;
            String bestSentence = null;
            for (String sentence : sentences) {
                // Clean each sentence.
                String cleanedSentence = sentence.toLowerCase().replaceAll("[^a-zA-Z\\s]", " ");
                if (!cleanedSentence.contains(topWord)) continue;
                // Calculate sentence score by summing frequencies.
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
        
        // Store selected sentences for highlighting.
        lastKeySentences.clear();
        lastKeySentences.addAll(selectedSentences);
        
        // 7. Build the bullet-point list.
        StringBuilder bulletSummary = new StringBuilder();
        for (String sentence : selectedSentences) {
            bulletSummary.append("• ").append(sentence).append("\n");
        }
        
        return bulletSummary.toString().trim();
    }
    
    /**
     * Highlights the key sentences in the input text area using a purple color.
     */
    private void highlightKeySentences(Set<String> keySentences) {
        Highlighter highlighter = inputArea.getHighlighter();
        Highlighter.HighlightPainter painter = new DefaultHighlighter.DefaultHighlightPainter(new Color(128, 0, 128)); // Purple
        // Remove existing highlights.
        highlighter.removeAllHighlights();
        String inputText = inputArea.getText();
        for (String sentence : keySentences) {
            if (sentence.isEmpty()) continue;
            int index = inputText.indexOf(sentence);
            while (index >= 0) {
                try {
                    highlighter.addHighlight(index, index + sentence.length(), painter);
                } catch (BadLocationException ex) {
                    ex.printStackTrace();
                }
                index = inputText.indexOf(sentence, index + sentence.length());
            }
        }
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable(){
            public void run(){
                new TextSummarizerGUI().setVisible(true);
            }
        });
    }
}
