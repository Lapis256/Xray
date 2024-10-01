package fr.atesab.xray.mixins;

import fr.atesab.xray.XrayMain;
import net.minecraft.client.renderer.block.LiquidBlockRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(LiquidBlockRenderer.class)
public class MixinLiquidBlockRenderer {
    @Inject(method = "isFaceOccludedByNeighbor", at = @At("HEAD"), cancellable = true)
    private static void onIsFaceOccludedByNeighbor(BlockGetter blockGetter, BlockPos pos, Direction p_203182_, float p_203183_, BlockState p_203184_, CallbackInfoReturnable<Boolean> cir) {
        if(XrayMain.getMod().isXrayEnabled()) {
            BlockState state = blockGetter.getBlockState(pos);
            cir.setReturnValue(XrayMain.getMod().isBlockInvisible(state));
        }
    }
}
