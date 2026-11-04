package net.succ.dimensionlock;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

/**
 * The set of locked dimensions, saved with the world in data/dimensionlock.dat.
 */
public class LockedDimensions extends SavedData {
    private static final String DATA_NAME = Dimensionlock.MODID;
    private static final SavedData.Factory<LockedDimensions> FACTORY = new SavedData.Factory<>(LockedDimensions::new, LockedDimensions::load);

    private final Set<ResourceLocation> locked = new TreeSet<>();

    public static LockedDimensions get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(FACTORY, DATA_NAME);
    }

    private static LockedDimensions load(CompoundTag tag, HolderLookup.Provider registries) {
        LockedDimensions data = new LockedDimensions();
        ListTag list = tag.getList("locked", Tag.TAG_STRING);
        for (int i = 0; i < list.size(); i++) {
            ResourceLocation id = ResourceLocation.tryParse(list.getString(i));
            if (id != null) data.locked.add(id);
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag list = new ListTag();
        locked.forEach(id -> list.add(StringTag.valueOf(id.toString())));
        tag.put("locked", list);
        return tag;
    }

    public boolean isLocked(ResourceLocation dimension) {
        return locked.contains(dimension);
    }

    /** @return true if the dimension was not already locked */
    public boolean lock(ResourceLocation dimension) {
        boolean changed = locked.add(dimension);
        if (changed) setDirty();
        return changed;
    }

    /** @return true if the dimension was locked */
    public boolean unlock(ResourceLocation dimension) {
        boolean changed = locked.remove(dimension);
        if (changed) setDirty();
        return changed;
    }

    public Set<ResourceLocation> all() {
        return Collections.unmodifiableSet(locked);
    }
}
