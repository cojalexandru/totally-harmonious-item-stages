package com.decursioteam.thitemstages.codec;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.Encoder;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public class RestrictionCodec {

    public static final RestrictionCodec DEFAULT = new RestrictionCodec("error");

    public static Codec<RestrictionCodec> codec(String name) {
        return RecordCodecBuilder.create(instance -> instance.group(
                MapCodec.of(Encoder.empty(), Decoder.unit(() -> name)).forGetter(RestrictionCodec::getName),
                Codec.STRING.fieldOf("stage").orElse("").forGetter(RestrictionCodec::getStage),
                ResourceLocation.CODEC.listOf().fieldOf("itemList").orElse(ImmutableList.of()).forGetter(RestrictionCodec::getItemList),
                ResourceLocation.CODEC.listOf().fieldOf("blockList").orElse(ImmutableList.of()).forGetter(RestrictionCodec::getBlockList),
                ResourceLocation.CODEC.listOf().fieldOf("tagList").orElse(ImmutableList.of()).forGetter(RestrictionCodec::getTagList),
                ResourceLocation.CODEC.listOf().fieldOf("dimensionList").orElse(ImmutableList.of()).forGetter(RestrictionCodec::getDimensionList),
                Codec.STRING.listOf().fieldOf("modList").orElse(ImmutableList.of()).forGetter(RestrictionCodec::getModList),
                Codec.STRING.listOf().fieldOf("containerList").orElse(ImmutableList.of()).forGetter(RestrictionCodec::getContainerList),
                ResourceLocation.CODEC.listOf().fieldOf("exceptionList").orElse(ImmutableList.of()).forGetter(RestrictionCodec::getExceptionList)
        ).apply(instance, RestrictionCodec::new));
    }



    protected String name;
    protected String stage;
    protected final List<ResourceLocation> blockList;
    protected final List<ResourceLocation> itemList;
    protected final List<ResourceLocation> dimensionList;
    protected final List<ResourceLocation> tagList;
    protected final List<String> modList;
    protected final List<String> containerList;
    protected final List<ResourceLocation> exceptionList;

    private RestrictionCodec(String name, String stage, List<ResourceLocation> itemList, List<ResourceLocation> blockList, List<ResourceLocation> tagList, List<ResourceLocation> dimensionList, List<String> modList, List<String> containerList, List<ResourceLocation> exceptionList){
        this.name = name;
        this.stage = stage;
        this.blockList = blockList;
        this.itemList = itemList;
        this.tagList = tagList;
        this.dimensionList = dimensionList;
        this.modList = modList;
        this.containerList = containerList;
        this.exceptionList = exceptionList;
    }

    private RestrictionCodec(String name) {
        this.name = name;
        this.blockList = new ArrayList<>();
        this.itemList = new ArrayList<>();
        this.dimensionList = new ArrayList<>();
        this.tagList = new ArrayList<>();
        this.modList = new ArrayList<>();
        this.containerList = new ArrayList<>();
        this.exceptionList = new ArrayList<>();
    }

    public List<ResourceLocation> getBlockList() {
        return blockList;
    }

    public List<ResourceLocation> getItemList() {
        return itemList;
    }

    public String getStage() {
        return stage;
    }
    public List<ResourceLocation> getTagList() {
        return tagList;
    }

    public List<String> getModList() {
        return modList;
    }

    public List<ResourceLocation> getDimensionList() {
        return dimensionList;
    }

    public List<String> getContainerList() {
        return containerList;
    }

    public List<ResourceLocation> getExceptionList() {
        return exceptionList;
    }


    public String getName() {
        return name;
    }


    public RestrictionCodec toImmutable() {
        return this;
    }

    public static class Mutable extends RestrictionCodec {

        public Mutable(String name, String stage, List<ResourceLocation> itemList, List<ResourceLocation> blockList, List<ResourceLocation> tagList, List<ResourceLocation> dimensionList, List<String> modList,  List<String> containerList, List<ResourceLocation> exceptionList) {
            super(name, stage, itemList, blockList, tagList, dimensionList, modList, containerList, exceptionList);
        }

        public Mutable(String name) {
            super(name);
        }

        public Mutable setName(String name) {
            this.name = name;
            return this;
        }

        @Override
        public RestrictionCodec toImmutable() {
            return new RestrictionCodec(this.name, this.stage, this.itemList, this.blockList, tagList, this.dimensionList, this.modList, this.containerList, this.exceptionList);
        }
    }
}
