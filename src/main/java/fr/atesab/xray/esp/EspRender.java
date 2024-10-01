package fr.atesab.xray.esp;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import fr.atesab.xray.XrayMain;
import fr.atesab.xray.config.ESPConfig;
import fr.atesab.xray.utils.GuiUtils;
import fr.atesab.xray.utils.RenderUtils;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;


public class EspRender {
    public static void handler(RenderLevelStageEvent ev) {
        if (ev.getStage() != RenderLevelStageEvent.Stage.AFTER_WEATHER) {
            return;
        }
//        XrayMain.log.info("Rendering ESP");
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        LocalPlayer player = minecraft.player;
        if (level == null || player == null) {
            return;
        }
        PoseStack stack = ev.getPoseStack();
        float delta = ev.getPartialTick();
        Camera mainCamera = ev.getCamera();
//        Camera mainCamera = minecraft.gameRenderer.getMainCamera();
        var config = XrayMain.getMod().getConfig();

        if (config.getEspConfigs().stream().noneMatch(ESPConfig::isEnabled)) {
            return;
        }

        MultiBufferSource.BufferSource multiBuffer = minecraft.renderBuffers().bufferSource();

//        RenderSystem.setShader(GameRenderer::getPositionColorShader);
//        RenderSystem.setShader(GameRenderer::getPositionShader);
//        RenderSystem.setShader(GameRenderer::getRendertypeLinesShader);
//        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
//		 RenderSystem.depthMask(false);
        RenderSystem.disableDepthTest();
//        RenderSystem.enableBlend();

        RenderType espRenderType = EspRenderType.makeType(config.getEspLineWidth());

//        RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
//        GL11.glEnable(GL11.GL_LINE);
//        GL11.glLineWidth(config.getEspLineWidth());
//        RenderSystem.depthMask(false);
//        RenderSystem.depthFunc(GL11.GL_NEVER);

//        Tesselator tessellator = RenderSystem.renderThreadTesselator();
//        BufferBuilder buffer = tessellator.getBuilder();
//        buffer.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);

        stack.pushPose();

//        RenderSystem.applyModelViewMatrix();
        stack.setIdentity();
        Vec3 reversedPosition = mainCamera.getPosition().reverse();
        stack.translate(reversedPosition.x, reversedPosition.y, reversedPosition.z);

        Vector3f look = mainCamera.getLookVector();
        float px = (float) (player.xOld + (player.getX() - player.xOld) * delta) + look.x();
        float py = (float) (player.yOld + (player.getY() - player.yOld) * delta) + player.getEyeHeight() + look.y();
        float pz = (float) (player.zOld + (player.getZ() - player.zOld) * delta) + look.z();

        int maxDistanceSquared = (config.getMaxTracerRange() * config.getMaxTracerRange());
        int distance = minecraft.options.getEffectiveRenderDistance();
        ChunkPos chunkPos = player.chunkPosition();
        int chunkX = chunkPos.x;
        int chunkZ = chunkPos.z;

        if (config.getEspConfigs().stream().anyMatch(ESPConfig::hasBlockEsp)) {
            for (int i = chunkX - distance; i <= chunkX + distance; i++) {
                for (int j = chunkZ - distance; j <= chunkZ + distance; j++) {
                    int ccx = i << 4 + 8;
                    int ccz = j << 4 + 8;

                    int squaredDistanceToChunk = (int) ((px - ccx) * (px - ccx) + (pz - ccz) * (pz - ccz));

                    // sqrt(2) ~= 3 / 2 "math"
                    if (squaredDistanceToChunk + 8 * 3 / 2 > maxDistanceSquared) {
                        // ignore this chunk, too far
                        continue;
                    }

                    ChunkAccess chunk = level.getChunk(i, j, ChunkStatus.FULL, false);
                    if (chunk != null) {
                        chunk.getBlockEntitiesPos().forEach(((blockPos) -> {
                            if ((config.getMaxTracerRange() != 0 && blockPos.distSqr(player.blockPosition()) > maxDistanceSquared)) {
                                return;
                            }

                            BlockEntity blockEntity = chunk.getBlockEntity(blockPos);

                            if (blockEntity == null) {
                                return;
                            }

                            BlockEntityType<?> type = blockEntity.getType();

                            config.getEspConfigs().stream().filter(esp -> esp.shouldTag(type)).forEach(esp -> {
                                GuiUtils.RGBResult c = GuiUtils.rgbaFromRGBA(esp.getColor());
                                float r = c.red() / 255F;
                                float g = c.green() / 255F;
                                float b = c.blue() / 255F;
                                float a = c.alpha() / 255F;

                                AABB aabb = new AABB(blockPos);
                                renderEsp(stack, multiBuffer, espRenderType, px, py, pz, esp, r, g, b, a, aabb);
                            });
                        }));
                    }
                }
            }
        }

        level.entitiesForRendering().forEach(e -> {

            if ((config.getMaxTracerRange() != 0 && e.distanceToSqr(player) > maxDistanceSquared) || player == e) {
                return;
            }

            EntityType<?> type = e.getType();

            boolean damage = !config.isDamageIndicatorDisabled() && e instanceof LivingEntity le && le.getLastDamageSource() != null;

            config.getEspConfigs().stream().filter(esp -> esp.shouldTag(type)).forEach(esp -> {
                double x = e.xOld + (e.getX() - e.xOld) * delta;
                double y = e.yOld + (e.getY() - e.yOld) * delta;
                double z = e.zOld + (e.getZ() - e.zOld) * delta;

                float r, g, b, a;

                if (damage) {
                    r = 1;
                    g = 0;
                    b = 0;
                    a = 1;
                } else {
                    GuiUtils.RGBResult c = GuiUtils.rgbaFromRGBA(esp.getColor());
                    r = c.red() / 255F;
                    g = c.green() / 255F;
                    b = c.blue() / 255F;
                    a = c.alpha() / 255F;
                }

                AABB aabb = type.getAABB(x, y, z);
                renderEsp(stack, multiBuffer, espRenderType, px, py, pz, esp, r, g, b, a, aabb);
            });
        });


//        tessellator.end();
        stack.popPose();
        multiBuffer.endBatch();
        RenderSystem.enableDepthTest();
//        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
//        RenderSystem.applyModelViewMatrix();
//        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

//        RenderSystem.depthMask(true);
//        RenderSystem.lineWidth(1.0F);
//        RenderSystem.depthFunc(GL11.GL_LEQUAL);
//        GL11.glDisable(GL11.GL_LINE_SMOOTH);
    }

    private static void renderEsp(PoseStack stack, MultiBufferSource.BufferSource multiBuffer, RenderType espRenderType, float px, float py, float pz, ESPConfig esp, float r, float g, float b, float a, AABB aabb) {
        VertexConsumer buffer = multiBuffer.getBuffer(espRenderType);

        LevelRenderer.renderLineBox(stack, buffer, aabb, r, g, b, a);

        if (esp.hasTracer()) {
            Vector3f center = aabb.getCenter().toVector3f();
            RenderUtils.renderSingleLine(stack, buffer, px, py, pz, center.x, center.y, center.z, r, g, b, a);
        }
    }
}
