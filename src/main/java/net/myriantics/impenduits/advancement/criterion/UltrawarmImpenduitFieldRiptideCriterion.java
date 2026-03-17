package net.myriantics.impenduits.advancement.criterion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;
import net.myriantics.impenduits.registry.advancement.ImpenduitsAdvancementCriteria;

import java.util.Optional;

public class UltrawarmImpenduitFieldRiptideCriterion extends SimpleCriterionTrigger<UltrawarmImpenduitFieldRiptideCriterion.Conditions> {

    @Override
    public Codec<Conditions> codec() {
        return Conditions.CODEC;
    }

    public void trigger(ServerPlayer player) {
        this.trigger(player, conditions -> player.serverLevel().dimensionType().ultraWarm());
    }

    public record Conditions(Optional<ContextAwarePredicate> player) implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<UltrawarmImpenduitFieldRiptideCriterion.Conditions> CODEC = RecordCodecBuilder.create(
                instance -> instance.group(
                        EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(UltrawarmImpenduitFieldRiptideCriterion.Conditions::player)
                )
                .apply(instance, UltrawarmImpenduitFieldRiptideCriterion.Conditions::new)
        );

        public static Criterion<UltrawarmImpenduitFieldRiptideCriterion.Conditions> create() {
            return ImpenduitsAdvancementCriteria.ULTRAWARM_IMPENDUIT_FIELD_RIPTIDE_CRITERION.createCriterion(new UltrawarmImpenduitFieldRiptideCriterion.Conditions(Optional.empty()));
        }
    }
}
