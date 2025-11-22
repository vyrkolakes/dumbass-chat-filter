package com.vyrkolakes.chatfilter;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DumbassChatFilter implements ClientModInitializer {
    public static final String MOD_ID = "dumbass-chat-filter";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    
    private static ChatFilterConfig config;
    private static Pattern rankPattern;

    @Override
    public void onInitializeClient() {
        LOGGER.info("Dumbass Filter for Chat initialized!");
        
        // Load configuration
        config = ChatFilterConfig.load();
        updateRankPattern();
        
        // Register chat message event handler
        ClientReceiveMessageEvents.ALLOW_CHAT.register((message, signedMessage, sender, params, receptionTimestamp) -> {
            return !shouldFilterMessage(message);
        });
        
        // Also handle game messages
        ClientReceiveMessageEvents.ALLOW_GAME.register((message, overlay) -> {
            if (overlay) return true; // Don't filter overlay messages
            return !shouldFilterMessage(message);
        });
    }
    
    /**
     * Get the current configuration
     */
    public static ChatFilterConfig getConfig() {
        return config;
    }
    
    /**
     * Reload the configuration and update patterns
     */
    public static void reloadConfig() {
        config = ChatFilterConfig.load();
        updateRankPattern();
        LOGGER.info("Configuration reloaded!");
    }
    
    /**
     * Update the rank pattern based on current config
     */
    private static void updateRankPattern() {
        String pattern = config.getRankPattern();
        if (pattern != null) {
            try {
                rankPattern = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
            } catch (Exception e) {
                LOGGER.error("Failed to compile rank pattern: {}", pattern, e);
                rankPattern = null;
            }
        } else {
            rankPattern = null;
        }
    }
    
    /**
     * Determines if a message should be filtered
     */
    private boolean shouldFilterMessage(Text message) {
        String messageText = message.getString();
        
        // Check rank filter
        if (config.enableRankFilter && rankPattern != null) {
            if (containsFilteredRank(messageText)) {
                return true;
            }
        }
        
        // Check word filter
        if (config.enableWordFilter) {
            if (containsFilteredWord(messageText)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Check if message contains a filtered rank
     */
    private boolean containsFilteredRank(String messageText) {
        if (rankPattern == null) return false;
        
        Matcher matcher = rankPattern.matcher(messageText);
        return matcher.find();
    }
    
    /**
     * Check if message contains a filtered word
     */
    private boolean containsFilteredWord(String messageText) {
        if (config.filteredWords.isEmpty()) return false;
        
        String textToCheck = config.wordFilterCaseSensitive ? messageText : messageText.toLowerCase();
        
        for (String word : config.filteredWords) {
            String wordToCheck = config.wordFilterCaseSensitive ? word : word.toLowerCase();
            
            if (config.wordFilterWholeWordOnly) {
                // Use word boundaries for exact word matching
                String regex = "\\b" + Pattern.quote(wordToCheck) + "\\b";
                Pattern pattern = Pattern.compile(regex, config.wordFilterCaseSensitive ? 0 : Pattern.CASE_INSENSITIVE);
                if (pattern.matcher(messageText).find()) {
                    return true;
                }
            } else {
                // Simple contains check
                if (textToCheck.contains(wordToCheck)) {
                    return true;
                }
            }
        }
        
        return false;
    }
}