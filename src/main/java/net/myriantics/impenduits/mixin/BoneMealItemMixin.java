package net.myriantics.impenduits.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.item.BoneMealItem;
import net.minecraft.registry.tag.StructureTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.StructureAccessor;
import net.myriantics.impenduits.tag.ImpenduitsStructureTags;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BoneMealItem.class)
public abstract class BoneMealItemMixin {
    @ModifyExpressionValue(
            method = "useOnGround",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/registry/entry/RegistryEntry;isIn(Lnet/minecraft/registry/tag/TagKey;)Z")
    )
    private static boolean impenduits$checkForImpenduitVault(
            boolean original,
            @Local(argsOnly = true) World world,
            @Local(ordinal = 1) BlockPos selectedPos
            ) {
        if (!original && world instanceof ServerWorld serverWorld) {
            StructureAccessor accessor = serverWorld.getStructureAccessor();

            original = accessor.getStructureContaining(selectedPos, ImpenduitsStructureTags.BONEMEAL_SPAWNS_CORAL_FANS_WITHIN).hasChildren();
        }

        return original;
    }
}
