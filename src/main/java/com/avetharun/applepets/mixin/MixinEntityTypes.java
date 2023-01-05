package com.avetharun.applepets.mixin;

import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.world.entity.EntityType;

import java.lang.reflect.Field;
import java.util.IdentityHashMap;

public class MixinEntityTypes {
    public static void unfreezeEntityRegistry() throws NoSuchFieldException, IllegalAccessException {
        Field intrusiveHolderCache = MappedRegistry.class.getDeclaredField("cc");
        intrusiveHolderCache.setAccessible(true);
        intrusiveHolderCache.set(Registry.ENTITY_TYPE, new IdentityHashMap<EntityType<?>, Holder.Reference<EntityType<?>>>());
        Field frozenField = MappedRegistry.class.getDeclaredField("ca");
        frozenField.setAccessible(true);
        frozenField.set(Registry.ENTITY_TYPE, false);
    }
}
