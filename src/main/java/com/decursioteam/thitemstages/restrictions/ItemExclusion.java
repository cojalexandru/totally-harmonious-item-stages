package com.decursioteam.thitemstages.restrictions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.Optional;

public class ItemExclusion {

    private ResourceLocation item;
    private CompoundTag compoundTag;
    private String mod;
    private ResourceLocation tag;

    public ItemExclusion(Optional<ResourceLocation> item, Optional<CompoundTag> compoundTag, Optional<ResourceLocation> tag, Optional<String> mod){
        try {
            item.ifPresent(x -> this.item = item.get());
            tag.ifPresent(x -> this.tag = tag.get());
            mod.ifPresent(x -> this.mod = mod.get());
            compoundTag.ifPresent(x -> this.compoundTag = compoundTag.get());
        }
        catch (NullPointerException e)
        {
            //
        }

    }

    public static Codec<ItemExclusion> codec() {
        return RecordCodecBuilder.create(instance -> instance.group(
                ResourceLocation.CODEC.optionalFieldOf("item").orElse(null).forGetter(itemExclusion -> Optional.ofNullable(itemExclusion.item)),
                CompoundTag.CODEC.optionalFieldOf("nbt").orElse(null).forGetter(itemExclusion -> Optional.ofNullable(itemExclusion.compoundTag)),
                ResourceLocation.CODEC.optionalFieldOf("tag").orElse(null).forGetter(itemExclusion -> Optional.ofNullable(itemExclusion.tag)),
                Codec.STRING.optionalFieldOf("mod").orElse(null).forGetter(itemExclusion -> Optional.ofNullable(itemExclusion.mod))
        ).apply(instance, ItemExclusion::new));
    }

    @Nullable
    public CompoundTag getCompoundNBT() {
        return compoundTag;
    }

    @Nullable
    public String getMod() {
        return mod;
    }

    @Nullable
    public ResourceLocation getTag() {
        return tag;
    }

    @Nullable
    public ResourceLocation getResourceLocation() {
        return item;
    }

    @Nullable
    public Item getItem() {
        return ForgeRegistries.ITEMS.getValue(item);
    }

    @Nullable
    public ItemStack getItemStack() {
        ItemStack itemStack = new ItemStack(getItem());
        if(getCompoundNBT() != null) itemStack.setTag(getCompoundNBT());
        return itemStack;
    }
}
