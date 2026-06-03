package net.smileycorp.hordes.client.render;

import com.google.common.base.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.util.ResourceLocation;
import net.smileycorp.hordes.common.entities.PlayerZombie;

import java.util.UUID;

public class LayerZombiePlayerOverlay<T extends EntityMob & PlayerZombie<T>> implements LayerRenderer<T> {
    
    private final RenderZombiePlayer<T> renderer;
    private final ResourceLocation texture;

    public LayerZombiePlayerOverlay(RenderZombiePlayer<T> renderer, ResourceLocation texture) {
       this.renderer = renderer;
       this.texture = texture;
    }

    @Override
    public void doRenderLayer(T entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        ModelBase model = renderer.getMainModel();
        Optional<UUID> optional = entity.getPlayerUUID();
        if (!optional.isPresent()) return;
        NetworkPlayerInfo playerInfo = Minecraft.getMinecraft().getConnection().getPlayerInfo(optional.get());
        if (playerInfo != null && "slim".equals(playerInfo.getSkinType())) model = renderer.slimModel;
        model.setModelAttributes(renderer.getMainModel());
        model.setLivingAnimations(entity, limbSwing, limbSwingAmount, partialTicks);
        model.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, entity);
        renderer.bindTexture(texture);
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
