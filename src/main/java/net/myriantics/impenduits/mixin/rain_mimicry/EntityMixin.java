package net.myriantics.impenduits.mixin.rain_mimicry;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.myriantics.impenduits.tag.ImpenduitsBlockTags;
import net.myriantics.impenduits.util.ImpenduitFieldStatusAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class EntityMixin implements ImpenduitFieldStatusAccess {

    @Unique
    private boolean impenduits$isTouchingImpenduitField;

    @Override
    public boolean impenduits$isTouchingImpenduitField() {
        return impenduits$isTouchingImpenduitField;
    }

    @ModifyReturnValue(
            method = "isInRain",
            at = @At(value = "RETURN")
    )
    private boolean impenduits$isBeingRainedOnOverride(boolean original) {
        return original || impenduits$isTouchingImpenduitField;
    }

    @Inject(
            method = "checkInsideBlocks",
            at = @At(value = "HEAD")
    )
    private void impenduits$initializeInterimVariable(CallbackInfo ci, @Share("impenduits$interimIsTouchingImpenduitField") LocalBooleanRef interimIsTouchingImpenduitField) {
        interimIsTouchingImpenduitField.set(false);
    }

    @Inject(
            method = "checkInsideBlocks",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;entityInside(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/Entity;)V")
    )
    private void impenduits$checkForImpenduitField(CallbackInfo ci, @Local BlockState checkedState, @Share("impenduits$interimIsTouchingImpenduitField") LocalBooleanRef interimIsTouchingImpenduitField) {
        if (!interimIsTouchingImpenduitField.get()) {
            interimIsTouchingImpenduitField.set(checkedState.is(ImpenduitsBlockTags.ENTITY_RAIN_MIMICKING_BLOCKS));
        }
    }

    @Inject(
            method = "checkInsideBlocks",
            at = @At(value = "TAIL")
    )
    private void impenduits$updateIndicatorVariable(CallbackInfo ci, @Share("impenduits$interimIsTouchingImpenduitField") LocalBooleanRef interimIsTouchingImpenduitField) {
        if (impenduits$isTouchingImpenduitField != interimIsTouchingImpenduitField.get()) {
            this.impenduits$isTouchingImpenduitField = interimIsTouchingImpenduitField.get();
        }
    }
}
