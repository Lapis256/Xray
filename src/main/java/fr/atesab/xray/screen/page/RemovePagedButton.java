package fr.atesab.xray.screen.page;

import fr.atesab.xray.widget.XrayButton;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

public class RemovePagedButton extends XrayButton {
    private static final Component REMOVE_COMPONENT = Component.literal("-").withStyle(ChatFormatting.RED);
    private static final Button.OnPress EMPTY_PRESS = btn -> {
    };

    private final PagedScreen<?> parent;

    public RemovePagedButton(PagedScreen<?> parent, int x, int y, int w, int h) {
        super(x, y, w, h, REMOVE_COMPONENT, EMPTY_PRESS);
        this.parent = parent;
    }

    @Override
    public void onPress() {
        parent.removeCurrent();
        super.onPress();
    }

}
