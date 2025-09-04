package net.myriantics.impenduits.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.TridentItem;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import net.myriantics.impenduits.registry.advancement.ImpenduitsAdvancementTriggers;
import net.myriantics.impenduits.util.ImpenduitFieldStatusAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TridentItem.class)
public class TridentItemMixin {
    @Inject(
            method = "onStoppedUsing",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;useRiptide(IFLnet/minecraft/item/ItemStack;)V")
    )
    private void impenduits$triggerRiptideInUltrawarmAdvancement(ItemStack stack, World world, LivingEntity user, int remainingUseTicks, CallbackInfo ci) {
        // trigger advancement if player uses riptide while touching impenduit field
        if (user instanceof ServerPlayerEntity serverPlayer && user instanceof ImpenduitFieldStatusAccess access && access.impenduits$isTouchingImpenduitField()) {
            ImpenduitsAdvancementTriggers.triggerUltrawarmImpenduitFieldRiptide(serverPlayer);
        }
    }
}
