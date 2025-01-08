package net.myriantics.impenduits;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.minecraft.client.color.world.BiomeColors;

public class ImpenduitsClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ColorProviderRegistry.BLOCK.register(
                ((state, world, pos, tintIndex) -> {
                    if (world == null || pos == null) {
                        return -1;
                    }
                    return BiomeColors.getWaterColor(world, pos);
                }),
                ImpenduitsCommon.IMPENDUIT_FIELD
        );
    }
}
