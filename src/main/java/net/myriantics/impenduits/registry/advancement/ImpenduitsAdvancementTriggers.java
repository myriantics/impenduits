package net.myriantics.impenduits.registry.advancement;

import net.minecraft.server.network.ServerPlayerEntity;

public abstract class ImpenduitsAdvancementTriggers {
    public static void triggerUltrawarmImpenduitFieldRiptide(ServerPlayerEntity serverPlayer) {
        ImpenduitsAdvancementCriteria.ULTRAWARM_IMPENDUIT_FIELD_RIPTIDE_CRITERION.trigger(serverPlayer);
    }

    public static void triggerImpenduitFieldActivation(ServerPlayerEntity serverPlayer) {
        ImpenduitsAdvancementCriteria.IMPENDUIT_FIELD_ACTIVATION.trigger(serverPlayer);
    }

    public static void triggerImpenduitFieldDeactivation(ServerPlayerEntity serverPlayer) {
        ImpenduitsAdvancementCriteria.IMPENDUIT_FIELD_DEACTIVATION.trigger(serverPlayer);
    }
}
