package net.myriantics.impenduits.mixin.hud_overlay;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.renderer.ScreenEffectRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.myriantics.impenduits.blocks.ImpenduitFieldBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ScreenEffectRenderer.class)
public abstract class ScreenEffectRendererMixin {

    @ModifyExpressionValue(
            method = "renderScreenEffect",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isEyeInFluid(Lnet/minecraft/tags/TagKey;)Z")
    )
    private static boolean impenduits$renderWaterOverlayOverride(boolean original, @Local Player player) {
        return original || player.level().getBlockState(BlockPos.containing(player.getEyePosition())).getBlock() instanceof ImpenduitFieldBlock;
    }
}
