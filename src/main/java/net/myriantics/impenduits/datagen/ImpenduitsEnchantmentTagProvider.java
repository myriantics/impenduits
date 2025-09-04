package net.myriantics.impenduits.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.myriantics.impenduits.tag.ImpenduitsEnchantmentTags;

import java.util.concurrent.CompletableFuture;

public class ImpenduitsEnchantmentTagProvider extends FabricTagProvider<Enchantment> {
    public ImpenduitsEnchantmentTagProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, RegistryKeys.ENCHANTMENT, registriesFuture);
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup wrapperLookup) {
        getOrCreateTagBuilder(ImpenduitsEnchantmentTags.IMPENDUIT_FIELD_WALKABLE_ENCHANTMENTS)
                .add(Enchantments.FROST_WALKER);
    }
}
