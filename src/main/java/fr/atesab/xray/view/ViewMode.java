package fr.atesab.xray.view;

import fr.atesab.xray.color.EnumElement;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;

import java.util.function.Consumer;
import java.util.function.Function;

public enum ViewMode implements EnumElement {
    /**
     * Default mode, like in Xray and Redstone mode
     */
    EXCLUSIVE("x13.mod.mode.view.exclusive", new ItemStack(Blocks.DIAMOND_ORE), (il, state) -> il),
    /**
     * Inclusive mode, like in Cave Mode
     */
    INCLUSIVE("x13.mod.mode.view.inclusive", new ItemStack(Blocks.STONE), (il, state) -> !il && state.isAir());

    private final Viewer viewer;
    private final Component title;
    private final ItemStack icon;

    ViewMode(String translation, ItemStack icon, Viewer viewer) {
        this.viewer = viewer;
        this.icon = icon;
        this.title = Component.translatable(translation);
    }

    @Override
    public Component getTitle() {
        return title;
    }

    public Viewer getViewer() {
        return viewer;
    }

    @Override
    public ItemStack getIcon() {
        return icon;
    }
}
