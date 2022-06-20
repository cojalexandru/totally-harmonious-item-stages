package com.decursioteam.thitemstages.datagen;

import com.decursioteam.thitemstages.THItemStages;
import com.decursioteam.thitemstages.datagen.utils.IRestrictionsData;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;

import java.util.*;

public class RestrictionsData implements IRestrictionsData {

    private static final RestrictionsData INSTANCE = new RestrictionsData();
    private static final Map<String, JsonObject> RAW_DATA = new LinkedHashMap<>();
    private static final Map<String, Restrictions> CUSTOM_DATA = new LinkedHashMap<>();


    public static RestrictionsData getRegistry() {
        return INSTANCE;
    }

    public static Restrictions getRestrictionData(String name) {
        return CUSTOM_DATA.getOrDefault(name, Restrictions.DEFAULT);
    }

    public void regenerateCustomRestrictionData() {
        RAW_DATA.forEach((s, jsonObject) -> CUSTOM_DATA.compute(s, (s1, restrictionCodec) ->
                Restrictions.codec(s).parse(JsonOps.INSTANCE, jsonObject)
                        .getOrThrow(false, s2 -> THItemStages.LOGGER.error("[T.H.I.S] - Couldn't create data for {} stage!", s))));
    }

    public JsonObject getRawORestrictionsData(String name) {
        return RAW_DATA.get(name);
    }

    @Override
    public Map<String, Restrictions> getRestrictions() {
        return Collections.unmodifiableMap(CUSTOM_DATA);
    }

    @Override
    public Set<Restrictions> getSetOfRestrictions() {
        return Collections.unmodifiableSet(new HashSet<>(CUSTOM_DATA.values()));
    }

    @Override
    public Map<String, JsonObject> getRawRestrictions() {
        return Collections.unmodifiableMap(RAW_DATA);
    }

    public void cacheRawRestrictionsData(String name, JsonObject restrictionCodec) {
        try {
            if(RAW_DATA.containsKey(name.toLowerCase(Locale.ENGLISH))) {
                RAW_DATA.computeIfPresent(name.toLowerCase(Locale.ENGLISH).replace(" ", "_"), (s, oldVal) -> Objects.requireNonNull(restrictionCodec));
            } else RAW_DATA.computeIfAbsent(name.toLowerCase(Locale.ENGLISH).replace(" ", "_"), s -> Objects.requireNonNull(restrictionCodec));
        } catch (IllegalArgumentException e){
            THItemStages.LOGGER.error("[T.H.I.S] - There is a error with the " + name + " stage file");
            THItemStages.LOGGER.error(e);
        }
    }
}
