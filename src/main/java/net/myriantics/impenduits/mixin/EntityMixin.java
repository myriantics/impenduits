package net.myriantics.impenduits.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.myriantics.impenduits.ImpenduitsCommon;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class EntityMixin {

    @Shadow public abstract boolean isTouchingWaterOrRain();

    @Shadow public abstract boolean isInSwimmingPose();

    @Unique
    public boolean isInsideImpenduitField;

    @Unique
    private boolean interimIsInsideImpenduitField;

    @ModifyReturnValue(
            method = "isTouchingWaterOrRain",
            at = @At(value = "RETURN")
    )
    private boolean impenduitFieldOverride(boolean original) {
        return original || isInsideImpenduitField;
    }

    @ModifyReturnValue(
            method = "isWet",
            at = @At(value = "RETURN")
    )
    private boolean fireExtinguishingOverride(boolean original) {
        return original || isInsideImpenduitField;
    }

    @ModifyReturnValue(
            method = "isTouchingWater",
            at = @At(value = "RETURN")
    )
    private boolean swimmingOverride(boolean original) {
        // you can swim through them because funny
        return original || (isInsideImpenduitField && isInSwimmingPose());
    }

    @Inject(
            method = "checkBlockCollision",
            at = @At(value = "HEAD")
    )
    private void initializeInterimVariable(CallbackInfo ci) {
        interimIsInsideImpenduitField = false;
    }

    @Inject(
            method = "checkBlockCollision",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;onEntityCollision(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/entity/Entity;)V")
    )
    private void checkForImpenduitField(CallbackInfo ci, @Local BlockState checkedState) {
        if (!interimIsInsideImpenduitField) {
            this.interimIsInsideImpenduitField = checkedState.isOf(ImpenduitsCommon.IMPENDUIT_FIELD);
        }
    }

    @Inject(
            method = "checkBlockCollision",
            at = @At(value = "TAIL")
    )
    private void updateRealVariable(CallbackInfo ci) {
        if (isInsideImpenduitField != interimIsInsideImpenduitField) {
            this.isInsideImpenduitField = this.interimIsInsideImpenduitField;
        }
    }
}
