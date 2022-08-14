package net.smileycorp.hordes.client.render;

import java.awt.Color;
import java.util.Optional;
import java.util.UUID;

import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type;

import net.minecraft.client.model.ZombieModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Zombie;
import net.smileycorp.atlas.api.client.RenderingUtils;
import net.smileycorp.hordes.common.ModDefinitions;
import net.smileycorp.hordes.common.entities.IZombiePlayer;

public class ZombiePlayerRenderer<T extends Zombie & IZombiePlayer> extends HumanoidMobRenderer<T, ZombiePlayerModel<T>> {

	public static final ModelLayerLocation MAIN_LAYER = new ModelLayerLocation(ModDefinitions.getResource("zombie_player"), "main");

	public ZombiePlayerRenderer(EntityRendererProvider.Context ctx, Color colour) {
		super(ctx, new ZombiePlayerModel<T>(ctx.bakeLayer(MAIN_LAYER)), 0.5F);
		addLayer(new HumanoidArmorLayer<>(this, new ZombieModel<T>(ctx.bakeLayer(ModelLayers.ZOMBIE_INNER_ARMOR)),
				new ZombieModel<T>(ctx.bakeLayer(ModelLayers.ZOMBIE_OUTER_ARMOR))));
		addLayer(new ZombiePlayerCapeLayer<>(this));
		addLayer(new ZombiePlayerElytraLayer<T>(this, ctx.getModelSet()));
	}

	@Override
	public ResourceLocation getTextureLocation(T entity) {
		Optional<UUID> optional = entity.getPlayerUUID();
		return RenderingUtils.getPlayerTexture(optional, Type.SKIN);
	}

	public static LayerDefinition createMainLayer() {
		return LayerDefinition.create(ZombiePlayerModel.createMesh(CubeDeformation.NONE, false), 64, 64);
	}


}