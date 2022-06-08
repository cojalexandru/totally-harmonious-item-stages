package com.decursioteam.thitemstages.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class CommonConfig {
    public static final ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec config;
    public static ForgeConfigSpec.BooleanValue debugMode;


    static {
        builder.push("General Options");
        debugMode = builder.comment("Set this value to 'true' whenever you're having issues, this will allow the mod to output everything it tries to do in the log.")
                .define("debugMode", false);
        builder.pop();
        config = builder.build();
    }
}
