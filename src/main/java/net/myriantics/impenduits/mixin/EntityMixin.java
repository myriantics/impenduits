package net.myriantics.impenduits.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.myriantics.impenduits.blocks.ImpenduitFieldBlock;
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

    @Unique
    private boolean interimIsInsideImpenduitField;

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
    private void impenduits$initializeInterimVariable(CallbackInfo ci) {
        interimIsInsideImpenduitField = false;
    }

    @Inject(
            method = "checkBlockCollision",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;onEntityCollision(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/entity/Entity;)V")
    )
    private void impenduits$checkForImpenduitField(CallbackInfo ci, @Local BlockState checkedState) {
        if (!interimIsInsideImpenduitField) {
            this.interimIsInsideImpenduitField = checkedState.getBlock() instanceof ImpenduitFieldBlock;
        }
    }

    @Inject(
            method = "checkBlockCollision",
            at = @At(value = "TAIL")
    )
    private void impenduits$updateIndicatorVariable(CallbackInfo ci) {
        if (isInsideImpenduitField != interimIsInsideImpenduitField) {
            this.isInsideImpenduitField = this.interimIsInsideImpenduitField;
        }
    }
}
