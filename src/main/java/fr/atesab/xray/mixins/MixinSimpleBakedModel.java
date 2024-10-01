package fr.atesab.xray.mixins;

import fr.atesab.xray.XrayMain;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.SimpleBakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;
import java.util.List;


@Mixin(value = SimpleBakedModel.class)
public class MixinSimpleBakedModel {
    @Inject(at = @At("HEAD"), method = "getQuads", cancellable = true)
    private void getQuads(@Nullable BlockState state, Direction par2, RandomSource par3, CallbackInfoReturnable<List<BakedQuad>> cir) {
        if (state != null && XrayMain.getMod().isBlockInvisible(state)) {
            cir.setReturnValue(List.of());
        }
    }
}
