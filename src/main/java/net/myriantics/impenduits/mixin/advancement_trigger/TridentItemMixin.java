package net.myriantics.impenduits.mixin.advancement_trigger;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.level.Level;
import net.myriantics.impenduits.registry.advancement.ImpenduitsAdvancementTriggers;
import net.myriantics.impenduits.util.ImpenduitFieldStatusAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TridentItem.class)
public abstract class TridentItemMixin {
    @Inject(
            method = "releaseUsing",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;startAutoSpinAttack(IFLnet/minecraft/world/item/ItemStack;)V")
    )
    private void impenduits$triggerRiptideInUltrawarmAdvancement(ItemStack stack, Level world, LivingEntity user, int remainingUseTicks, CallbackInfo ci) {
        // trigger advancement if player uses riptide while touching impenduit field
        if (user instanceof ServerPlayer serverPlayer && user instanceof ImpenduitFieldStatusAccess access && access.impenduits$isTouchingImpenduitField()) {
            ImpenduitsAdvancementTriggers.triggerUltrawarmImpenduitFieldRiptide(serverPlayer);
        }
    }
}
