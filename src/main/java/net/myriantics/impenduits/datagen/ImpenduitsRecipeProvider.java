package net.myriantics.impenduits.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.world.item.Items;
import net.myriantics.impenduits.registry.block.ImpenduitsBlocks;

import java.util.concurrent.CompletableFuture;

public class ImpenduitsRecipeProvider extends FabricRecipeProvider {
    public ImpenduitsRecipeProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    public void buildRecipes(RecipeOutput recipeExporter) {
        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, ImpenduitsBlocks.IMPENDUIT_PYLON, 4)
                .define('D', Items.DARK_PRISMARINE)
                .define('L', Items.SEA_LANTERN)
                .define('C', Items.PRISMARINE_CRYSTALS)
                .define('B', Items.PRISMARINE_BRICKS)
                .pattern("CCC")
                .pattern("DLD")
                .pattern("BDB")
                .unlockedBy("heart_of_the_sea", has(Items.HEART_OF_THE_SEA))
                .save(recipeExporter);
    }
}
