package net.smileycorp.hordes.client;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import net.minecraft.client.Minecraft;
import net.minecraft.client.network.play.NetworkPlayerInfo;
import net.minecraft.client.renderer.entity.BipedRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.layers.BipedArmorLayer;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.entity.MobEntity;
import net.minecraft.util.ResourceLocation;
import net.smileycorp.hordes.common.entities.IZombiePlayer;

import com.mojang.authlib.GameProfile;

public class ZombiePlayerRenderer<T extends MobEntity & IZombiePlayer> extends BipedRenderer<T, ZombiePlayerModel<T>> {

    public ZombiePlayerRenderer(EntityRendererManager manager, Color colour) {
        super(manager, new ZombiePlayerModel<T>(colour), 0.5F);
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

}