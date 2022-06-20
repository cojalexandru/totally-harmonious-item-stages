package com.decursioteam.thitemstages.integrations;

import com.decursioteam.thitemstages.config.CommonConfig;
import com.decursioteam.thitemstages.datagen.RestrictionsData;
import com.flemmli97.improvedmobs.capability.PlayerDifficultyData;
import com.flemmli97.improvedmobs.capability.TileCapProvider;
import com.flemmli97.improvedmobs.difficulty.DifficultyData;
import com.flemmli97.improvedmobs.network.PacketHandler;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Collections;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;

import static com.decursioteam.thitemstages.utils.StageUtil.*;

public class ImprovedMobs {

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void stagePlayerDifficulty(TickEvent.PlayerTickEvent event) {
        if(CommonConfig.improvedMobs.get()) {
            if (event.player instanceof ServerPlayerEntity && !event.player.level.isClientSide() && event.phase == TickEvent.Phase.START && !(event.player instanceof FakePlayer)) {
                float playerDifficulty = event.player.getCapability(TileCapProvider.PlayerCap).map(PlayerDifficultyData::getDifficultyLevel).orElse(0F);
                try {
                    Set<Float> difficulties = new HashSet<>();
                    RestrictionsData.getRegistry().getRestrictions().forEach((restriction, s) -> {
                        if (hasStage(event.player, RestrictionsData.getRestrictionData(restriction).getData().getStage())) {
                            difficulties.add(RestrictionsData.getRestrictionData(restriction).getSettingsCodec().getImprovedMobsDifficulty());
                        }
                    });
                    if (!hasAnyOf(event.player, getStages())) {
                        TileCapProvider.getPlayerDifficultyData((ServerPlayerEntity) event.player).ifPresent(data -> {
                            data.setDifficultyLevel(0f);
                            PacketHandler.sendDifficultyToClient(DifficultyData.get(event.player.level), (ServerPlayerEntity) event.player);
                        });
                    }
                    if (playerDifficulty != Collections.max(difficulties)) {
                        TileCapProvider.getPlayerDifficultyData((ServerPlayerEntity) event.player).ifPresent(data -> {
                            data.setDifficultyLevel(Collections.max(difficulties));
                            PacketHandler.sendDifficultyToClient(DifficultyData.get(event.player.level), (ServerPlayerEntity) event.player);
                        });
                    }
                } catch (NoSuchElementException e) {
                    //
                }
            }
        }
    }
}
