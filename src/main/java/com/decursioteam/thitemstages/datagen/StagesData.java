package com.decursioteam.thitemstages.datagen;

import com.decursioteam.thitemstages.datagen.utils.IStagesData;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraftforge.common.util.Constants;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class StagesData implements IStagesData {

    public static final String stagesTag = "Stages";
    private final Set<String> unlockedStages = new HashSet<>();

    @Override
    public ArrayList<String> getStages () {

        return new ArrayList<>(this.unlockedStages);
    }

    @Override
    public boolean hasStage (String stage) {

        return this.unlockedStages.contains(stage.toLowerCase());
    }

    @Override
    public void addStage (String stage) {

        this.unlockedStages.add(stage.toLowerCase());
    }

    @Override
    public void removeStage (String stage) {

        this.unlockedStages.remove(stage.toLowerCase());
    }

    @Override
    public void clear () {

        this.unlockedStages.clear();
    }

    @Override
    public void readFromNBT (CompoundNBT tag) {
        final ListNBT list = tag.getList(stagesTag, Constants.NBT.TAG_STRING);
        for (int tagIndex = 0; tagIndex < list.size(); tagIndex++) {
            this.addStage(list.getString(tagIndex));
        }
    }

    @Override
    public CompoundNBT writeToNBT () {
        final CompoundNBT tag = new CompoundNBT();
        final ListNBT list = new ListNBT();
        for (final String stage : this.unlockedStages) {
            list.add(StringNBT.valueOf(stage));
        }
        tag.put(stagesTag, list);
        return tag;
    }

    @Override
    public String toString () {
        return "Player Stages [unlockedStages=" + this.unlockedStages + "]";
    }
}
