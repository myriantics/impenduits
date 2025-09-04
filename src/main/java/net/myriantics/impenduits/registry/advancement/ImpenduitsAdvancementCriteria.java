package net.myriantics.impenduits.registry.advancement;

import net.minecraft.advancement.criterion.Criterion;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.myriantics.impenduits.ImpenduitsCommon;
import net.myriantics.impenduits.advancement.criterion.ImpenduitFieldActivationCriterion;
import net.myriantics.impenduits.advancement.criterion.ImpenduitFieldDeactivationCriterion;
import net.myriantics.impenduits.advancement.criterion.UltrawarmImpenduitFieldRiptideCriterion;

public abstract class ImpenduitsAdvancementCriteria {

    public static final UltrawarmImpenduitFieldRiptideCriterion ULTRAWARM_IMPENDUIT_FIELD_RIPTIDE_CRITERION = register("ultrawarm_impenduit_field_riptide", new UltrawarmImpenduitFieldRiptideCriterion());
    public static final ImpenduitFieldActivationCriterion IMPENDUIT_FIELD_ACTIVATION = register("impenduit_field_activation", new ImpenduitFieldActivationCriterion());
    public static final ImpenduitFieldDeactivationCriterion IMPENDUIT_FIELD_DEACTIVATION = register("impenduit_field_deactivation", new ImpenduitFieldDeactivationCriterion());

    private static <T extends Criterion<?>> T register(String name, T criterion) {
        return Registry.register(Registries.CRITERION, ImpenduitsCommon.locate(name), criterion);
    }

    public static void init() {
        ImpenduitsCommon.LOGGER.info("Registered Impenduits' Advancement Criteria");
    }
}
