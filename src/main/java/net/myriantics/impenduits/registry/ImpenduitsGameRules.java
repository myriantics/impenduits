package net.myriantics.impenduits.registry;

import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.minecraft.world.level.GameRules;
import net.myriantics.impenduits.ImpenduitsCommon;

public abstract class ImpenduitsGameRules {
    public static final GameRules.Key<GameRules.IntegerValue> RULE_MAX_IMPENDUIT_FIELD_SIDE_LENGTH = register(
            "maxImpenduitFieldSideLength",
            GameRules.Category.UPDATES,
            24,
            0,
            64
    );

    private static GameRules.Key<GameRules.IntegerValue> register(String name, GameRules.Category category, int defaultValue, int minValue, int maxValue) {
        return GameRuleRegistry.register(ImpenduitsCommon.locateAlt(name), category, GameRuleFactory.createIntRule(24, 0, 64));
    }

    public static void init() {
        ImpenduitsCommon.LOGGER.info("Registered Impenduits' GameRules!");
    }
}
