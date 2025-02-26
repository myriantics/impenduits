package net.myriantics.impenduits.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.advancement.criterion.CriterionConditions;
import net.minecraft.advancement.criterion.ItemCriterion;
import net.minecraft.data.server.recipe.RecipeJsonProvider;
import net.minecraft.data.server.recipe.ShapedRecipeJsonBuilder;
import net.minecraft.item.Items;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.myriantics.impenduits.ImpenduitsCommon;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.function.Consumer;

public class ImpenduitsRecipeProvider extends FabricRecipeProvider {
    public ImpenduitsRecipeProvider(FabricDataOutput output) {
        super(output);
    }

    @Override
    public void generate(Consumer<RecipeJsonProvider> exporter) {
        ShapedRecipeJsonBuilder.create(RecipeCategory.REDSTONE, ImpenduitsCommon.IMPENDUIT_PYLON, 4)
                .input('D', Items.DARK_PRISMARINE)
                .input('L', Items.SEA_LANTERN)
                .input('C', Items.PRISMARINE_CRYSTALS)
                .input('B', Items.PRISMARINE_BRICKS)
                .pattern("CCC")
                .pattern("DLD")
                .pattern("BDB")
                .criterion("heart_of_the_sea", conditionsFromItem(Items.HEART_OF_THE_SEA))
                .offerTo(exporter);
    }
}
