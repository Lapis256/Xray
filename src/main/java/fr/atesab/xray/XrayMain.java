package fr.atesab.xray;

import java.io.File;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import com.mojang.blaze3d.platform.InputConstants;

import fr.atesab.xray.esp.EspRender;
import fr.atesab.xray.view.ViewMode;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.client.event.*;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import fr.atesab.xray.color.ColorSupplier;
import fr.atesab.xray.color.IColorObject;
import fr.atesab.xray.color.TextHudBuffer;
import fr.atesab.xray.config.AbstractModeConfig;
import fr.atesab.xray.config.BlockConfig;
import fr.atesab.xray.config.LocationFormatTool;
import fr.atesab.xray.config.XrayConfig;
import fr.atesab.xray.screen.XrayMenu;
import fr.atesab.xray.utils.KeyInput;
import fr.atesab.xray.utils.XrayUtils;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(XrayMain.MOD_ID)
public class XrayMain {
    public static final String MOD_ID = "atianxray";
    public static final String MOD_NAME = "Xray";
    public static final String[] MOD_AUTHORS = {"ATE47", "ThaEin", "ALFECLARE"};
    public static final URL MOD_SOURCE = XrayUtils.soWhat(() -> new URL("https://github.com/ate47/Xray"));
    public static final URL MOD_ISSUE = XrayUtils.soWhat(() -> new URL("https://github.com/ate47/Xray/issues"));
    public static final URL MOD_LINK = XrayUtils
            .soWhat(() -> new URL("https://www.curseforge.com/minecraft/mc-mods/xray-1-13-rift-modloader"));
    private static final int maxFullbrightStates = 20;
    public static final Logger log = LogManager.getLogger(MOD_ID);

    private static XrayMain instance;

    private final OptionInstance<Double> gammaBypass = new OptionInstance<>(
            "options.gamma", OptionInstance.noTooltip(), (optionText, value) -> Component.empty(), OptionInstance.UnitDouble.INSTANCE.xmap(
            d -> (double) getInternalFullbrightState(), d -> 1
    ), 0.5, value -> {
    });

    private boolean fullBrightEnable = false;

    private int internalFullbrightState = 0;

    private KeyMapping configKey, fullbrightKey, locationEnableKey;

    private XrayConfig config;

    private int fullbrightColor = 0;

    private final IColorObject fullbrightMode = new IColorObject() {
        public int getColor() {
            return fullbrightColor;
        }

        public String getModeName() {
            return I18n.get("x13.mod.fullbright");
        }
    };

    /**
     * Toggle fullBright
     */
    public XrayMain fullBright() {
        return fullBright(!fullBrightEnable);
    }

    /**
     * Set fullBright
     */
    public XrayMain fullBright(boolean enable) {
        fullBrightEnable = enable;
        return internalFullbright();
    }

    @SuppressWarnings("deprecation")
    public static <T> T getBlockNamesCollected(Collection<Block> blocks, Collector<CharSequence, ?, T> collector) {
        // BLOCK
        return blocks.stream().filter(b -> !Blocks.AIR.equals(b)).map(BuiltInRegistries.BLOCK::getId).map(Objects::toString).collect(collector);
    }

    /**
     * get a list of block names from a list of blocks
     */
    public static List<CharSequence> getBlockNamesToList(Collection<Block> blocks) {
        return getBlockNamesCollected(blocks, Collectors.toList());
    }

    /**
     * get a String of a list of block names join by space from a list of blocks
     */
    public static String getBlockNamesToString(Collection<Block> blocks) {
        return getBlockNamesCollected(blocks, Collectors.joining(" "));
    }

    /**
     * load internal fullbright by checking if a mode is enabled
     */
    public XrayMain internalFullbright() {
        if (fullBrightEnable) {
            if (internalFullbrightState == 0)
                internalFullbrightState = 1;
            return this;
        }
        boolean modeEnabled = config.getSelectedBlockMode() != null;

        if (modeEnabled) {
            internalFullbrightState = maxFullbrightStates;
        } else {
            internalFullbrightState = 0;
        }
        return this;
    }

    public XrayConfig getConfig() {
        return config;
    }

    public boolean isFullBrightEnable() {
        return fullBrightEnable;
    }

    public boolean isInternalFullbrightEnable() {
        return getInternalFullbrightState() != 0;
    }

    /**
     * @return the internalFullbrightEnable
     */
    public float getInternalFullbrightState() {
        return 20f * internalFullbrightState / maxFullbrightStates;
    }

    /**
     * @return the gamma option
     */
    public OptionInstance<Double> getGammaBypass() {
        // force value
        gammaBypass.set(1.0);
        return gammaBypass;
    }


    private static void log(String message) {
        log.info("[{}] {}", log.getName(), message);
    }

    /**
     * Reload modules
     */
    public XrayMain modules() {
        fullBright(isFullBrightEnable());
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc.levelRenderer != null)
                mc.levelRenderer.allChanged();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
        return this;
    }

    public boolean isXrayEnabled() {
        return getConfig().getBlockConfigs().stream().anyMatch(AbstractModeConfig::isEnabled);
    }

    public boolean isBlockInvisible(BlockState state) {
        var filtered = getConfig().getBlockConfigs().stream().filter(BlockConfig::isEnabled).toList();
        if (filtered.isEmpty()) {
            return false;
        }
        return filtered.stream().anyMatch(mode -> !mode.isVisible(state));
    }

    public static String significantNumbers(double d) {
        boolean a = d < 0;
        if (a) {
            d *= -1;
        }
        int d1 = (int) (d);
        d %= 1;
        String s = String.format("%.3G", d);
        if (s.length() > 0)
            s = s.substring(1);
        if (s.contains("E+"))
            s = String.format(Locale.US, "%.0f", Double.valueOf(String.format("%.3G", d)));
        return (a ? "-" : "") + d1 + s;
    }

    public XrayMain() {
        instance = this;
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.addListener(this::setup);
        bus.addListener(this::registerKeyBinding);

        MinecraftForge.EVENT_BUS.register(this);

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> MinecraftForge.EVENT_BUS.addListener(EspRender::handler));
    }

    /**
     * get this mod
     */
    public static XrayMain getMod() {
        return instance;
    }

    /**
     * Mod config file
     */
    public static File getSaveFile() {
        Minecraft mc = Minecraft.getInstance();
        return new File(mc.gameDirectory, "config/xray2.json");
    }

    /**
     * Load mod configs
     */
    public void loadConfigs() {
        config = XrayConfig.sync(getSaveFile());
    }

    @SubscribeEvent
    public void onEndTickEvent(TickEvent.ClientTickEvent ev) {
        if (ev.phase != Phase.END)
            return;
        if (internalFullbrightState != 0 && internalFullbrightState < maxFullbrightStates) {
            internalFullbrightState++;
        }
    }

    @SubscribeEvent
    public void onKeyEvent(InputEvent.Key ev) {
        Minecraft client = Minecraft.getInstance();
        if (client.screen != null)
            return;

        KeyInput input = new KeyInput(ev.getKey(), ev.getScanCode(), ev.getAction(), ev.getModifiers());

        if (InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), input.key())) {
            config.getModes().forEach(mode -> mode.onKeyInput(input));
        }

        if (fullbrightKey.consumeClick()) {
            fullBright();
        }
        if (locationEnableKey.consumeClick()) {
            config.getLocationConfig().setEnabled(!config.getLocationConfig().isEnabled());
        }
        if (configKey.consumeClick()) {
            client.setScreen(new XrayMenu(null));
        }
    }

    @SubscribeEvent
    public void onHudRender(RenderGuiOverlayEvent ev) {
        GuiGraphics graphics = ev.getGuiGraphics();
        Minecraft mc = Minecraft.getInstance();
        Font render = mc.font;
        LocalPlayer player = mc.player;

        if (!config.getLocationConfig().isEnabled() || player == null || mc.options.renderDebug) {
            return;
        }

        TextHudBuffer buffer = new TextHudBuffer();

        // TODO: add option to render the modes one line/mode
        buffer.newLine();
        if (config.getLocationConfig().isShowMode()) {
            for (AbstractModeConfig cfg : config.getModes()) {
                if (!cfg.isEnabled()) {
                    continue;
                }
                buffer.append(
                        Component.literal("[" + cfg.getModeName() + "] ")
                                .withStyle(s -> s.withColor(cfg.getColor()))
                );
            }
            if (fullBrightEnable) {
                buffer.append(
                        Component.literal("[" + fullbrightMode.getModeName() + "] ")
                                .withStyle(s -> s.withColor(fullbrightMode.getColor()))
                );
            }
        }

        if (config.getLocationConfig().isEnabled()) {
            Component[] format = LocationFormatTool.applyColor(
                    getConfig().getLocationConfig().getCompiledFormat().apply(mc, player, mc.level)
            );

            if (format.length > 0) {
                buffer.append(format[0]);

                for (int i = 1; i < format.length; i++) {
                    buffer.newLine();
                    buffer.append(format[i]);
                }
            }
        }

        buffer.draw(
                graphics,
                mc.getWindow().getGuiScaledWidth(),
                mc.getWindow().getGuiScaledHeight(),
                config.getLocationConfig(),
                render
        );
    }

    /**
     * Save mod configs
     */
    public void saveConfigs() {
        config.save();
        modules();
    }

    private void registerKeyBinding(final RegisterKeyMappingsEvent ev) {
        fullbrightKey = new KeyMapping("x13.mod.fullbright", GLFW.GLFW_KEY_H, "key.categories.xray");
        configKey = new KeyMapping("x13.mod.config", GLFW.GLFW_KEY_N, "key.categories.xray");
        locationEnableKey = new KeyMapping("x13.mod.locationEnable", GLFW.GLFW_KEY_J, "key.categories.xray");

        ev.register(fullbrightKey);
        ev.register(configKey);
        ev.register(locationEnableKey);
    }

    private void setup(final FMLCommonSetupEvent event) {
        log("Initialization");
        fullbrightColor = ColorSupplier.DEFAULT.getColor();
        loadConfigs();

        ModList.get().getModContainerById(MOD_ID).ifPresent(con -> {
            con.registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class,
                    () -> new ConfigScreenHandler.ConfigScreenFactory((mc, parent) -> new XrayMenu(parent)));
        });
    }
}
