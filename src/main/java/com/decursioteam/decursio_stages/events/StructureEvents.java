package com.decursioteam.decursio_stages.events;

import com.decursioteam.decursio_stages.Registry;
import com.decursioteam.decursio_stages.client.HUDOverlay;
import com.decursioteam.decursio_stages.datagen.RestrictionsData;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.*;

import static com.decursioteam.decursio_stages.utils.StageUtil.hasStage;

public class StructureEvents {
    private final Map<UUID, Boolean> playerInStructure = new HashMap<>();

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && !event.player.level().isClientSide()) {
            ServerPlayer player = (ServerPlayer) event.player;
            UUID playerUUID = player.getUUID();
            Registry.getRestrictions().forEach((s, x) -> {
                String stage = RestrictionsData.getRestrictionData(s).getData().getStage().toLowerCase(Locale.ROOT);
                if(!player.level().isClientSide && hasStage(player, stage)) {
                    StructureManager structureManager = Objects.requireNonNull(event.player.getServer()).getLevel(player.level().dimension()).structureManager();
                    if (!RestrictionsData.getRestrictionData(s).getData().getStructureList().isEmpty()) {
                        RestrictionsData.getRestrictionData(s).getData().getStructureList().forEach(structureRestriction -> {
                            Structure structure = structureManager.registryAccess().registryOrThrow(Registries.STRUCTURE).get(structureRestriction.getStructure());
                            if (structure != null) {
                                boolean isInStructure = structureManager.getStructureAt(event.player.getOnPos(), structure).isValid();

                                if (isInStructure && !Boolean.TRUE.equals(playerInStructure.get(playerUUID))) {
                                    // Player just entered the structure
                                    player.connection.send(new ClientboundSetActionBarTextPacket(Component.translatable("decursio_stages.structure.enter")));
                                    //HUDOverlay.setMessage(Component.translatable("decursio_stages.structure.enter").getString(), 50);
                                    playerInStructure.put(playerUUID, true);
                                } else if (!isInStructure && Boolean.TRUE.equals(playerInStructure.get(playerUUID))) {
                                    // Player just left the structure
                                    player.connection.send(new ClientboundSetActionBarTextPacket(Component.translatable("decursio_stages.structure.leave")));
                                    //HUDOverlay.setMessage(Component.translatable("decursio_stages.structure.leave").getString(), 50);
                                    playerInStructure.put(playerUUID, false);
                                }
                            }
                        });
                    }
                }
            });
        }
    }
}
