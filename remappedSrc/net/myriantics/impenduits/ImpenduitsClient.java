package net.myriantics.impenduits;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.impl.blockrenderlayer.BlockRenderLayerMapImpl;
import net.minecraft.client.color.world.BiomeColors;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.Colors;
import net.minecraft.util.math.ColorHelper;

import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.image.ColorConvertOp;

public class ImpenduitsClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        BlockRenderLayerMap.INSTANCE.putBlock(ImpenduitsCommon.IMPENDUIT_FIELD, RenderLayer.getTranslucent());

        ColorProviderRegistry.BLOCK.register(
                ((state, world, pos, tintIndex) -> {
                    if (world == null || pos == null) {
                        return -1;
                    }

                    int waterColor = BiomeColors.getWaterColor(world, pos);

                    return ColorHelper.Argb.mixColor(waterColor, Colors.WHITE);
                }),
                ImpenduitsCommon.IMPENDUIT_FIELD
        );
    }
}
