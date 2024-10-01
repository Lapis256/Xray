package fr.atesab.xray.mixins;

import fr.atesab.xray.XrayMain;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(value = net.minecraft.client.renderer.chunk.VisGraph.class)
public class MixinVisGraph {
    @Inject(method = "setOpaque", at = @At("HEAD"), cancellable = true)
    private void setOpaque(BlockPos pos, CallbackInfo ci) {
//        if (XrayMain.getMod().isXrayEnabled()) {
//            ci.cancel();
//        }
    }
}
