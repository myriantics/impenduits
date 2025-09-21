package net.myriantics.impenduits.registry;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.stat.StatFormatter;
import net.minecraft.stat.Stats;
import net.minecraft.util.Identifier;
import net.myriantics.impenduits.ImpenduitsCommon;

public abstract class ImpenduitsStatistics {
    public static Identifier IMPENDUIT_FIELDS_ACTIVATED = register("impenduit_fields_activated");
    public static Identifier IMPENDUIT_FIELDS_DEACTIVATED = register("impenduit_fields_deactivated");
    public static Identifier IMPENDUIT_PYLON_POWER_CORES_REMOVED = register("impenduit_pylon_power_cores_removed");
    public static Identifier IMPENDUIT_PYLON_POWER_CORES_INSERTED = register("impenduit_pylon_power_cores_inserted");

    private static Identifier register(String name) {
        return register(name, StatFormatter.DEFAULT);
    }

    private static Identifier register(String name, StatFormatter statFormatter) {
        Identifier id = ImpenduitsCommon.locate(name);
        Registry.register(Registries.CUSTOM_STAT, name, id);
        Stats.CUSTOM.getOrCreateStat(id, statFormatter);
        return id;
    }

    public static void init() {
        ImpenduitsCommon.LOGGER.info("Registered Impenduits' Statistics!");
    }
}
