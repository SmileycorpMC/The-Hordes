package net.smileycorp.hordes.client.render;

import com.google.common.base.Optional;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelZombie;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.layers.LayerBipedArmor;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.util.ResourceLocation;
import net.smileycorp.hordes.common.entities.PlayerZombie;
import net.smileycorp.hordes.config.ClientConfigHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RenderZombiePlayer<T extends EntityMob & PlayerZombie<T>> extends RenderBiped<T> {

    protected final ModelBase defaultModel;
    protected final ModelBase slimModel;
    private final boolean isTall;
    private final int[] colour;

    public RenderZombiePlayer(RenderManager rendermanager, int[] colour, ResourceLocation overlay, boolean isDrowned, boolean isTall) {
        super(rendermanager, new ModelZombiePlayer(isDrowned, false), 0.5F);
        addLayer(new LayerBipedArmor(this) {
            @Override
            protected void initArmor() {
                modelLeggings = new ModelZombie(0.5F, true);
                modelArmor = new ModelZombie(1.0F, true);
            }
        });
        if (ClientConfigHandler.zombiePlayerCapes) addLayer(new LayerZombiePlayerCape<>(this));
        addLayer(new LayerZombiePlayerElytra<>(this));
        addLayer(new LayerZombiePlayerOverlay<>(this, overlay));
        defaultModel = mainModel;
        slimModel = new ModelZombiePlayer(isDrowned, true);
        this.isTall = isTall;
        this.colour = colour;
    }

    @Override
	protected ResourceLocation getEntityTexture(T entity) {
    	Optional<UUID> optional = entity.getPlayerUUID();
        if (!optional.isPresent()) return new ResourceLocation("textures/entity/steve.png");
        UUID uuid = optional.get();
    	NetworkPlayerInfo playerinfo = Minecraft.getMinecraft().getConnection().getPlayerInfo(uuid);
    	return playerinfo == null ? getTexture(uuid) : playerinfo.getLocationSkin();
    }

    @Override
    protected void preRenderCallback(T entity, float partialTicks) {
        if (isTall) GlStateManager.scale(1.0625F, 1.0625F, 1.0625F);
        super.preRenderCallback(entity, partialTicks);
    }
    
    private ResourceLocation getTexture(UUID uuid) {
    	List<ResourceLocation> loc = new ArrayList<ResourceLocation>();
    	Minecraft mc = Minecraft.getMinecraft();
    	mc.getSkinManager().loadProfileTextures(new GameProfile(uuid, null), (t, l, p)->loc.add(l), true);
		return loc.isEmpty() ? DefaultPlayerSkin.getDefaultSkin(uuid) : loc.get(0);
	}

	@Override
    public void doRender(T entity, double x, double y, double z, float entityYaw, float partialTicks) {
    	GlStateManager.pushMatrix();
    	GlStateManager.color((float) colour[0] / 255f, (float) colour[1] / 255f, (float) colour[2] / 255f);
    	super.doRender(entity, x, y, z, entityYaw, partialTicks);
        GlStateManager.popMatrix();
    }

}