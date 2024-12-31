package net.myriantics.impenduits.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.entity.Entity;
import net.minecraft.fluid.Fluid;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.myriantics.impenduits.ImpenduitsCommon;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin {

    @Shadow private World world;

    @Shadow public abstract boolean isWet();

    @Inject(
            method = "updateMovementInFluid",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/fluid/FluidState;isIn(Lnet/minecraft/registry/tag/TagKey;)Z")
    )
    private void checkForImpenduitField(TagKey<Fluid> tag, double speed, CallbackInfoReturnable<Boolean> cir, @Local BlockPos.Mutable blockPos, @Local(ordinal = 1) LocalBooleanRef isTouchingWater) {

       isWet();

        // if it's not already touching water, set the boolean to true if the blockpos targets an Impenduit Field block
        if (!isTouchingWater.get()) {
            isTouchingWater.set(world.getBlockState(blockPos).isOf(ImpenduitsCommon.IMPENDUIT_FIELD));
        }
    }
}
