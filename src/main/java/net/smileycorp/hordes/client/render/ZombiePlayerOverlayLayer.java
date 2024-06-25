package net.smileycorp.hordes.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Zombie;
import net.smileycorp.atlas.api.client.PlayerTextureRenderer;
import net.smileycorp.hordes.common.entities.PlayerZombie;

import java.util.Optional;
import java.util.UUID;

public class ZombiePlayerOverlayLayer<T extends Zombie & PlayerZombie> extends RenderLayer<T, ZombiePlayerModel<T>> {

    protected final ZombiePlayerModel<T> defaultModel;
    protected final ZombiePlayerModel<T> slimModel;

    protected final ResourceLocation texture;

    public ZombiePlayerOverlayLayer(RenderLayerParent<T, ZombiePlayerModel<T>> parent, ZombiePlayerModel<T> defaultModel,
                                    ZombiePlayerModel<T> slimModel, ResourceLocation texture) {
        super(parent);
        this.defaultModel = defaultModel;
        this.slimModel = slimModel;
        this.texture = texture;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffers, int packedLight, T entity,  float limbSwing, float limbSwingAmount, float pt, float age, float headYaw, float headPitch) {
        Optional<UUID> optional = entity.getPlayerUUID();
        ZombiePlayerModel<T> model = "slim".equals(PlayerTextureRenderer.getSkinType(optional)) ? slimModel : defaultModel;
        getParentModel().copyPropertiesTo(model);
        RenderType rendertype = model.renderType(texture);
        VertexConsumer vertexconsumer = buffers.getBuffer(rendertype);
        poseStack.pushPose();
        poseStack.scale(1.01f, 1.01f, 1.01f);
        model.renderToBuffer(poseStack, vertexconsumer, packedLight, OverlayTexture.NO_OVERLAY, -1);
        poseStack.popPose();
    }
    
}
