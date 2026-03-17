package net.myriantics.impenduits.mixin.coral_farming;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.StructureManager;
import net.myriantics.impenduits.tag.ImpenduitsStructureTags;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BoneMealItem.class)
public abstract class BoneMealItemMixin {
    @ModifyExpressionValue(
            method = "growWaterPlant",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/core/Holder;is(Lnet/minecraft/tags/TagKey;)Z")
    )
    private static boolean impenduits$checkForImpenduitVault(
            boolean original,
            @Local(argsOnly = true) Level world,
            @Local(ordinal = 1) BlockPos selectedPos
            ) {
        if (!original && world instanceof ServerLevel serverWorld) {
            StructureManager accessor = serverWorld.structureManager();

            original = accessor.getStructureWithPieceAt(selectedPos, ImpenduitsStructureTags.BONEMEAL_SPAWNS_CORAL_FANS_WITHIN).isValid();
        }

        return original;
    }
}
