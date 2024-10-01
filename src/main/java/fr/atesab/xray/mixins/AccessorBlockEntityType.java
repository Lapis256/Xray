package fr.atesab.xray.mixins;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Set;


@Mixin(BlockEntityType.class)
public interface AccessorBlockEntityType {
    @Accessor
    Set<Block> getValidBlocks();
}
