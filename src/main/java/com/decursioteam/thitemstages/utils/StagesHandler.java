package com.decursioteam.thitemstages.utils;

import com.decursioteam.thitemstages.THItemStages;
import com.decursioteam.thitemstages.config.CommonConfig;
import com.decursioteam.thitemstages.datagen.StagesData;
import com.decursioteam.thitemstages.datagen.utils.IStagesData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.util.*;

import static com.decursioteam.thitemstages.THItemStages.LOGGER;

@Mod.EventBusSubscriber(modid = "thitemstages")
public class StagesHandler {

    private static final Map<UUID, IStagesData> GLOBAL_STAGE_DATA = new HashMap<>();
    @OnlyIn(Dist.CLIENT)
    private static IStagesData clientData;

    public static Set<String> getStages(){
        return new HashSet<>(CommonConfig.stages.get());
    }

    @SubscribeEvent
    public static void onPlayerLoad(PlayerEvent.LoadFromFile event) {

        final File playerFile = getPlayerFile(event.getPlayerDirectory(), event.getPlayerUUID());
        final IStagesData playerData = new StagesData();

        if (playerFile.exists()) {

            try {

                final CompoundTag tag = NbtIo.read(playerFile);
                playerData.readFromNBT(tag);
                THItemStages.LOGGER.debug("[T.H.I.S] - Loaded {} stages for {}.", playerData.getStages().size(), event.getEntity().getName().getString());
            }

            catch (final IOException e) {

                THItemStages.LOGGER.error("[T.H.I.S] - Could not read player data for {}.", event.getEntity().getName().getString());
                THItemStages.LOGGER.catching(e);
            }
        }

        GLOBAL_STAGE_DATA.put(event.getEntity().getUUID(), playerData);
    }

    @SubscribeEvent
    public static void onPlayerSave (PlayerEvent.SaveToFile event) {

        final UUID playerUUID = event.getEntity().getUUID();

        if (GLOBAL_STAGE_DATA.containsKey(playerUUID)) {

            final IStagesData playerData = getPlayerData(playerUUID);
            final File playerFile = getPlayerFile(event.getPlayerDirectory(), event.getPlayerUUID());
            final CompoundTag tag = playerData.writeToNBT();

            if (tag != null) {
                try {
                    NbtIo.write(tag, playerFile);
                    LOGGER.info("[T.H.I.S] - Saved {} stages for {}.", playerData.getStages().size(), event.getEntity().getName().getString());
                }

                catch (final IOException e) {
                    if(CommonConfig.debugMode.get()) {
                        LOGGER.error("[T.H.I.S] - Could not write player data for {}.", playerFile.getName());
                        LOGGER.catching(e);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn (PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer) {
            if(CommonConfig.debugMode.get()) LOGGER.info("[T.H.I.S] - Syncing {} player data with the client", event.getEntity().getName().getString());
            StageUtil.syncPlayer((ServerPlayer) event.getEntity());
        }
    }

    @Nullable
    public static IStagesData getPlayerData (UUID uuid) {
        return GLOBAL_STAGE_DATA.get(uuid);
    }

    private static File getPlayerFile (File playerDir, String uuid) {
        final File saveDir = new File(playerDir, "thitemstages");
        if (!saveDir.exists()) {
            saveDir.mkdirs();
        }
        return new File(saveDir, uuid + ".dat");
    }

    @OnlyIn(Dist.CLIENT)
    public static IStagesData getClientData () {
        return clientData;
    }

    @OnlyIn(Dist.CLIENT)
    public static void setClientData (IStagesData stageData) {
        clientData = stageData;
    }
}
