package net.myriantics.impenduits.advancement.criterion;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancement.AdvancementCriterion;
import net.minecraft.advancement.criterion.AbstractCriterion;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.predicate.entity.LootContextPredicate;
import net.minecraft.server.network.ServerPlayerEntity;
import net.myriantics.impenduits.registry.advancement.ImpenduitsAdvancementCriteria;

import java.util.Optional;

public class ImpenduitFieldDeactivationCriterion extends AbstractCriterion<ImpenduitFieldDeactivationCriterion.Conditions> {

    @Override
    public Codec<Conditions> getConditionsCodec() {
        return Conditions.CODEC;
    }

    public void trigger(ServerPlayerEntity serverPlayer) {
        this.trigger(serverPlayer, conditions -> true);
    }

    public record Conditions(Optional<LootContextPredicate> player) implements AbstractCriterion.Conditions {
        public static final Codec<ImpenduitFieldDeactivationCriterion.Conditions> CODEC = RecordCodecBuilder.create(
                instance -> instance.group(
                                EntityPredicate.LOOT_CONTEXT_PREDICATE_CODEC.optionalFieldOf("player").forGetter(ImpenduitFieldDeactivationCriterion.Conditions::player)
                        )
                        .apply(instance, ImpenduitFieldDeactivationCriterion.Conditions::new)
        );

        public static AdvancementCriterion<ImpenduitFieldDeactivationCriterion.Conditions> create() {
            return ImpenduitsAdvancementCriteria.IMPENDUIT_FIELD_DEACTIVATION.create(new ImpenduitFieldDeactivationCriterion.Conditions(Optional.empty()));
        }
    }
}
