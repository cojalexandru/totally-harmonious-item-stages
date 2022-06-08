package com.decursioteam.thitemstages.utils;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.simple.SimpleChannel;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class NetworkUtil {

    private final SimpleChannel channel;
    private int nextPacketId = 0;

    public NetworkUtil(String channelName, String protocolVersion) {
        this(new ResourceLocation(channelName), () -> protocolVersion, protocolVersion::equals, protocolVersion::equals);
    }

    public NetworkUtil(ResourceLocation channelName, String protocolVersion) {
        this(channelName, () -> protocolVersion, protocolVersion::equals, protocolVersion::equals);
    }

    public NetworkUtil(ResourceLocation channelName, Supplier<String> protocolVersion, Predicate<String> clientValidator, Predicate<String> serverValidator) {
        this.channel = NetworkRegistry.newSimpleChannel(channelName, protocolVersion, clientValidator, serverValidator);
    }


    public <T> void registerEnqueuedMessage (Class<T> messageType, BiConsumer<T, PacketBuffer> encoder, Function<PacketBuffer, T> decoder, BiConsumer<T, Supplier<NetworkEvent.Context>> messageConsumer) {
        this.registerMessage(messageType, encoder, decoder, (message, context) -> context.get().enqueueWork( () -> {
            messageConsumer.accept(message, context);
            context.get().setPacketHandled(true);
        }));
    }

    public <T> void registerMessage (Class<T> messageType, BiConsumer<T, PacketBuffer> encoder, Function<PacketBuffer, T> decoder, BiConsumer<T, Supplier<NetworkEvent.Context>> messageConsumer) {
        this.channel.registerMessage(this.nextPacketId, messageType, encoder, decoder, messageConsumer);
        this.nextPacketId++;
    }

    public void sendToServer (Object message) {
        this.channel.sendToServer(message);
    }

    public void send (PacketDistributor.PacketTarget target, Object message) {
        this.channel.send(target, message);
    }

    public void sendToPlayer (ServerPlayerEntity player, Object message) {
        this.send(PacketDistributor.PLAYER.with( () -> player), message);
    }

    public void sendToDimension (RegistryKey<World> dimension, Object message) {
        this.send(PacketDistributor.DIMENSION.with( () -> dimension), message);
    }

    public void sendToNearbyPlayers (double x, double y, double z, double radius, RegistryKey<World> dimension, Object message) {
        this.sendToNearbyPlayers(new PacketDistributor.TargetPoint(x, y, z, radius, dimension), message);
    }

    public void sendToNearbyPlayers (PacketDistributor.TargetPoint point, Object message) {
        this.send(PacketDistributor.NEAR.with( () -> point), message);
    }

    public void sendToAllPlayers (Object message) {
        this.send(PacketDistributor.ALL.noArg(), message);
    }

    public void sendToChunk (Chunk chunk, Object message) {
        this.send(PacketDistributor.TRACKING_CHUNK.with( () -> chunk), message);
    }
}
