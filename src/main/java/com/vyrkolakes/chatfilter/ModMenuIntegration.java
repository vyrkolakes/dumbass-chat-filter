package com.vyrkolakes.chatfilter;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.ArrayList;

public class ModMenuIntegration implements ModMenuApi {
    
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return this::createConfigScreen;
    }
    
    private Screen createConfigScreen(Screen parent) {
        ChatFilterConfig config = DumbassChatFilter.getConfig();
        
        ConfigBuilder builder = ConfigBuilder.create()
            .setParentScreen(parent)
            .setTitle(Text.literal("Dumbass Filter Configuration"))
            .setSavingRunnable(() -> {
                config.save();
                DumbassChatFilter.reloadConfig();
            });
        
        ConfigEntryBuilder entryBuilder = builder.entryBuilder();
        
        // ===== GENERAL SETTINGS =====
        ConfigCategory general = builder.getOrCreateCategory(Text.literal("General"));
        
        general.addEntry(entryBuilder.startBooleanToggle(
            Text.literal("Enable Rank Filter"),
            config.enableRankFilter)
            .setDefaultValue(true)
            .setTooltip(Text.literal("Filter messages from players with specific ranks/tags"))
            .setSaveConsumer(val -> config.enableRankFilter = val)
            .build()
        );
        
        general.addEntry(entryBuilder.startBooleanToggle(
            Text.literal("Enable Word Filter"),
            config.enableWordFilter)
            .setDefaultValue(true)
            .setTooltip(Text.literal("Filter messages containing specific words"))
            .setSaveConsumer(val -> config.enableWordFilter = val)
            .build()
        );
        
        // ===== RANK FILTER SETTINGS =====
        ConfigCategory rankFilter = builder.getOrCreateCategory(Text.literal("Rank Filter"));
        
        rankFilter.addEntry(entryBuilder.startStrList(
            Text.literal("Filtered Ranks"),
            config.filteredRanks != null ? config.filteredRanks : new ArrayList<>())
            .setDefaultValue(new ArrayList<>())
            .setTooltip(Text.literal("List of ranks to filter (e.g., Legend, Prime, Elite)"))
            .setSaveConsumer(val -> config.filteredRanks = val)
            .build()
        );
        
        rankFilter.addEntry(entryBuilder.startStrField(
            Text.literal("Opening Brackets"),
            config.openingBrackets)
            .setDefaultValue("[<{(")
            .setTooltip(Text.literal("Characters that can open a rank tag (e.g., [<{( )"))
            .setSaveConsumer(val -> config.openingBrackets = val)
            .build()
        );
        
        rankFilter.addEntry(entryBuilder.startStrField(
            Text.literal("Closing Brackets"),
            config.closingBrackets)
            .setDefaultValue("]>})")
            .setTooltip(Text.literal("Characters that can close a rank tag (e.g., ]>}) )"))
            .setSaveConsumer(val -> config.closingBrackets = val)
            .build()
        );
        
        // ===== WORD FILTER SETTINGS =====
        ConfigCategory wordFilter = builder.getOrCreateCategory(Text.literal("Word Filter"));
        
        wordFilter.addEntry(entryBuilder.startStrList(
            Text.literal("Filtered Words"),
            config.filteredWords != null ? config.filteredWords : new ArrayList<>())
            .setDefaultValue(new ArrayList<>())
            .setTooltip(Text.literal("List of words to filter from chat"))
            .setSaveConsumer(val -> config.filteredWords = val)
            .build()
        );
        
        wordFilter.addEntry(entryBuilder.startBooleanToggle(
            Text.literal("Case Sensitive"),
            config.wordFilterCaseSensitive)
            .setDefaultValue(false)
            .setTooltip(Text.literal("If enabled, 'Word' and 'word' are treated differently"))
            .setSaveConsumer(val -> config.wordFilterCaseSensitive = val)
            .build()
        );
        
        wordFilter.addEntry(entryBuilder.startBooleanToggle(
            Text.literal("Whole Word Only"),
            config.wordFilterWholeWordOnly)
            .setDefaultValue(false)
            .setTooltip(Text.literal("If enabled, only filters exact word matches (e.g., 'bad' won't match 'badass')"))
            .setSaveConsumer(val -> config.wordFilterWholeWordOnly = val)
            .build()
        );
        
        return builder.build();
    }
}