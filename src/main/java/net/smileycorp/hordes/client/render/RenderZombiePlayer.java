package net.smileycorp.hordes.client.render;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelZombie;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.layers.LayerBipedArmor;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.util.ResourceLocation;
import net.smileycorp.hordes.common.entities.EntityZombiePlayer;
import net.smileycorp.hordes.config.ClientConfigHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RenderZombiePlayer extends RenderBiped<EntityZombiePlayer> {

    public RenderZombiePlayer(RenderManager rendermanager) {
        super(rendermanager, new ModelZombiePlayer(), 0.5F);
        addLayer(new LayerBipedArmor(this) {
            @Override
            protected void initArmor() {
                modelLeggings = new ModelZombie(0.5F, true);
                modelArmor = new ModelZombie(1.0F, true);
            }
        });
        addLayer(new LayerZombiePlayerCape<>(this));
        addLayer(new LayerZombiePlayerElytra<>(this));
        addLayer(new LayerZombiePlayerOverlay(this));
    }

    @Override
	protected ResourceLocation getEntityTexture(EntityZombiePlayer entity) {
    	UUID uuid = entity.getPlayerUUID();
    	NetworkPlayerInfo playerinfo = Minecraft.getMinecraft().getConnection().getPlayerInfo(uuid);
    	return playerinfo == null ? getTexture(uuid) : playerinfo.getLocationSkin();
    }
    
    private ResourceLocation getTexture(UUID uuid) {
    	List<ResourceLocation> loc = new ArrayList<ResourceLocation>();
    	Minecraft mc = Minecraft.getMinecraft();
    	mc.getSkinManager().loadProfileTextures(new GameProfile(uuid, null), (t, l, p)->loc.add(l), true);
		return loc.isEmpty() ? DefaultPlayerSkin.getDefaultSkin(uuid) : loc.get(0);
	}

	@Override
    public void doRender(EntityZombiePlayer entity, double x, double y, double z, float entityYaw, float partialTicks) {
    	GlStateManager.pushMatrix();
        int[] colour = ClientConfigHandler.getZombiePlayerColour();
    	GlStateManager.color((float)colour[0] * 0.0039215f, (float)colour[1] * 0.0039215f, (float)colour[2] * 0.0039215f);
    	super.doRender(entity, x, y, z, entityYaw, partialTicks);
        GlStateManager.popMatrix();
    }
}