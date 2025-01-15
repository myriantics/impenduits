package net.myriantics.impenduits.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.gui.hud.InGameOverlayRenderer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.myriantics.impenduits.ImpenduitsCommon;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(InGameOverlayRenderer.class)
public abstract class InGameOverlayRendererMixin {

    @ModifyExpressionValue(
            method = "renderOverlays",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;isSubmergedIn(Lnet/minecraft/registry/tag/TagKey;)Z")
    )
    private static boolean impenduits$renderWaterOverlayOverride(boolean original, @Local PlayerEntity player) {
        return original || player.getWorld().getBlockState(BlockPos.ofFloored(player.getEyePos())).isOf(ImpenduitsCommon.IMPENDUIT_FIELD);
    }
}
