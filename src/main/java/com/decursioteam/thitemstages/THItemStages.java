package com.decursioteam.thitemstages;

import com.decursioteam.thitemstages.commands.THISCommands;
import com.decursioteam.thitemstages.config.CommonConfig;
import com.decursioteam.thitemstages.events.Events;
import com.decursioteam.thitemstages.network.ClientPacketHandler;
import com.decursioteam.thitemstages.network.ServerPacketHandler;
import com.decursioteam.thitemstages.network.messages.SyncStagesMessage;
import com.decursioteam.thitemstages.utils.NetworkUtil;
import com.decursioteam.thitemstages.utils.StagesReload;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static com.decursioteam.thitemstages.THItemStages.MOD_ID;

@Mod(MOD_ID)
public class THItemStages {

    public static final Logger LOGGER = LogManager.getLogger();
    public static final String MOD_ID = "thitemstages";
    public static final NetworkUtil NETWORK = new NetworkUtil("thitemstages:main", "7.0.x");

    public THItemStages() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, CommonConfig.config, "thitemstages/thitemstages-common.toml");
        NETWORK.registerEnqueuedMessage(SyncStagesMessage.class, ServerPacketHandler::encodeStageMessage, t -> ClientPacketHandler.decodeStageMessage(t), (t, u) -> ClientPacketHandler.processSyncStagesMessage(t, u));

        THISCommands.init();

        Registry.setupRestrictions();
        Registry.registerRestrictionsList();

        MinecraftForge.EVENT_BUS.addListener(this::registerReloadListener);
        MinecraftForge.EVENT_BUS.register(new Events());

        CommonConfig.loadConfig(CommonConfig.config, FMLPaths.CONFIGDIR.get().resolve("thitemstages/thitemstages-common.toml").toString());
        if (CommonConfig.generate_defaults.get()) {
            Registry.setupDefaultRestrictions();
            CommonConfig.generate_defaults.set(false);
            CommonConfig.generate_defaults.save();
        }
    }

    private void registerReloadListener(AddReloadListenerEvent event){
        event.addListener(new StagesReload());
    }
}