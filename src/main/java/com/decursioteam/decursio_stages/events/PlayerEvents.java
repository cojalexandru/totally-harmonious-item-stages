package com.decursioteam.decursio_stages.events;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class PlayerEvents {

    private static boolean playerReachedGamePoint = false;

    public static void setPlayerReachedGamePoint(boolean reached) {
        playerReachedGamePoint = reached;
    }

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        Minecraft mc = Minecraft.getInstance();
        Level level = mc.level;

        // Ensure valid level and player
        if (level == null || mc.player == null) return;

        PoseStack poseStack = event.getPoseStack();
        BlockRenderDispatcher blockRenderer = mc.getBlockRenderer();

        // Define the visible area (extend the view frustum by 50 blocks around the player)
        AABB frustum = mc.player.getBoundingBox().inflate(50);

        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource(); // Use buffer source for rendering

        // Loop through the blocks in the visible area
        for (int x = (int) frustum.minX; x <= frustum.maxX; x++) {
            for (int y = (int) frustum.minY; y <= frustum.maxY; y++) {
                for (int z = (int) frustum.minZ; z <= frustum.maxZ; z++) {
                    mutablePos.set(x, y, z);
                    BlockState blockState = level.getBlockState(mutablePos);

                    // If the block is iron ore, render it as stone
                    if (blockState.getBlock() == Blocks.IRON_ORE) {
                        BlockState stoneState = Blocks.STONE.defaultBlockState();

                        poseStack.pushPose();  // Push pose to isolate transformations
                        poseStack.translate(mutablePos.getX() - event.getCamera().getPosition().x,
                                mutablePos.getY() - event.getCamera().getPosition().y,
                                mutablePos.getZ() - event.getCamera().getPosition().z);

                        blockRenderer.renderBatched(stoneState, mutablePos, level, poseStack,
                                bufferSource.getBuffer(RenderType.solid()), false, mc.level.random);

                        poseStack.popPose();  // Pop the pose to restore previous state
                    }
                }
            }
        }

        bufferSource.endBatch();  // Ensure all rendering operations finish
    }
}
