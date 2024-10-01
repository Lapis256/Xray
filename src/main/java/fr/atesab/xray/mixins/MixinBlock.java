package fr.atesab.xray.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import fr.atesab.xray.XrayMain;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;


@Mixin(value = Block.class)
public abstract class MixinBlock {
    @Inject(at = @At("HEAD"), method = "shouldRenderFace(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;Lnet/minecraft/core/BlockPos;)Z", cancellable = true)
    private static void shouldRenderFace(BlockState state, BlockGetter reader, BlockPos pos, Direction face, BlockPos blockPosaaa, CallbackInfoReturnable<Boolean> cir) {
        if(XrayMain.getMod().isXrayEnabled()) {
            cir.setReturnValue(!XrayMain.getMod().isBlockInvisible(state));
        }
    }
}
