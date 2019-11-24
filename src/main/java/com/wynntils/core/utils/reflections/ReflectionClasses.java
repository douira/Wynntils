package com.wynntils.core.utils.reflections;

import net.minecraftforge.fml.relauncher.ReflectionHelper;

public enum ReflectionClasses {

    SPacketPlayerListItem$AddPlayerData("net.minecraft.network.play.server.SPacketPlayerListItem$AddPlayerData");

    public final Class<?> clazz;

    ReflectionClasses(String... names) {
        this.clazz = ReflectionHelper.getClass(ReflectionClasses.class.getClassLoader(), names);
    }
}