package net.myriantics.impenduits.registry;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.StatFormatter;
import net.minecraft.stats.Stats;
import net.myriantics.impenduits.ImpenduitsCommon;

public abstract class ImpenduitsStatistics {
    public static ResourceLocation IMPENDUIT_FIELDS_ACTIVATED = register("impenduit_fields_activated");
    public static ResourceLocation IMPENDUIT_FIELDS_DEACTIVATED = register("impenduit_fields_deactivated");
    public static ResourceLocation IMPENDUIT_PYLON_POWER_CORES_REMOVED = register("impenduit_pylon_power_cores_removed");
    public static ResourceLocation IMPENDUIT_PYLON_POWER_CORES_INSERTED = register("impenduit_pylon_power_cores_inserted");

    private static ResourceLocation register(String name) {
        return register(name, StatFormatter.DEFAULT);
    }

    private static ResourceLocation register(String name, StatFormatter statFormatter) {
        ResourceLocation id = ImpenduitsCommon.locate(name);
        Registry.register(BuiltInRegistries.CUSTOM_STAT, name, id);
        Stats.CUSTOM.get(id, statFormatter);
        return id;
    }

    public static void init() {
        ImpenduitsCommon.LOGGER.info("Registered Impenduits' Statistics!");
    }
}
