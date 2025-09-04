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

public class UltrawarmImpenduitFieldRiptideCriterion extends AbstractCriterion<UltrawarmImpenduitFieldRiptideCriterion.Conditions> {

    @Override
    public Codec<Conditions> getConditionsCodec() {
        return Conditions.CODEC;
    }

    public void trigger(ServerPlayerEntity player) {
        this.trigger(player, conditions -> player.getServerWorld().getDimension().ultrawarm());
    }

    public record Conditions(Optional<LootContextPredicate> player) implements AbstractCriterion.Conditions {
        public static final Codec<UltrawarmImpenduitFieldRiptideCriterion.Conditions> CODEC = RecordCodecBuilder.create(
                instance -> instance.group(
                        EntityPredicate.LOOT_CONTEXT_PREDICATE_CODEC.optionalFieldOf("player").forGetter(UltrawarmImpenduitFieldRiptideCriterion.Conditions::player)
                )
                .apply(instance, UltrawarmImpenduitFieldRiptideCriterion.Conditions::new)
        );

        public static AdvancementCriterion<UltrawarmImpenduitFieldRiptideCriterion.Conditions> create() {
            return ImpenduitsAdvancementCriteria.ULTRAWARM_IMPENDUIT_FIELD_RIPTIDE_CRITERION.create(new UltrawarmImpenduitFieldRiptideCriterion.Conditions(Optional.empty()));
        }
    }
}
