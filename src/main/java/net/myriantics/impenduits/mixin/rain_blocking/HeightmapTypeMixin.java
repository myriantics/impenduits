package net.myriantics.impenduits.mixin.rain_blocking;

import net.minecraft.block.BlockState;
import net.minecraft.world.Heightmap;
import net.myriantics.impenduits.blocks.ImpenduitFieldBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Slice;

import java.util.function.Predicate;

@Mixin(Heightmap.Type.class)
public abstract class HeightmapTypeMixin {
    @ModifyArg(
            method = "<clinit>",
            at = @At(
                    value = "INVOKE",
                    // FUCKETH THOUST MCDEV FOR GASLIGHTING ME
                    // I HAD TO GO INTO THE BYTECODE TO RETRIEVE THIS GAH
                    target = "Lnet/minecraft/world/Heightmap$Type;<init>(Ljava/lang/String;ILjava/lang/String;Lnet/minecraft/world/Heightmap$Purpose;Ljava/util/function/Predicate;)V"
            ),
            slice = @Slice(
                    // apply this to all heightmap types starting from MOTION_BLOCKING
                    from = @At(
                            value = "INVOKE",
                            target = "Lnet/minecraft/world/Heightmap$Type;<init>(Ljava/lang/String;ILjava/lang/String;Lnet/minecraft/world/Heightmap$Purpose;Ljava/util/function/Predicate;)V",
                            ordinal = 4
                    )
            ),
            index = 4
    )
    private static Predicate<BlockState> impenduits$ensureImpenduitFieldsBlockRain(
            Predicate<BlockState> predicate
    ) {
        return predicate.or((state -> state.getBlock() instanceof ImpenduitFieldBlock));
    }
}
