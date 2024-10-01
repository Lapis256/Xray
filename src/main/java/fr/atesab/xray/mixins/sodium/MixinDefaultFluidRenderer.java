package fr.atesab.xray.mixins.sodium;

import fr.atesab.xray.XrayMain;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Pseudo
@Mixin(targets = {"me.jellysquid.mods.sodium.client.render.chunk.compile.pipeline.FluidRenderer"}, remap = false)
public class MixinDefaultFluidRenderer {
    @Inject(at = @At("HEAD"), method = "isFluidOccluded", cancellable = true)
    private void onIsFluidOccluded(BlockAndTintGetter world, int x, int y, int z, Direction dir, Fluid fluid, CallbackInfoReturnable<Boolean> cir)  {
        if(XrayMain.getMod().isXrayEnabled()) {
            BlockState state = world.getBlockState(new BlockPos(x, y, z));
            cir.setReturnValue(XrayMain.getMod().isBlockInvisible(state));
        }
    }
}
