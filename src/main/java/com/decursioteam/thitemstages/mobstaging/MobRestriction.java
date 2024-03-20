package com.decursioteam.thitemstages.mobstaging;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public class MobRestriction {

    private final ResourceLocation entityID;
    private final List<LoadoutRestriction> loadoutList;

    public MobRestriction(ResourceLocation entityID, List<LoadoutRestriction> loadoutList) {
        this.entityID = entityID;
        this.loadoutList = loadoutList;
    }

    public static Codec<MobRestriction> codec() {
        return RecordCodecBuilder.create(instance -> instance.group(
                ResourceLocation.CODEC.fieldOf("entityID").orElse(new ResourceLocation("")).forGetter(MobRestriction::getEntityID),
                LoadoutRestriction.codec().listOf().fieldOf("loadout").orElse(null).forGetter(MobRestriction::getLoadoutList)
        ).apply(instance, MobRestriction::new));
    }

    public ResourceLocation getEntityID() {
        return entityID;
    }

    public List<LoadoutRestriction> getLoadoutList() {
        return loadoutList;
    }
}
