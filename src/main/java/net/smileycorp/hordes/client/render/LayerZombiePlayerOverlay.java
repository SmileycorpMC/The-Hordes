package net.smileycorp.hordes.client.render;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.util.ResourceLocation;
import net.smileycorp.hordes.common.Constants;
import net.smileycorp.hordes.common.entities.EntityZombiePlayer;

public class LayerZombiePlayerOverlay<T extends EntityZombiePlayer> implements LayerRenderer<T> {
    
    protected static final ResourceLocation TEXTURE = Constants.loc("textures/entity/layer/zombie_player_outer_layer.png");
    
    private final RenderZombiePlayer renderer;
    
    public LayerZombiePlayerOverlay(RenderZombiePlayer renderer) {
       this.renderer = renderer;
    }

    @Override
    public void doRenderLayer(T entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        ModelBase model = renderer.getMainModel();
        model.setModelAttributes(renderer.getMainModel());
        model.setLivingAnimations(entity, limbSwing, limbSwingAmount, partialTicks);
        model.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, entity);
        renderer.bindTexture(TEXTURE);
        GlStateManager.pushMatrix();
        GlStateManager.scale(1.01f, 1.01f, 1.01f);
        model.render(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
        GlStateManager.popMatrix();
    }
    
    @Override
    public boolean shouldCombineTextures() {
        return false;
    }
    
}
