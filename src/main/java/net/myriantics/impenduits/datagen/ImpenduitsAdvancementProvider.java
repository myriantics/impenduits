package net.myriantics.impenduits.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricAdvancementProvider;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.advancements.critereon.EnchantmentPredicate;
import net.minecraft.advancements.critereon.EntityEquipmentPredicate;
import net.minecraft.advancements.critereon.EntityFlagsPredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.ItemEnchantmentsPredicate;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.ItemSubPredicates;
import net.minecraft.advancements.critereon.LocationPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.PlayerTrigger;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
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

    private CompletableFuture<HolderLookup.Provider> registryLookup;

    public ImpenduitsAdvancementProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registryLookup) {
        super(output, registryLookup);
        this.registryLookup = registryLookup;
    }

    @Override
    public void generateAdvancement(HolderLookup.Provider wrapperLookup, Consumer<AdvancementHolder> consumer) {

        HolderLookup.RegistryLookup<Enchantment> impl = wrapperLookup.lookupOrThrow(Registries.ENCHANTMENT);

        AdvancementHolder activateField = create((builder -> builder
                .parent(new AdvancementHolder(ResourceLocation.withDefaultNamespace("adventure/root"), null))
                .display(
                        ImpenduitsItems.IMPENDUIT_PYLON,
                        Component.translatable("advancements.adventure.activate_impenduit_field.title"),
                        Component.translatable("advancements.adventure.activate_impenduit_field.description"),
                        null,
                        AdvancementType.TASK,
                        true,
                        true,
                        false
                )
                .addCriterion(
                        "activate_impenduit_field",
                        ImpenduitFieldActivationCriterion.Conditions.create()
                )
                .save(consumer, ImpenduitsCommon.locate("activate_impenduit_field").toString())
        ));

        AdvancementHolder deactivateField = create((builder -> builder
                .parent(activateField)
                .display(
                        Items.IRON_PICKAXE,
                        Component.translatable("advancements.adventure.deactivate_impenduit_field.title"),
                        Component.translatable("advancements.adventure.deactivate_impenduit_field.description"),
                        null,
                        AdvancementType.TASK,
                        true,
                        true,
                        false
                )
                .addCriterion(
                        "deactivate_impenduit_field",
                        ImpenduitFieldDeactivationCriterion.Conditions.create()
                )
                .save(consumer, ImpenduitsCommon.locate("deactivate_impenduit_field").toString())
        ));

        AdvancementHolder frostWalkOnField = create((builder -> builder
                .parent(activateField)
                .display(
                        new ItemStack(BuiltInRegistries.ITEM.wrapAsHolder(Items.IRON_BOOTS), 1, DataComponentPatch.builder().set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true).build()),
                        Component.translatable("advancements.adventure.walk_on_impenduit_field_with_frost_walker.title"),
                        Component.translatable("advancements.adventure.walk_on_impenduit_field_with_frost_walker.description"),
                        null,
                        AdvancementType.GOAL,
                        true,
                        true,
                        false
                )
                .addCriterion(
                        "walk_on_impenduit_field_with_frost_walker",
                        PlayerTrigger.TriggerInstance.located(EntityPredicate.Builder.entity()
                                .equipment(EntityEquipmentPredicate.Builder.equipment().feet(
                                            ItemPredicate.Builder.item().withSubPredicate(
                                                    ItemSubPredicates.ENCHANTMENTS,
                                                    ItemEnchantmentsPredicate.enchantments(List.of(new EnchantmentPredicate(
                                                            impl.getOrThrow(ImpenduitsEnchantmentTags.IMPENDUIT_FIELD_WALKABLE_ENCHANTMENTS),
                                                            MinMaxBounds.Ints.atLeast(1))
                                                    ))
                                            )))
                                .flags(
                                        EntityFlagsPredicate.Builder.flags().setOnGround(true)
                                )
                                .steppingOn(
                                        LocationPredicate.Builder.location().setBlock(
                                                BlockPredicate.Builder.block().of(
                                                        ImpenduitsBlocks.IMPENDUIT_FIELD
                                                )
                                        )
                                )
                        )
                )
                .save(consumer, ImpenduitsCommon.locate("walk_on_impenduit_field_with_frost_walker").toString())
        ));

        AdvancementHolder ultrawarmRiptide = create((builder -> builder
                .parent(activateField)
                .display(
                        new ItemStack(BuiltInRegistries.ITEM.wrapAsHolder(Items.TRIDENT), 1, DataComponentPatch.builder().set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true).build()),
                        Component.translatable("advancements.nether.use_riptide_in_ultrawarm_dimension_while_touching_impenduit_field.title"),
                        Component.translatable("advancements.nether.use_riptide_in_ultrawarm_dimension_while_touching_impenduit_field.description"),
                        null,
                        AdvancementType.CHALLENGE,
                        true,
                        true,
                        true
                )
                .addCriterion(
                        "use_riptide_in_ultrawarm_dimension_while_touching_impenduit_field",
                        UltrawarmImpenduitFieldRiptideCriterion.Conditions.create()
                )
                .save(consumer, ImpenduitsCommon.locate("use_riptide_in_ultrawarm_dimension_while_touching_impenduit_field").toString())
        ));
    }


    private AdvancementHolder create(Function<Advancement.Builder, AdvancementHolder> consumer) {
        return consumer.apply(Advancement.Builder.advancement());
    }
}
