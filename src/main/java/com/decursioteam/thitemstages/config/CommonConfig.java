package com.decursioteam.thitemstages.config;

import com.decursioteam.thitemstages.THItemStages;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import net.minecraftforge.common.ForgeConfigSpec;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class CommonConfig {
    public static final ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec config;
    public static ForgeConfigSpec.BooleanValue debugMode;
    public static ForgeConfigSpec.BooleanValue generate_defaults;
    public static ForgeConfigSpec.ConfigValue<List<String>> stages;


    static {
        builder.push("General Options");
        stages = builder.comment("List all the stages that you want to have.")
                .define("stages", new ArrayList<>());

        debugMode = builder.comment("Set this value to 'true' whenever you're having issues, this will allow the mod to output everything it tries to do in the log.")
                .define("debugMode", false);

        generate_defaults = builder.comment("Set this value to 'true' whenever you want the default restrictions to be regenerated. [true/false]")
                .define("generate_defaults", true);
        builder.pop();
        config = builder.build();
    }

    public static void loadConfig(ForgeConfigSpec config, String path) {
        THItemStages.LOGGER.info("Loading config: " + path);
        final CommentedFileConfig file = CommentedFileConfig.builder(new File(path)).sync().autosave().writingMode(WritingMode.REPLACE).build();
        THItemStages.LOGGER.info("Built config: " + path);
        file.load();
        THItemStages.LOGGER.info("Loaded config: " + path);
        config.setConfig(file);
    }
}
