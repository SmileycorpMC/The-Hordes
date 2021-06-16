package net.smileycorp.hordes.client;

import java.util.UUID;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelZombie;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.layers.LayerBipedArmor;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.util.ResourceLocation;
import net.smileycorp.hordes.infection.entities.EntityZombiePlayer;

public class RenderZombiePlayer extends RenderBiped<EntityZombiePlayer> {

    public RenderZombiePlayer(RenderManager rendermanager) {
        super(rendermanager, new ModelZombiePlayer(), 0.5F);
        
        LayerBipedArmor layerbipedarmor = new LayerBipedArmor(this)
        
        {
            @Override
			protected void initArmor()
            {
                this.modelLeggings = new ModelZombie(0.5F, true);
                this.modelArmor = new ModelZombie(1.0F, true);
            }
        };
        this.addLayer(layerbipedarmor);
    }

    @Override
	protected ResourceLocation getEntityTexture(EntityZombiePlayer entity) {
    	UUID uuid = entity.getPlayerUUID();
    	NetworkPlayerInfo playerinfo = Minecraft.getMinecraft().getConnection().getPlayerInfo(uuid);
    	return playerinfo == null ? DefaultPlayerSkin.getDefaultSkin(uuid) : playerinfo.getLocationSkin();
    }
    
    @Override
    public void doRender(EntityZombiePlayer entity, double x, double y, double z, float entityYaw, float partialTicks) {
    	GlStateManager.pushMatrix();
    	GlStateManager.color(0.4745f, 0.6117f, 0.3961f);
    	super.doRender(entity, x, y, z, entityYaw, partialTicks);
        GlStateManager.popMatrix();
    }
}