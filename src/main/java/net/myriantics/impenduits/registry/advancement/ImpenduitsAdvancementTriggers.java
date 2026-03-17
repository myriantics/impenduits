package net.myriantics.impenduits.registry.advancement;

import net.minecraft.server.level.ServerPlayer;

public abstract class ImpenduitsAdvancementTriggers {
    public static void triggerUltrawarmImpenduitFieldRiptide(ServerPlayer serverPlayer) {
        ImpenduitsAdvancementCriteria.ULTRAWARM_IMPENDUIT_FIELD_RIPTIDE_CRITERION.trigger(serverPlayer);
    }

    public static void triggerImpenduitFieldActivation(ServerPlayer serverPlayer) {
        ImpenduitsAdvancementCriteria.IMPENDUIT_FIELD_ACTIVATION.trigger(serverPlayer);
    }

    public static void triggerImpenduitFieldDeactivation(ServerPlayer serverPlayer) {
        ImpenduitsAdvancementCriteria.IMPENDUIT_FIELD_DEACTIVATION.trigger(serverPlayer);
    }
}
