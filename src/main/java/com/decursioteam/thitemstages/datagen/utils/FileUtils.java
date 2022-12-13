package com.decursioteam.thitemstages.datagen.utils;

import com.decursioteam.thitemstages.Registry;
import com.decursioteam.thitemstages.THItemStages;
import com.decursioteam.thitemstages.datagen.RestrictionsData;
import com.decursioteam.thitemstages.restrictions.ItemRestriction;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.*;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static com.decursioteam.thitemstages.utils.ResourceUtil.getRegistryName;
import static com.decursioteam.thitemstages.utils.ResourceUtil.isItemExcluded;

public class FileUtils
{

    public static final String JSON = ".json";
    public static final String ZIP = ".zip";
    public static final Path MOD_ROOT = ModList.get().getModFileById(THItemStages.MOD_ID).getFile().getFilePath();

    public static void streamFilesAndParse(Path directoryPath, BiConsumer<Reader, String> instructions, String errorMessage) {
        try (Stream<Path> zipStream = Files.walk(directoryPath);
             Stream<Path> jsonStream = Files.walk(directoryPath)) {
            zipStream.filter(f -> f.getFileName().toString().endsWith(ZIP)).forEach(path -> addZippedFile(path, instructions));
            jsonStream.filter(f -> f.getFileName().toString().endsWith(JSON)).forEach(path -> addFile(path, instructions));
        } catch (IOException e) {
            THItemStages.LOGGER.error(errorMessage, e);
        }
    }

    public static void setupDefaultFiles(String dataPath, Path targetPath) {
        if (Files.isRegularFile(MOD_ROOT)) {
            try (FileSystem fileSystem = FileSystems.newFileSystem(MOD_ROOT, ClassLoader.getSystemClassLoader())) {
                Path path = fileSystem.getPath(dataPath);
                if (Files.exists(path)) {
                    copyFiles(path, targetPath);
                }
            } catch (IOException e) {
                THItemStages.LOGGER.error("Could not load source {}!!", MOD_ROOT);
                e.printStackTrace();
            }
        } else if (Files.isDirectory(MOD_ROOT)) {
            copyFiles(Paths.get(MOD_ROOT.toString(), dataPath), targetPath);
        }
    }

    private static void addFile(Path path, BiConsumer<Reader, String> instructions) {
    File f = path.toFile();
    try {
        parseType(f, instructions);
    } catch (IOException e) {
        THItemStages.LOGGER.warn("File not found: {}", path);
    }
    }

    private static void addZippedFile(Path file, BiConsumer<Reader, String> instructions) {
        try (ZipFile zf = new ZipFile(file.toString())) {
            zf.stream()
                    .filter(zipEntry -> zipEntry.getName().endsWith(JSON))
                    .forEach(zipEntry -> {
                        try {
                            parseType(zf, zipEntry, instructions);
                        } catch (IOException e) {
                            THItemStages.LOGGER.error("Could not parse zip entry: {}", zipEntry.getName());
                        }
                    });
        } catch (IOException e) {
            THItemStages.LOGGER.warn("Could not read Zip File: {}", file.getFileName());
        }
    }

    private static void parseType(File file, BiConsumer<Reader, String> consumer) throws IOException {
        String name = file.getName();
        name = name.substring(0, name.indexOf('.'));

        Reader r = Files.newBufferedReader(file.toPath());

        consumer.accept(r, name);
    }

    private static void parseType(ZipFile zf, ZipEntry zipEntry, BiConsumer<Reader, String> consumer) throws IOException {
        String name = zipEntry.getName();
        name = name.substring(name.lastIndexOf("/") + 1, name.indexOf('.'));

        InputStream input = zf.getInputStream(zipEntry);
        BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8));

        consumer.accept(reader, name);
    }

    private static void copyFiles(Path source, Path targetPath) {
        try (Stream<Path> sourceStream = Files.walk(source)) {
            sourceStream.filter(f -> f.getFileName().toString().endsWith(JSON))
                    .forEach(path -> {
                        try {
                            Files.copy(path, Paths.get(targetPath.toString(), path.getFileName().toString()), StandardCopyOption.REPLACE_EXISTING);
                        } catch (IOException e) {
                            THItemStages.LOGGER.error("Could not copy file: {}, Target: {}", path, targetPath);
                        }
                    });
        } catch (IOException e) {
            THItemStages.LOGGER.error("Could not stream source files: {}", source);
        }
    }

    public static boolean restrictionExists(String restriction, String stage, String advancedTooltips, String itemTitle, int pickupDelay, boolean hideInJEI, boolean canPickup, boolean containerListWhitelist, boolean checkPlayerInventory, boolean checkPlayerEquipment, boolean usableItems, boolean usableBlocks, boolean destroyableBlocks)
    {
        return RestrictionsData.getRestrictionData(restriction).getData().getStage().equals(stage)
                && RestrictionsData.getRestrictionData(restriction).getSettingsCodec().getAdvancedTooltips().equals(advancedTooltips)
                && RestrictionsData.getRestrictionData(restriction).getSettingsCodec().getItemTitle().equals(itemTitle)
                && RestrictionsData.getRestrictionData(restriction).getSettingsCodec().getPickupDelay() == pickupDelay
                && RestrictionsData.getRestrictionData(restriction).getSettingsCodec().getHideInJEI() == hideInJEI
                && RestrictionsData.getRestrictionData(restriction).getSettingsCodec().getCanPickup() == canPickup
                && RestrictionsData.getRestrictionData(restriction).getSettingsCodec().getContainerListWhitelist() == containerListWhitelist
                && RestrictionsData.getRestrictionData(restriction).getSettingsCodec().getCheckPlayerInventory() == checkPlayerInventory
                && RestrictionsData.getRestrictionData(restriction).getSettingsCodec().getCheckPlayerEquipment() == checkPlayerEquipment
                && RestrictionsData.getRestrictionData(restriction).getSettingsCodec().getUsableItems() == usableItems
                && RestrictionsData.getRestrictionData(restriction).getSettingsCodec().getUsableBlocks() == usableBlocks
                && RestrictionsData.getRestrictionData(restriction).getSettingsCodec().getDestroyableBlocks() == destroyableBlocks;
    }
    public static void restrictItem(String stage, String advancedTooltips, String itemTitle, int pickupDelay, boolean hideInJEI, boolean canPickup, boolean containerListWhitelist, boolean checkPlayerInventory, boolean checkPlayerEquipment, boolean usableItems, boolean usableBlocks, boolean destroyableBlocks, ItemStack itemStack)
    {
        JsonObject itemElement = new JsonObject();
        itemElement.addProperty("item", getRegistryName(itemStack.getItem()).toString());
        if(itemStack.getTag() != null) {
            try {
                itemElement.add("nbt", GsonHelper.parse(NbtUtils.toPrettyComponent(itemStack.getTag()).getString(), true));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        RestrictionsData.getRegistry().getRawRestrictions().forEach((restriction, jsonFile) -> {
            if(restrictionExists(restriction, stage, advancedTooltips, itemTitle, pickupDelay, hideInJEI, canPickup, containerListWhitelist, checkPlayerInventory, checkPlayerEquipment, usableItems, usableBlocks, destroyableBlocks)) {


                if(!RestrictionsData.getRestrictionData(restriction).getData().getItemList().contains(new ItemRestriction(getRegistryName(itemStack.getItem()), Objects.requireNonNull(Objects.requireNonNull(Optional.ofNullable(itemStack.getTag())))))) {
                    jsonFile.get("Restriction Data").getAsJsonObject().get("itemList").getAsJsonArray().add(itemElement);
                }

                if(!isItemExcluded(restriction, itemStack)) {
                    for (int i = 0; i <= jsonFile.get("Restriction Data").getAsJsonObject().get("exceptionList").getAsJsonArray().size(); i++) {
                        if(jsonFile.get("Restriction Data").getAsJsonObject().get("exceptionList").getAsJsonArray().get(i).equals(itemElement)) {
                            jsonFile.get("Restriction Data").getAsJsonObject().get("exceptionList").getAsJsonArray().remove(i);
                        }
                    }
                }

                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                String jsonOutput = gson.toJson(jsonFile);
                try (PrintWriter out = new PrintWriter(new FileWriter(createCustomPath("restrictions")+ "/" + restriction + ".json"))) {
                    out.write(jsonOutput);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if(!RestrictionsData.getRestrictionData(restriction).getData().getStage().equals(stage)) {
                if(RestrictionsData.getRestrictionData(restriction).getData().getItemList().contains((new ItemRestriction(getRegistryName(itemStack.getItem()), Objects.requireNonNull(Objects.requireNonNull(Optional.ofNullable(itemStack.getTag()))))))) {
                    for (int i = 0; i <= jsonFile.get("Restriction Data").getAsJsonObject().get("itemList").getAsJsonArray().size(); i++) {
                        if(jsonFile.get("Restriction Data").getAsJsonObject().get("itemList").getAsJsonArray().get(i).equals(itemElement)) {
                            jsonFile.get("Restriction Data").getAsJsonObject().get("itemList").getAsJsonArray().remove(i);
                        }
                    }
                }

                if(isItemExcluded(restriction, itemStack)) {
                    jsonFile.get("Restriction Data").getAsJsonObject().get("exceptionList").getAsJsonArray().add(itemElement);
                }
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                String jsonOutput = gson.toJson(jsonFile);
                try (PrintWriter out = new PrintWriter(new FileWriter(createCustomPath("restrictions")+ "/" + restriction + ".json"))) {
                    out.write(jsonOutput);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            RestrictionsData.getRegistry().cacheRawRestrictionsData(restriction, jsonFile);
            RestrictionsData.getRegistry().regenerateCustomRestrictionData();
        });
    }
    public static void addRestriction(String stage, String advancedTooltips, String itemTitle, int pickupDelay, boolean hideInJEI, boolean canPickup, boolean containerListWhitelist, boolean checkPlayerInventory, boolean checkPlayerEquipment, boolean usableItems, boolean usableBlocks, boolean destroyableBlocks)
    {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        JsonObject jsonFile = new JsonObject();
        JsonObject restrictionData = new JsonObject();
        JsonObject settings = new JsonObject();
        JsonArray itemList = new JsonArray();

        jsonFile.add("Restriction Data", restrictionData);
        jsonFile.add("Settings", settings);

        restrictionData.addProperty("stage", stage);
        restrictionData.add("itemList", itemList);

        settings.addProperty("advancedTooltips", advancedTooltips);
        settings.addProperty("itemTitle", itemTitle);
        settings.addProperty("pickupDelay", pickupDelay);
        settings.addProperty("hideInJEI", hideInJEI);
        settings.addProperty("canPickup", canPickup);
        settings.addProperty("containerListWhitelist", containerListWhitelist);
        settings.addProperty("checkPlayerInventory", checkPlayerInventory);
        settings.addProperty("checkPlayerEquipment", checkPlayerEquipment);
        settings.addProperty("usableItems", usableItems);
        settings.addProperty("destroyableBlocks", destroyableBlocks);
        settings.addProperty("usableBlocks", usableBlocks);


        String jsonOutput = gson.toJson(jsonFile);
        try (PrintWriter out = new PrintWriter(new FileWriter(createCustomPath("restrictions")+ "/" + stage + "_" + itemTitle + ".json"))) {
            out.write(jsonOutput);
            RestrictionsData.getRegistry().cacheRawRestrictionsData(stage + "_" + itemTitle, jsonFile);
            RestrictionsData.getRegistry().regenerateCustomRestrictionData();
            Registry.registerRestrictionsList();
        } catch (Exception e) {
            e.printStackTrace();
        }
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
