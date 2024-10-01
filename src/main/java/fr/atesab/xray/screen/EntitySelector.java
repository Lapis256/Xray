package fr.atesab.xray.screen;

import fr.atesab.xray.XrayMain;
import fr.atesab.xray.utils.GuiUtils;
import fr.atesab.xray.widget.XrayButton;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

public abstract class EntitySelector extends XrayScreen {
    private final List<XrayEntityMenu.EntityUnion> entities;
    private final List<XrayEntityMenu.EntityUnion> visible = new ArrayList<>();
    private EditBox searchBar;
    private XrayButton lastButton;
    private XrayButton nextButton;
    private int elementByPage = 1;
    private int elementsX = 1;
    private int elementsY = 1;
    private int page = 0;
    private int maxPage = 0;

    public EntitySelector(Screen parent) {
        super(Component.translatable("x13.mod.esp.selector"), parent);
        entities =  Stream.concat(
                ForgeRegistries.ENTITY_TYPES.getValues().stream().map(XrayEntityMenu.EntityUnion::new),
                ForgeRegistries.BLOCK_ENTITY_TYPES.getValues().stream().map(XrayEntityMenu.EntityUnion::new)
        ).sorted(Comparator.comparing(XrayEntityMenu.EntityUnion::text)).toList();
    }

    @Override
    protected void init() {
        int sizeX = Math.min(width, 400);
        int sizeY = Math.min(height - 48, 400);

        elementsX = sizeX / 200;
        elementsY = sizeY / 20;
        elementByPage = elementsY;

        int pageTop = height / 2 - sizeY / 2 - 24;
        int pageBottom = height / 2 + sizeY / 2 + 2;

        searchBar = new EditBox(font, width / 2 - sizeX / 2, pageTop + 2, sizeX, 16, Component.literal("")) {
            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                if (button == 1 && mouseX >= this.getX() && mouseX <= this.getX() + this.width && mouseY >= this.getY()
                        && mouseY <= this.getY() + this.height) {
                    setValue("");
                    return true;
                }
                return super.mouseClicked(mouseX, mouseY, button);
            }

            @Override
            public void setValue(@NotNull String text) {
                super.setValue(text);
                updateSearch();
            }

            @Override
            public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
                if (super.keyPressed(keyCode, scanCode, modifiers)) {
                    updateSearch();
                    return true;
                }
                return false;
            }

            @Override
            public boolean charTyped(char chr, int modifiers) {
                if (super.charTyped(chr, modifiers)) {
                    updateSearch();
                    return true;
                }
                return false;
            }
        };

        lastButton = new XrayButton(width / 2 - 124, pageBottom, 20, 20, Component.literal("<-"), b -> {
            lastPage();
        });
        XrayButton cancelBtn = new XrayButton(width / 2 - 100, pageBottom, 200, 20, Component.translatable("gui.cancel"),
                b -> getMinecraft().setScreen(parent));
        nextButton = new XrayButton(width / 2 + 104, pageBottom, 20, 20, Component.literal("->"), b -> {
            nextPage();
        });

        addWidget(searchBar);
        addRenderableWidget(lastButton);
        addRenderableWidget(cancelBtn);
        addRenderableWidget(nextButton);

        updateArrows();
        updateSearch();

        setFocused(searchBar);
    }

    public void updateArrows() {
        nextButton.active = (page + 1) * elementByPage < visible.size(); // have last page
        lastButton.active = page * elementByPage > 0; // have next page
    }

    public void updateSearch() {
        String query = searchBar.getValue().toLowerCase();
        visible.clear();
        entities.stream().filter(e -> e.text().toLowerCase().contains(query)).forEach(visible::add);
        page = Math.min(visible.size() / elementByPage, page);
        maxPage = Math.max(1, visible.size() / elementByPage + (visible.size() % elementByPage != 0 ? 1 : 0));
        updateArrows();
    }

    public List<XrayEntityMenu.EntityUnion> getView() {
        return visible.subList(page * elementByPage, Math.min((page + 1) * elementByPage, visible.size()));
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        searchBar.render(graphics, mouseX, mouseY, partialTick);
        int left = width / 2 - 200 / 2;
        int top = height / 2 - elementsY * 20 / 2;

        List<XrayEntityMenu.EntityUnion> view = getView();
        int i;
        for (i = 0; i < view.size(); i++) {
            XrayEntityMenu.EntityUnion b = view.get(i);
            int x = left;
            int y = top + (i % elementsY) * 20;

            int color;

            if (mouseX >= x && mouseX <= x + 200 && mouseY >= y && mouseY <= y + 20) {
                color = 0x33ffffff;
            } else {
                color = 0x22ffffff;
            }

            graphics.fill(x, y, x + 200, y + 20, color);
            GuiUtils.renderItemIdentity(graphics, b.getIcon(), x + 1, y + 1);
            graphics.drawString(font, b.getTitle(), x + 22, y + 20 / 2 - font.lineHeight / 2, 0xffffffff);
        }
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    public void lastPage() {
        if (page != 0) {
            --page;
            if (page == 0)
                lastButton.active = false;
            nextButton.active = true;
        } else {
            lastButton.active = false;
        }
    }

    public void nextPage() {
        if (page + 1 != maxPage) {
            ++page;
            if (page + 1 == maxPage)
                nextButton.active = false;

            lastButton.active = true;
        } else {
            nextButton.active = false;
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button))
            return true;

        int left = width / 2 - 200 / 2;
        int top = height / 2 - elementsY * 20 / 2;

        List<XrayEntityMenu.EntityUnion> view = getView();
        int i;
        for (i = 0; i < view.size(); i++) {
            XrayEntityMenu.EntityUnion b = view.get(i);
            int x = left;
            int y = top + (i % elementsY) * 20;

            if (mouseX >= x && mouseX <= x + 200 && mouseY >= y && mouseY <= y + 20) {
                if (button == 0) { // left click: select
                    save(b);
                    getMinecraft().setScreen(parent);
                    return true;
                }
                return false;
            }
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scroll) {
        if (scroll < 0) {
            nextPage();
        } else {
            lastPage();
        }
        updateArrows();
        return super.mouseScrolled(mouseX, mouseY, scroll);
    }

    /**
     * save the selected block (only call when a Block is selected, doesn't call
     * after a cancel)
     * 
     * @param selection the selected block
     */
    protected abstract void save(XrayEntityMenu.EntityUnion selection);
}
