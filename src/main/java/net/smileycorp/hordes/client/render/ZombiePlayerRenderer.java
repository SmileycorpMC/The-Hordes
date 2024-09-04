package net.smileycorp.hordes.client.render;

import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.play.NetworkPlayerInfo;
import net.minecraft.client.renderer.entity.BipedRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.layers.BipedArmorLayer;
import net.minecraft.client.renderer.entity.model.ZombieModel;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.entity.monster.ZombieEntity;
import net.minecraft.util.ResourceLocation;
import net.smileycorp.hordes.common.entities.PlayerZombie;

import java.awt.*;
import java.util.List;
import java.util.UUID;

public class ZombiePlayerRenderer<T extends ZombieEntity & PlayerZombie> extends BipedRenderer<T, ZombiePlayerModel<T>> {
	
	private final boolean isTall;

	public ZombiePlayerRenderer(EntityRendererManager manager, Color colour, ResourceLocation overlay, boolean isDrowned, boolean isTall) {
		super(manager, new ZombiePlayerModel<T>(colour, isDrowned), 0.5F);
		addLayer(new BipedArmorLayer<>(this, new ZombieModel<T>(0.5F, true), new ZombieModel<T>(1.0F, true)));
		addLayer(new ZombiePlayerCapeLayer<>(this));
		addLayer(new ZombiePlayerElytraLayer<>(this));
		addLayer(new ZombiePlayerOverlayLayer(this, new ZombiePlayerModel<>(Color.WHITE, isDrowned), overlay));
		this.isTall = isTall;
	}
	
	@Override
	public ResourceLocation getTextureLocation(T entity) {
		UUID uuid = (UUID) entity.getPlayerUUID().get();
		NetworkPlayerInfo playerinfo = Minecraft.getInstance().getConnection().getPlayerInfo(uuid);
		return playerinfo == null ? getTexture(uuid) : playerinfo.getSkinLocation();
	}
	
	public NetworkPlayerInfo getPlayerInfo(T entity) {
		UUID uuid = (UUID) entity.getPlayerUUID().get();
		return Minecraft.getInstance().getConnection().getPlayerInfo(uuid);
	}
	
	private ResourceLocation getTexture(UUID uuid) {
		List<ResourceLocation> loc = Lists.newArrayList();
		Minecraft mc = Minecraft.getInstance();
		mc.getSkinManager().getInsecureSkinInformation(new GameProfile(uuid, null));
		return loc.isEmpty() ? DefaultPlayerSkin.getDefaultSkin(uuid) : loc.get(0);
	}
	
	@Override
	protected void scale(T entity, MatrixStack poseStack, float p_114909_) {
		if (isTall) poseStack.scale(1.0625F, 1.0625F, 1.0625F);
		super.scale(entity, poseStack, p_114909_);
	}
	
}