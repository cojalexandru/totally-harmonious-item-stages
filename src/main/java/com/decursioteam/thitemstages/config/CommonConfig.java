package com.decursioteam.thitemstages.config;

import net.minecraftforge.common.ForgeConfigSpec;

import java.util.ArrayList;
import java.util.List;

public class CommonConfig {
    public static final ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec config;
    public static ForgeConfigSpec.BooleanValue debugMode;
    public static ForgeConfigSpec.ConfigValue<List<String>> stages;


    static {
        builder.push("General Options");
        stages = builder.comment("List all the stages that you want to have.")
                .define("stages", new ArrayList<>());

        debugMode = builder.comment("Set this value to 'true' whenever you're having issues, this will allow the mod to output everything it tries to do in the log.")
                .define("debugMode", false);

        builder.pop();
        config = builder.build();
    }
}
