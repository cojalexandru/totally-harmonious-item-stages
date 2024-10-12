package com.decursioteam.decursio_stages.utils;

import net.minecraft.nbt.*;
public class NBTComparisonUtil {
    public static boolean areNBTCompoundsEqual(CompoundTag tag1, CompoundTag tag2) {
        if (tag1 == tag2) return true;
        if (tag1 == null || tag2 == null) return false;
        if (tag1.size() != tag2.size()) return false;

        for (String key : tag1.getAllKeys()) {
            if (!tag2.contains(key)) return false;
            if (!areNBTElementsEqual(tag1.get(key), tag2.get(key))) return false;
        }
        return true;
    }

    private static boolean areNBTElementsEqual(Tag element1, Tag element2) {
        if (element1 == element2) return true;
        if (element1 == null || element2 == null) return false;

        if (element1 instanceof ByteTag && element2 instanceof ByteTag) {
            byte b1 = ((ByteTag) element1).getAsByte();
            byte b2 = ((ByteTag) element2).getAsByte();
            return (b1 == 0 && b2 == 0) || (b1 == 1 && b2 == 1);
        }

        if (element1.getClass() != element2.getClass()) {
            if (element1 instanceof NumericTag && element2 instanceof NumericTag) {
                return ((NumericTag) element1).getAsNumber().doubleValue() == ((NumericTag) element2).getAsNumber().doubleValue();
            }
            return false;
        }

        if (element1 instanceof CompoundTag) {
            return areNBTCompoundsEqual((CompoundTag) element1, (CompoundTag) element2);
        } else if (element1 instanceof ListTag) {
            ListTag list1 = (ListTag) element1;
            ListTag list2 = (ListTag) element2;
            if (list1.size() != list2.size()) return false;
            for (int i = 0; i < list1.size(); i++) {
                if (!areNBTElementsEqual(list1.get(i), list2.get(i))) return false;
            }
            return true;
        } else {
            return element1.equals(element2);
        }
    }
}
