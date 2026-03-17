package net.myriantics.impenduits.advancement.criterion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;
import net.myriantics.impenduits.ImpenduitsCommon;
import net.myriantics.impenduits.registry.advancement.ImpenduitsAdvancementCriteria;

import java.util.Optional;

public class ImpenduitFieldActivationCriterion extends SimpleCriterionTrigger<ImpenduitFieldActivationCriterion.Conditions> {

    @Override
    public Codec<Conditions> codec() {
        return Conditions.CODEC;
    }

    public void trigger(ServerPlayer serverPlayer) {
        this.trigger(serverPlayer, conditions -> true);
    }

    public record Conditions(Optional<ContextAwarePredicate> player) implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<ImpenduitFieldActivationCriterion.Conditions> CODEC = RecordCodecBuilder.create(
                instance -> instance.group(
                                EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(ImpenduitFieldActivationCriterion.Conditions::player)
                        )
                        .apply(instance, ImpenduitFieldActivationCriterion.Conditions::new)
        );

        public static Criterion<ImpenduitFieldActivationCriterion.Conditions> create() {
            return ImpenduitsAdvancementCriteria.IMPENDUIT_FIELD_ACTIVATION.createCriterion(new ImpenduitFieldActivationCriterion.Conditions(Optional.empty()));
        }
    }
}
