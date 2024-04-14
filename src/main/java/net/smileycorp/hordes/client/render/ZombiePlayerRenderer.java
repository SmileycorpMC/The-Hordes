package net.smileycorp.hordes.client.render;

import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.ZombieModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Zombie;
import net.smileycorp.atlas.api.client.PlayerTextureRenderer;
import net.smileycorp.hordes.common.Constants;
import net.smileycorp.hordes.common.entities.PlayerZombie;

import java.awt.*;
import java.util.Optional;
import java.util.UUID;

public class ZombiePlayerRenderer<T extends Zombie & PlayerZombie> extends HumanoidMobRenderer<T, ZombiePlayerModel<T>> {
	
	public static final ModelLayerLocation DEFAULT = new ModelLayerLocation(Constants.loc("zombie_player"), "default");
	public static final ModelLayerLocation SLIM = new ModelLayerLocation(Constants.loc("zombie_player"), "slim");
	
	protected final ZombiePlayerModel<T> defaultModel;
	protected final ZombiePlayerModel<T> slimModel;
	private final boolean isTall;
	
	public ZombiePlayerRenderer(EntityRendererProvider.Context ctx, Color colour, ResourceLocation overlay, boolean isDrowned, boolean isTall) {
		super(ctx, new ZombiePlayerModel<>(ctx.bakeLayer(DEFAULT), colour, isDrowned), 0.5F);
		addLayer(new HumanoidArmorLayer<>(this, new ZombieModel<>(ctx.bakeLayer(ModelLayers.ZOMBIE_INNER_ARMOR)),
				new ZombieModel<>(ctx.bakeLayer(ModelLayers.ZOMBIE_OUTER_ARMOR)), ctx.m_266367_()));
		addLayer(new ZombiePlayerCapeLayer<>(this));
		addLayer(new ZombiePlayerElytraLayer<>(this, ctx.getModelSet()));
		addLayer(new ZombiePlayerOverlayLayer(this, new ZombiePlayerModel<>(ctx.bakeLayer(DEFAULT)),
				new ZombiePlayerModel<>(ctx.bakeLayer(SLIM)), overlay));
		defaultModel = model;
		slimModel = new ZombiePlayerModel<>(ctx.bakeLayer(SLIM), colour, isDrowned);
		this.isTall = isTall;
	}
	
	@Override
	public ResourceLocation getTextureLocation(T entity) {
		Optional<UUID> optional = entity.getPlayerUUID();
		return PlayerTextureRenderer.getTexture(optional, Type.SKIN);
	}
	
	@Override
	protected void scale(T entity, PoseStack poseStack, float p_114909_) {
		if (isTall) poseStack.scale(1.0625F, 1.0625F, 1.0625F);
		super.scale(entity, poseStack, p_114909_);
	}
	
	@Override
	public void render(T entity, float yaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int packedLightIn) {
		Optional<UUID> optional = entity.getPlayerUUID();
		boolean isSlim = "slim".equals(PlayerTextureRenderer.getSkinType(optional));
		if (isSlim && model != slimModel) model = slimModel;
		else if (!isSlim && model != defaultModel) model = defaultModel;
		super.render(entity, yaw, partialTicks, poseStack, bufferSource, packedLightIn);
	}
	
	public static LayerDefinition createLayer(boolean slim) {
		return LayerDefinition.create(ZombiePlayerModel.createMesh(CubeDeformation.NONE, slim), 64, 64);
	}
	
}