package fr.atesab.xray.mixins.sodium;

import fr.atesab.xray.XrayMain;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Pseudo
@Mixin(targets = {"me.jellysquid.mods.sodium.client.render.chunk.compile.pipeline.BlockOcclusionCache"}, remap = false)
public class MixinBlockOcclusionCache {
    @Inject(at = @At("RETURN"), method = "shouldDrawSide", cancellable = true)
    public void shouldDrawSide(BlockState state, BlockGetter world, BlockPos pos, Direction side, CallbackInfoReturnable<Boolean> cir) {
        if(XrayMain.getMod().isXrayEnabled()) {
            cir.setReturnValue(!XrayMain.getMod().isBlockInvisible(state));
        }
    }
}
