package com.decursioteam.thitemstages.datagen;

import com.decursioteam.thitemstages.THItemStages;
import com.decursioteam.thitemstages.codec.RestrictionCodec;
import com.decursioteam.thitemstages.codec.SettingsCodec;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import javax.annotation.Nullable;
import java.util.function.Consumer;

@SuppressWarnings("unused")
public class Restrictions {

    public static final Restrictions DEFAULT = new Restrictions(RestrictionCodec.DEFAULT, SettingsCodec.DEFAULT);


    public static Codec<Restrictions> codec(String name) {
        return RecordCodecBuilder.create(instance -> instance.group(
                RestrictionCodec.codec(name).fieldOf("Restriction Data").orElseGet((Consumer<String>) s -> THItemStages.LOGGER.error("Restriction Data is required!"), null).forGetter(Restrictions::getData),
                SettingsCodec.CODEC.fieldOf("Settings").orElse(SettingsCodec.DEFAULT).forGetter(Restrictions::getSettingsCodec)
        ).apply(instance, Restrictions::new));
    }

    protected RestrictionCodec restrictionCodec;
    protected SettingsCodec settingsCodec;
    protected JsonObject rawData;

    private Restrictions(RestrictionCodec restrictionCodec, SettingsCodec settingsCodec) {
        this.restrictionCodec = restrictionCodec;
        this.settingsCodec = settingsCodec;
        this.rawData = RestrictionsData.getRegistry().getRawORestrictionsData(restrictionCodec.getName());
    }

    private Restrictions(Mutable mutable) {
        this.restrictionCodec = mutable.restrictionCodec.toImmutable();
        this.settingsCodec = mutable.settingsCodec.toImmutable();
        this.rawData = mutable.rawData;
    }

    public RestrictionCodec getData() {
        return restrictionCodec;
    }

    public SettingsCodec getSettingsCodec() {
        return settingsCodec;
    }

    @Nullable
    public JsonObject getRawData() {
        return rawData;
    }

    public Restrictions toImmutable() {
        return this;
    }

    //TODO: javadoc this sub class
    public static class Mutable extends Restrictions {
        public Mutable(RestrictionCodec restrictionCodec, SettingsCodec settingsCodec) {
            super(restrictionCodec, settingsCodec);
        }

        public Mutable() {
            super(RestrictionCodec.DEFAULT, SettingsCodec.DEFAULT);
        }

        public Mutable setData(RestrictionCodec restrictionCodec) {
            this.restrictionCodec = restrictionCodec;
            return this;
        }

        public Mutable setData(SettingsCodec settingsCodec) {
            this.settingsCodec = settingsCodec;
            return this;
        }


        public Mutable setRawData(JsonObject rawData) {
            this.rawData = rawData;
            return this;
        }

        @Override
        public Restrictions toImmutable() {
            return new Restrictions(this);
        }
    }
}
