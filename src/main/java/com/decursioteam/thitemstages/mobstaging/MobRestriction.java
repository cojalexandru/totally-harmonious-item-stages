package com.decursioteam.thitemstages.mobstaging;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Optional;

public class MobRestriction {

    private final Optional<ResourceLocation> entityID;
    private final Optional<String> mobCategory;
    private final Optional<Integer> minLight;
    private final Optional<Integer> maxLight;
    private final Optional<List<ResourceLocation>> biomes;

    private final Optional<Integer> health;

    private final Optional<List<EffectsCodec>> effects;
    private final List<LoadoutRestriction> loadoutList;

    public MobRestriction(Optional<ResourceLocation> entityID, Optional<String> mobCategory, Optional<Integer> minLight, Optional<Integer> maxLight, Optional<List<ResourceLocation>> biomes, Optional<Integer> health, Optional<List<EffectsCodec>> effects, List<LoadoutRestriction> loadoutList) {
        this.entityID = entityID;
        this.mobCategory = mobCategory;
        this.minLight = minLight;
        this.maxLight = maxLight;
        this.biomes = biomes;
        this.health = health;
        this.effects = effects;
        this.loadoutList = loadoutList;
    }

    public static Codec<MobRestriction> codec() {
        return RecordCodecBuilder.create(instance -> instance.group(
                ResourceLocation.CODEC.optionalFieldOf("entityID").forGetter(MobRestriction::getEntityID),
                Codec.STRING.optionalFieldOf("mobCategory").forGetter(MobRestriction::getMobCategory),
                Codec.INT.optionalFieldOf("minLight").forGetter(MobRestriction::getMinLight),
                Codec.INT.optionalFieldOf("maxLight").forGetter(MobRestriction::getMaxLight),
                ResourceLocation.CODEC.listOf().optionalFieldOf("biomes").forGetter(MobRestriction::getBiomes),
                Codec.INT.optionalFieldOf("health").forGetter(MobRestriction::getHealth),
                EffectsCodec.codec().listOf().optionalFieldOf("effects").forGetter(MobRestriction::getEffects),
                LoadoutRestriction.codec().listOf().fieldOf("loadout").orElse(null).forGetter(MobRestriction::getLoadoutList)
        ).apply(instance, (entityID1, mobCategory1, minlight1, maxlight1, biomes1, health1, effects1, loadoutList1) -> new MobRestriction(entityID1, mobCategory1, minlight1, maxlight1, biomes1, health1, effects1, loadoutList1)));
    }

    public Optional<Integer> getHealth() {
        return health;
    }

    public Optional<Integer> getMaxLight() {
        return maxLight;
    }

    public Optional<Integer> getMinLight() {
        return minLight;
    }

    public Optional<List<EffectsCodec>> getEffects() {
        return effects;
    }

    public Optional<List<ResourceLocation>> getBiomes() {
        return biomes;
    }

    public Optional<ResourceLocation> getEntityID() {
        return entityID;
    }

    public Optional<String> getMobCategory() {
        return mobCategory;
    }

    public List<LoadoutRestriction> getLoadoutList() {
        return loadoutList;
    }
}
