package net.myriantics.impenduits.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.myriantics.impenduits.blocks.ImpenduitFieldBlock;
import net.myriantics.impenduits.util.ImpenduitsTags;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class EntityMixin {

    @Unique
    public boolean isInsideImpenduitField;

    @ModifyReturnValue(
            method = "isBeingRainedOn",
            at = @At(value = "RETURN")
    )
    private boolean impenduits$isBeingRainedOnOverride(boolean original) {
        return original || isInsideImpenduitField;
    }

    @Inject(
            method = "checkBlockCollision",
            at = @At(value = "HEAD")
    )
    private void impenduits$initializeInterimVariable(CallbackInfo ci, @Share("interimIsInsideImpenduitField") LocalBooleanRef interimIsInsideImpenduitField) {

        interimIsInsideImpenduitField.set(false);
    }

    @Inject(
            method = "checkBlockCollision",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;onEntityCollision(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/entity/Entity;)V")
    )
    private void impenduits$checkForImpenduitField(CallbackInfo ci, @Local BlockState checkedState, @Share("interimIsInsideImpenduitField") LocalBooleanRef interimIsInsideImpenduitField) {
        if (!interimIsInsideImpenduitField.get()) {
            interimIsInsideImpenduitField.set(checkedState.isIn(ImpenduitsTags.ENTITY_RAIN_MIMICKING_BLOCKS));
        }
    }

    @Inject(
            method = "checkBlockCollision",
            at = @At(value = "TAIL")
    )
    private void impenduits$updateIndicatorVariable(CallbackInfo ci, @Share("interimIsInsideImpenduitField") LocalBooleanRef interimIsInsideImpenduitField) {
        if (isInsideImpenduitField != interimIsInsideImpenduitField.get()) {
            this.isInsideImpenduitField = interimIsInsideImpenduitField.get();
        }
    }
}
