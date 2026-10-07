package com.experiencestorage.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

/** Draws the vanilla experience-orb billboard for any entity, with a custom icon index and scale. */
public abstract class XpOrbLikeRenderer<T extends Entity> extends EntityRenderer<T> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("textures/entity/experience_orb.png");
    private static final RenderType RENDER_TYPE = RenderType.itemEntityTranslucentCull(TEXTURE);

    protected XpOrbLikeRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0F;
    }

    /** Icon 0-10 on the 4x4 vanilla orb sheet. */
    protected abstract int getIcon(T entity);

    protected float getScale(T entity, float partialTick) {
        return 0.3F;
    }

    @Override
    protected int getBlockLightLevel(T entity, BlockPos pos) {
        return Mth.clamp(super.getBlockLightLevel(entity, pos) + 7, 0, 15);
    }

    @Override
    public void render(T entity, float yaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer,
                       int packedLight) {
        poseStack.pushPose();
        int icon = getIcon(entity);
        float u0 = (float) (icon % 4 * 16) / 64.0F;
        float u1 = (float) (icon % 4 * 16 + 16) / 64.0F;
        float v0 = (float) (icon / 4 * 16) / 64.0F;
        float v1 = (float) (icon / 4 * 16 + 16) / 64.0F;
        float time = ((float) entity.tickCount + partialTick) / 2.0F;
        int r = (int) ((Mth.sin(time) + 1.0F) * 0.5F * 255.0F);
        int g = 255;
        int b = (int) ((Mth.sin(time + 4.1887903F) + 1.0F) * 0.1F * 255.0F);

        poseStack.translate(0.0F, 0.1F, 0.0F);
        poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        float scale = getScale(entity, partialTick);
        poseStack.scale(scale, scale, scale);

        VertexConsumer consumer = buffer.getBuffer(RENDER_TYPE);
        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix = pose.pose();
        Matrix3f normal = pose.normal();
        vertex(consumer, matrix, normal, -0.5F, -0.25F, r, g, b, u0, v1, packedLight);
        vertex(consumer, matrix, normal, 0.5F, -0.25F, r, g, b, u1, v1, packedLight);
        vertex(consumer, matrix, normal, 0.5F, 0.75F, r, g, b, u1, v0, packedLight);
        vertex(consumer, matrix, normal, -0.5F, 0.75F, r, g, b, u0, v0, packedLight);
        poseStack.popPose();
        super.render(entity, yaw, partialTick, poseStack, buffer, packedLight);
    }

    private static void vertex(VertexConsumer consumer, Matrix4f matrix, Matrix3f normal, float x, float y,
                               int r, int g, int b, float u, float v, int light) {
        consumer.vertex(matrix, x, y, 0.0F).color(r, g, b, 128).uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(normal, 0.0F, 1.0F, 0.0F).endVertex();
    }

    @Override
    public ResourceLocation getTextureLocation(T entity) {
        return TEXTURE;
    }
}
