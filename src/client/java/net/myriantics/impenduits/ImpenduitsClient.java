package net.myriantics.impenduits;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.CommonColors;
import net.minecraft.util.FastColor;
import net.myriantics.impenduits.registry.block.ImpenduitsBlocks;

public class ImpenduitsClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        BlockRenderLayerMap.INSTANCE.putBlock(ImpenduitsBlocks.IMPENDUIT_FIELD, RenderType.translucent());

        ColorProviderRegistry.BLOCK.register(
                ((state, world, pos, tintIndex) -> {
                    if (world == null || pos == null) {
                        return -1;
                    }

                    int waterColor = BiomeColors.getAverageWaterColor(world, pos);

                    return FastColor.ARGB32.multiply(waterColor, CommonColors.WHITE);
                }),
                ImpenduitsBlocks.IMPENDUIT_FIELD
        );
    }
}
