package com.decursioteam.thitemstages.datagen.utils;

import com.decursioteam.thitemstages.datagen.Restrictions;
import com.google.gson.JsonObject;

import java.util.Map;
import java.util.Set;

public interface IRestrictionsData {

    JsonObject getRawORestrictionsData(String name);

    Map<String, Restrictions> getRestrictions();

    Set<Restrictions> getSetOfRestrictions();

    Map<String, JsonObject> getRawRestrictions();
}
