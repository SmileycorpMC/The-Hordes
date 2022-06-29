package net.smileycorp.hordes.client;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.mojang.authlib.GameProfile;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.PathfinderMob;
import net.smileycorp.hordes.common.entities.IZombiePlayer;

public class ZombiePlayerRenderer<T extends PathfinderMob & IZombiePlayer> extends HumanoidMobRenderer<T, ZombiePlayerModel<T>> {

	public ZombiePlayerRenderer(EntityRendererProvider.Context ctx, Color colour) {
		super(ctx, new ZombiePlayerModel<T>(colour), 0.5F);
		addLayer(new HumanoidArmorLayer<>(this, new ZombiePlayerModel<T>(), new ZombiePlayerModel<T>()));
		addLayer(new ZombieCapeLayer<T>(this, new ZombiePlayerModel<T>()));
	}

	@Override
	public ResourceLocation getTextureLocation(T entity) {
		UUID uuid = entity.getPlayerUUID();
		PlayerInfo playerinfo = Minecraft.getInstance().getConnection().getPlayerInfo(uuid);
		return playerinfo == null ? getTexture(uuid) : playerinfo.getSkinLocation();
	}

	private ResourceLocation getTexture(UUID uuid) {
		List<ResourceLocation> loc = new ArrayList<ResourceLocation>();
		Minecraft mc = Minecraft.getInstance();
		mc.getSkinManager().getInsecureSkinInformation(new GameProfile(uuid, null));
		return loc.isEmpty() ? DefaultPlayerSkin.getDefaultSkin(uuid) : loc.get(0);
	}

}