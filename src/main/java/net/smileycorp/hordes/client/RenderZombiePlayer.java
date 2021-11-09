package net.smileycorp.hordes.client;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import net.minecraft.client.Minecraft;
import net.minecraft.client.network.play.NetworkPlayerInfo;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.BipedRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.layers.BipedArmorLayer;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.entity.MobEntity;
import net.minecraft.util.ResourceLocation;
import net.smileycorp.hordes.common.entities.IZombiePlayer;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;

public class RenderZombiePlayer<T extends MobEntity & IZombiePlayer> extends BipedRenderer<T, ZombiePlayerModel<T>> {

    public RenderZombiePlayer(EntityRendererManager manager) {
        super(manager, new ZombiePlayerModel<T>(), 0.5F);
        this.addLayer(new BipedArmorLayer<>(this, new ZombiePlayerModel<T>(), new ZombiePlayerModel<T>()));
    }

    @Override
    public ResourceLocation getTextureLocation(T entity) {
    	UUID uuid = entity.getPlayerUUID();
    	NetworkPlayerInfo playerinfo = Minecraft.getInstance().getConnection().getPlayerInfo(uuid);
    	return playerinfo == null ? getTexture(uuid) : playerinfo.getSkinLocation();
    }

    private ResourceLocation getTexture(UUID uuid) {
    	List<ResourceLocation> loc = new ArrayList<ResourceLocation>();
    	Minecraft mc = Minecraft.getInstance();
    	mc.getSkinManager().getInsecureSkinInformation(new GameProfile(uuid, null));
		return loc.isEmpty() ? DefaultPlayerSkin.getDefaultSkin(uuid) : loc.get(0);
	}

	@Override
	public void render(T entity, float yaw, float partialTicks, MatrixStack stack, IRenderTypeBuffer buffers, int light) {
    	stack.pushPose();
    	Color colour = entity.getColour();
    	GlStateManager._blendColor(colour.getRed()/255, colour.getGreen()/255, colour.getBlue()/255, colour.getAlpha()/255);
    	super.render(entity, yaw, partialTicks, stack, buffers, light);
        stack.popPose();
    }
}