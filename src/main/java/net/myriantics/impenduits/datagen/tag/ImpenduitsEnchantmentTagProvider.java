package net.myriantics.impenduits.datagen.tag;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.myriantics.impenduits.tag.ImpenduitsEnchantmentTags;

import java.util.concurrent.CompletableFuture;

public class ImpenduitsEnchantmentTagProvider extends FabricTagProvider<Enchantment> {
    public ImpenduitsEnchantmentTagProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(output, Registries.ENCHANTMENT, registriesFuture);
    }

    @Override
    protected void addTags(HolderLookup.Provider wrapperLookup) {
        tag(ImpenduitsEnchantmentTags.IMPENDUIT_FIELD_WALKABLE_ENCHANTMENTS)
                .add(Enchantments.FROST_WALKER);
    }
}
