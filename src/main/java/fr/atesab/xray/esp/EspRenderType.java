package fr.atesab.xray.esp;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraftforge.client.event.RenderLevelStageEvent;

import java.util.OptionalDouble;

public class EspRenderType extends RenderType {
//    public static final RenderType INSTANCE = RenderType.create(
//            "xray_esp_lines",
//            DefaultVertexFormat.POSITION_COLOR_NORMAL,
////            DefaultVertexFormat.POSITION_COLOR,
//            VertexFormat.Mode.LINES,
//            256, false, false,
//            CompositeState.builder()
//                    .setLineState(new LineStateShard(OptionalDouble.of(config.getEspLineWidth())))
////                    .setTransparencyState(TransparencyStateShard.GLINT_TRANSPARENCY)
//                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
//                    .setTextureState(NO_TEXTURE)
//                    .setDepthTestState(NO_DEPTH_TEST)
//                    .setCullState(NO_CULL)
//                    .setLightmapState(NO_LIGHTMAP)
//                    .setWriteMaskState(COLOR_DEPTH_WRITE)
//                    .setShaderState(RENDERTYPE_LINES_SHADER)
//                    .createCompositeState(false)
//    );

    public static RenderType makeType(double lineWidth) {
        return RenderType.create(
                "xray_esp_lines",
                DefaultVertexFormat.POSITION_COLOR_NORMAL,
                VertexFormat.Mode.LINES,
                256, false, false,
                CompositeState.builder()
                        .setLineState(new LineStateShard(OptionalDouble.of(lineWidth)))
                        .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                        .setTextureState(NO_TEXTURE)
                        .setDepthTestState(NO_DEPTH_TEST)
                        .setCullState(NO_CULL)
                        .setLightmapState(NO_LIGHTMAP)
                        .setWriteMaskState(COLOR_WRITE)
                        .setShaderState(RENDERTYPE_LINES_SHADER)
                        .createCompositeState(false)
        );
    }

    public EspRenderType(String p0, VertexFormat p1, VertexFormat.Mode p3, int p4, boolean p5, boolean p6, Runnable p7, Runnable p8) {
        super(p0, p1, p3, p4, p5, p6, p7, p8);
        throw new IllegalStateException("Cannot instantiate EspRenderType");
    }
}
