package com.decursioteam.decursio_stages.events.ore_staging;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.BakedModelWrapper;
import net.minecraftforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DynamicBlockModel extends BakedModelWrapper<BakedModel> {

    private final BakedModel replacementModel;

    public DynamicBlockModel(BakedModel originalModel, BakedModel replacementModel) {
        super(originalModel);
        this.replacementModel = replacementModel;
    }

    @NotNull
    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @NotNull RandomSource rand, @NotNull ModelData extraData, @Nullable net.minecraft.client.renderer.RenderType renderType) {
        if (state != null && state.is(Blocks.IRON_ORE)) {
            // Return the quads for the replacement block model (stone)
            return replacementModel.getQuads(state, side, rand, extraData, renderType);
        }
        // Default behavior for other blocks
        return super.getQuads(state, side, rand, extraData, renderType);
    }

    @Override
    public TextureAtlasSprite getParticleIcon(@NotNull ModelData data) {
        return replacementModel.getParticleIcon(data);
    }
}