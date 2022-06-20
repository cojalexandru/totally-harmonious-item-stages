package com.decursioteam.thitemstages;

import com.decursioteam.thitemstages.datagen.RestrictionsData;
import com.decursioteam.thitemstages.datagen.utils.FileUtils;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import net.minecraft.util.JSONUtils;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

@Mod.EventBusSubscriber(modid = THItemStages.MOD_ID)
public class Registry {

    public static final Gson GSON = new Gson();
    private static final Multimap<String, String> RESTRICTIONS = HashMultimap.create();
    public static Set<String> getRestrictionsHashSet() {
        return new HashSet<>(RESTRICTIONS.values());
    }

    public static Multimap<String, String> getRestrictions() {
        return RESTRICTIONS;
    }

    public static void registerRestrictionsList() {
        RestrictionsData.getRegistry().getRestrictions().forEach((name, data) -> registerRestrictions(name));
    }

    public static void registerRestrictions(String name) {
        getRestrictions().put(name, name);
    }

    public static void setupRestrictions() {
        THItemStages.LOGGER.info("Loading restrictions...");
        FileUtils.streamFilesAndParse(createCustomPath("restrictions"), Registry::parseRestriction, "Could not stream restrictions!");

        RestrictionsData.getRegistry().regenerateCustomRestrictionData();
    }

    private static void parseRestriction(Reader reader, String name) {
        JsonObject jsonObject = JSONUtils.fromJson(GSON, reader, JsonObject.class);
        name = Codec.STRING.fieldOf("name").orElse(name).codec().fieldOf("Restriction Data").codec().parse(JsonOps.INSTANCE, jsonObject).get().orThrow();
        RestrictionsData.getRegistry().cacheRawRestrictionsData(name.toLowerCase(Locale.ENGLISH).replace(" ", "_"), jsonObject);
    }

    private static Path createCustomPath(String pathName) {
        Path customPath = Paths.get(FMLPaths.CONFIGDIR.get().toAbsolutePath().toString(), THItemStages.MOD_ID, pathName);
        createDirectory(customPath, pathName);
        return customPath;
    }


    private static void createDirectory(Path path, String dirName) {
        try {
            Files.createDirectories(path);
        } catch (FileAlreadyExistsException ignored) { //ignored
        } catch (IOException e) {
            THItemStages.LOGGER.error("failed to create \"{}\" directory", dirName);
        }
    }
}
