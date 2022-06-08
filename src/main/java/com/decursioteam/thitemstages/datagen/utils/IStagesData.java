package com.decursioteam.thitemstages.datagen.utils;

import net.minecraft.nbt.CompoundNBT;

import javax.annotation.Nonnull;
import java.util.ArrayList;

public interface IStagesData {

    ArrayList<String> getStages();

    boolean hasStage (@Nonnull String stage);

    void addStage (@Nonnull String stage);

    void removeStage (@Nonnull String stage);

    void clear ();

    void readFromNBT (CompoundNBT tag);

    CompoundNBT writeToNBT ();
}
