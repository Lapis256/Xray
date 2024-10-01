package fr.atesab.xray.screen;

import java.util.Optional;
import java.util.stream.Stream;

import com.mojang.blaze3d.vertex.PoseStack;

import fr.atesab.xray.config.BlockConfig;
import fr.atesab.xray.screen.page.AddPagedButton;
import fr.atesab.xray.screen.page.AddPagedElement;
import fr.atesab.xray.screen.page.PagedElement;
import fr.atesab.xray.screen.page.PagedScreen;
import fr.atesab.xray.screen.page.RemovePagedButton;
import fr.atesab.xray.utils.KeyData;
import fr.atesab.xray.utils.XrayUtils;
import fr.atesab.xray.view.ViewMode;
import fr.atesab.xray.widget.BlockConfigWidget;
import fr.atesab.xray.widget.XrayButton;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

public abstract class XrayBlockModesConfig extends PagedScreen<BlockConfig> {
    private class PagedBlockMode extends PagedElement<BlockConfig> {
        private final BlockConfig cfg;
        private boolean textHover = false;
        private BlockConfigWidget blocks;

        public PagedBlockMode(BlockConfig cfg) {
            super(XrayBlockModesConfig.this);
            this.cfg = cfg;
        }

        public PagedBlockMode() {
            this(new BlockConfig());
        }

        @Override
        public void init() {
            int x = width / 2 - 230;
            addSubWidget(new XrayButton(x, 0, 71, 20, Component.literal(cfg.getModeName()).withStyle(Style.EMPTY.withColor(cfg.getColor())), btn -> {
                minecraft.setScreen(new XrayAbstractModeConfig(XrayBlockModesConfig.this, cfg));
            }));
            x += 75;
            blocks = addSubWidget(new BlockConfigWidget(x, 0, 125, 20, cfg, XrayBlockModesConfig.this));
            x += 129;
            addSubWidget(new XrayButton(x, 0, 56, 20, KeyData.getName(cfg.getKey()), btn -> {
                minecraft.setScreen(new KeySelector(XrayBlockModesConfig.this, cfg.getKey(), oKey -> {
                    cfg.setKey(oKey);
                    btn.setMessage(KeyData.getName(cfg.getKey()));
                }));
            }));
            x += 60;
            addSubWidget(new XrayButton(x, 0, 56, 20, Component.literal(cfg.isEnabled() ? "Enable" : "Disable").withStyle(cfg.isEnabled() ? ChatFormatting.GREEN : ChatFormatting.RED), btn -> {
                cfg.setEnabled(!cfg.isEnabled());
                btn.setMessage(Component.literal(cfg.isEnabled() ? "Enable" : "Disable").withStyle(cfg.isEnabled() ? ChatFormatting.GREEN : ChatFormatting.RED));
            }));
            x += 60;
            addSubWidget(new XrayButton(x, 0, 64, 20, cfg.getViewMode().getTitle(), btn -> {
                minecraft.setScreen(new EnumSelector<>(
                        Component.translatable("x13.mod.mode.view.title"), getParentScreen(), ViewMode.values()) {

                    @Override
                    protected void select(ViewMode element) {
                        cfg.setViewMode(element);
                        btn.setMessage(cfg.getViewMode().getTitle());
                    }

                });
            }));
            x += 68;
            addSubWidget(new XrayButton(x, 0, 20, 20, Component.translatable("x13.mod.template.little"), btn -> {
                minecraft.setScreen(new EnumSelector<BlockConfig.Template>(
                        Component.translatable("x13.mod.template"), XrayBlockModesConfig.this,
                        BlockConfig.Template.values()) {

                    @Override
                    protected void select(BlockConfig.Template template) {
                        String oldName = cfg.getModeName();
                        int color = cfg.getColor();
                        Optional<KeyData> key = cfg.getKey();
                        template.cloneInto(cfg);
                        cfg.setName(oldName);
                        cfg.setColor(color);
                        cfg.setKey(key);
                    }

                });
            }));
            x += 24;

            addSubWidget(new AddPagedButton<>(XrayBlockModesConfig.this,
                    x, 0, 20, 20, PagedBlockMode::new));
            x += 24;
            addSubWidget(new RemovePagedButton(XrayBlockModesConfig.this,
                    x, 0, 20, 20));
            super.init();
        }

        @Override
        public void updateDelta(int delta, int index) {
            blocks.setDeltaY(delta);
        }

        @Override
        public BlockConfig save() {
            return cfg;
        }
    }

    public XrayBlockModesConfig(Screen parent, Stream<BlockConfig> stream) {
        super(Component.translatable("x13.mod.mode"), parent, 24, stream);
    }

    @Override
    protected void initElements(Stream<BlockConfig> stream) {
        stream.map(PagedBlockMode::new).forEach(this::addElement);
        addElement(new AddPagedElement<>(this, PagedBlockMode::new));
    }
}
