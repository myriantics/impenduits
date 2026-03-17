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

public class ImpenduitFieldDeactivationCriterion extends SimpleCriterionTrigger<ImpenduitFieldDeactivationCriterion.Conditions> {

    @Override
    public Codec<Conditions> codec() {
        return Conditions.CODEC;
    }

    public void trigger(ServerPlayer serverPlayer) {
        this.trigger(serverPlayer, conditions -> true);
    }

    public record Conditions(Optional<ContextAwarePredicate> player) implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<ImpenduitFieldDeactivationCriterion.Conditions> CODEC = RecordCodecBuilder.create(
                instance -> instance.group(
                                EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(ImpenduitFieldDeactivationCriterion.Conditions::player)
                        )
                        .apply(instance, ImpenduitFieldDeactivationCriterion.Conditions::new)
        );

        public static Criterion<ImpenduitFieldDeactivationCriterion.Conditions> create() {
            return ImpenduitsAdvancementCriteria.IMPENDUIT_FIELD_DEACTIVATION.createCriterion(new ImpenduitFieldDeactivationCriterion.Conditions(Optional.empty()));
        }
    }
}
