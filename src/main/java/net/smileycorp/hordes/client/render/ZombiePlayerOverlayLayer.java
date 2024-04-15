package net.smileycorp.hordes.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.monster.ZombieEntity;
import net.minecraft.util.ResourceLocation;
import net.smileycorp.hordes.common.entities.PlayerZombie;

public class ZombiePlayerOverlayLayer<T extends ZombieEntity & PlayerZombie> extends LayerRenderer<T, ZombiePlayerModel<T>> {
    protected final ZombiePlayerModel<T> model;
    protected final ResourceLocation texture;

    public ZombiePlayerOverlayLayer(IEntityRenderer<T, ZombiePlayerModel<T>> parent, ZombiePlayerModel<T> model, ResourceLocation texture) {
        super(parent);
        this.model = model;
        this.texture = texture;
    }

    @Override
    public void render(MatrixStack matrixStack, IRenderTypeBuffer buffer, int packedLight, T entity, float p_225628_5_, float p_225628_6_, float p_225628_7_, float p_225628_8_, float p_225628_9_, float p_225628_10_) {
        if (!model.fixedArms) model.fixArms(entity);
        getParentModel().copyPropertiesTo(model);
        RenderType rendertype = model.renderType(texture);
        IVertexBuilder vertexconsumer = buffer.getBuffer(rendertype);
        matrixStack.pushPose();
        matrixStack.scale(1.01f, 1.01f, 1.01f);
        model.renderToBuffer(matrixStack, vertexconsumer, packedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
        matrixStack.popPose();
    }
    
}
