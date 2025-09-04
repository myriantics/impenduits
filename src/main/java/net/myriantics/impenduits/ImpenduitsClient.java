package net.myriantics.impenduits;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.minecraft.client.color.world.BiomeColors;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.Colors;
import net.minecraft.util.math.ColorHelper;
import net.myriantics.impenduits.registry.ImpenduitsBlocks;

public class ImpenduitsClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        BlockRenderLayerMap.INSTANCE.putBlock(ImpenduitsBlocks.IMPENDUIT_FIELD, RenderLayer.getTranslucent());

        ColorProviderRegistry.BLOCK.register(
                ((state, world, pos, tintIndex) -> {
                    if (world == null || pos == null) {
                        return -1;
                    }

                    int waterColor = BiomeColors.getWaterColor(world, pos);

                    return ColorHelper.Argb.mixColor(waterColor, Colors.WHITE);
                }),
                ImpenduitsBlocks.IMPENDUIT_FIELD
        );
    }
}
