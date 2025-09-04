package net.myriantics.impenduits.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricAdvancementProvider;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.advancement.AdvancementFrame;
import net.minecraft.advancement.criterion.ItemCriterion;
import net.minecraft.advancement.criterion.TickCriterion;
import net.minecraft.component.ComponentChanges;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.predicate.BlockPredicate;
import net.minecraft.predicate.NumberRange;
import net.minecraft.predicate.StatePredicate;
import net.minecraft.predicate.entity.EntityEquipmentPredicate;
import net.minecraft.predicate.entity.EntityFlagsPredicate;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.predicate.entity.LocationPredicate;
import net.minecraft.predicate.item.*;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.myriantics.impenduits.ImpenduitsCommon;
import net.myriantics.impenduits.advancement.criterion.ImpenduitFieldActivationCriterion;
import net.myriantics.impenduits.advancement.criterion.ImpenduitFieldDeactivationCriterion;
import net.myriantics.impenduits.advancement.criterion.UltrawarmImpenduitFieldRiptideCriterion;
import net.myriantics.impenduits.registry.block.ImpenduitsBlockStateProperties;
import net.myriantics.impenduits.registry.block.ImpenduitsBlocks;
import net.myriantics.impenduits.registry.item.ImpenduitsItems;
import net.myriantics.impenduits.tag.ImpenduitsEnchantmentTags;
import net.myriantics.impenduits.tag.ImpenduitsItemTags;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;

public class ImpenduitsAdvancementProvider extends FabricAdvancementProvider {

    private CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup;

    public ImpenduitsAdvancementProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup) {
        super(output, registryLookup);
        this.registryLookup = registryLookup;
    }

    @Override
    public void generateAdvancement(RegistryWrapper.WrapperLookup wrapperLookup, Consumer<AdvancementEntry> consumer) {

        RegistryWrapper.Impl<Enchantment> impl = wrapperLookup.getWrapperOrThrow(RegistryKeys.ENCHANTMENT);

        AdvancementEntry activateField = create((builder -> builder
                .parent(new AdvancementEntry(Identifier.ofVanilla("adventure/root"), null))
                .display(
                        ImpenduitsItems.IMPENDUIT_PYLON,
                        Text.translatable("advancements.adventure.activate_impenduit_field.title"),
                        Text.translatable("advancements.adventure.activate_impenduit_field.description"),
                        null,
                        AdvancementFrame.TASK,
                        true,
                        true,
                        false
                )
                .criterion(
                        "activate_impenduit_field",
                        ImpenduitFieldActivationCriterion.Conditions.create()
                )
                .build(consumer, ImpenduitsCommon.locate("activate_impenduit_field").toString())
        ));

        AdvancementEntry deactivateField = create((builder -> builder
                .parent(activateField)
                .display(
                        Items.IRON_PICKAXE,
                        Text.translatable("advancements.adventure.deactivate_impenduit_field.title"),
                        Text.translatable("advancements.adventure.deactivate_impenduit_field.description"),
                        null,
                        AdvancementFrame.TASK,
                        true,
                        true,
                        false
                )
                .criterion(
                        "deactivate_impenduit_field",
                        ImpenduitFieldDeactivationCriterion.Conditions.create()
                )
                .build(consumer, ImpenduitsCommon.locate("deactivate_impenduit_field").toString())
        ));

        AdvancementEntry frostWalkOnField = create((builder -> builder
                .parent(activateField)
                .display(
                        new ItemStack(Registries.ITEM.getEntry(Items.IRON_BOOTS), 1, ComponentChanges.builder().add(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true).build()),
                        Text.translatable("advancements.adventure.walk_on_impenduit_field_with_frost_walker.title"),
                        Text.translatable("advancements.adventure.walk_on_impenduit_field_with_frost_walker.description"),
                        null,
                        AdvancementFrame.GOAL,
                        true,
                        true,
                        false
                )
                .criterion(
                        "walk_on_impenduit_field_with_frost_walker",
                        TickCriterion.Conditions.createLocation(EntityPredicate.Builder.create()
                                .equipment(EntityEquipmentPredicate.Builder.create().feet(
                                            ItemPredicate.Builder.create().subPredicate(
                                                    ItemSubPredicateTypes.ENCHANTMENTS,
                                                    EnchantmentsPredicate.enchantments(List.of(new EnchantmentPredicate(
                                                            impl.getOrThrow(ImpenduitsEnchantmentTags.IMPENDUIT_FIELD_WALKABLE_ENCHANTMENTS),
                                                            NumberRange.IntRange.atLeast(1))
                                                    ))
                                            )))
                                .flags(
                                        EntityFlagsPredicate.Builder.create().onGround(true)
                                )
                                .steppingOn(
                                        LocationPredicate.Builder.create().block(
                                                BlockPredicate.Builder.create().blocks(
                                                        ImpenduitsBlocks.IMPENDUIT_FIELD
                                                )
                                        )
                                )
                        )
                )
                .build(consumer, ImpenduitsCommon.locate("walk_on_impenduit_field_with_frost_walker").toString())
        ));

        AdvancementEntry ultrawarmRiptide = create((builder -> builder
                .parent(activateField)
                .display(
                        new ItemStack(Registries.ITEM.getEntry(Items.TRIDENT), 1, ComponentChanges.builder().add(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true).build()),
                        Text.translatable("advancements.nether.use_riptide_in_ultrawarm_dimension_while_touching_impenduit_field.title"),
                        Text.translatable("advancements.nether.use_riptide_in_ultrawarm_dimension_while_touching_impenduit_field.description"),
                        null,
                        AdvancementFrame.CHALLENGE,
                        true,
                        true,
                        true
                )
                .criterion(
                        "use_riptide_in_ultrawarm_dimension_while_touching_impenduit_field",
                        UltrawarmImpenduitFieldRiptideCriterion.Conditions.create()
                )
                .build(consumer, ImpenduitsCommon.locate("use_riptide_in_ultrawarm_dimension_while_touching_impenduit_field").toString())
        ));
    }


    private AdvancementEntry create(Function<Advancement.Builder, AdvancementEntry> consumer) {
        return consumer.apply(Advancement.Builder.create());
    }
}
