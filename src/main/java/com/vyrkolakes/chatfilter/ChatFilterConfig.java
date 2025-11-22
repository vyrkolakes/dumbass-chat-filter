package com.vyrkolakes.chatfilter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ChatFilterConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger("ChatFilterConfig");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("dumbass-filter.json");
    
    // Filter settings
    public boolean enableRankFilter = true;
    public boolean enableWordFilter = true;
    
    // Rank filter settings
    public List<String> filteredRanks = new ArrayList<>(Arrays.asList(
        "Legend", "Prime", "Elite", "Apex", "Prime Ultra", "Elite Ultra"
    ));
    
    // Bracket syntax - what symbols to check for ranks
    public String openingBrackets = "[<{(";
    public String closingBrackets = "]>})";
    
    // Word filter settings
    public List<String> filteredWords = new ArrayList<>(Arrays.asList(
        
    ));
    
    public boolean wordFilterCaseSensitive = false;
    public boolean wordFilterWholeWordOnly = false; // If true, only filter exact word matches
    
    // Save config to file
    public void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            try (Writer writer = new FileWriter(CONFIG_PATH.toFile())) {
                GSON.toJson(this, writer);
                LOGGER.info("Configuration saved to {}", CONFIG_PATH);
            }
        } catch (IOException e) {
            LOGGER.error("Failed to save config", e);
        }
    }
    
    // Load config from file
    public static ChatFilterConfig load() {
        if (Files.exists(CONFIG_PATH)) {
            try (Reader reader = new FileReader(CONFIG_PATH.toFile())) {
                ChatFilterConfig config = GSON.fromJson(reader, ChatFilterConfig.class);
                LOGGER.info("Configuration loaded from {}", CONFIG_PATH);
                return config;
            } catch (IOException e) {
                LOGGER.error("Failed to load config, using defaults", e);
            }
        }
        
        // Create new config with defaults
        ChatFilterConfig config = new ChatFilterConfig();
        config.save();
        return config;
    }
    
    // Get regex pattern for rank filtering based on current settings
    public String getRankPattern() {
        if (filteredRanks.isEmpty()) {
            return null;
        }
        
        // Escape special regex characters in brackets
        String escapedOpen = openingBrackets.replaceAll("([\\[\\]{}()\\\\])", "\\\\$1");
        String escapedClose = closingBrackets.replaceAll("([\\[\\]{}()\\\\])", "\\\\$1");
        
        // Build rank alternation (Legend|Prime|Elite|etc)
        StringBuilder rankAlternation = new StringBuilder();
        for (int i = 0; i < filteredRanks.size(); i++) {
            if (i > 0) rankAlternation.append("|");
            rankAlternation.append(filteredRanks.get(i).replaceAll("([\\[\\]{}()\\\\|])", "\\\\$1"));
        }
        
        return "[" + escapedOpen + "]\\s*(" + rankAlternation + ")\\s*[" + escapedClose + "]";
    }
}