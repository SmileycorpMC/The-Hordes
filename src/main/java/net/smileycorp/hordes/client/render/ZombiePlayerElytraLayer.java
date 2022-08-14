package net.smileycorp.hordes.client.render;

import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.ElytraLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.smileycorp.atlas.api.client.RenderingUtils;
import net.smileycorp.hordes.common.entities.IZombiePlayer;

public class ZombiePlayerElytraLayer<T extends Zombie & IZombiePlayer> extends ElytraLayer<T, ZombiePlayerModel<T>> {

	public ZombiePlayerElytraLayer(RenderLayerParent<T, ZombiePlayerModel<T>> renderer, EntityModelSet models) {
		super(renderer, models);
	}

	@Override
	public void render(PoseStack poseStack, MultiBufferSource buffers, int size, T entity, float p_225628_5_, float p_225628_6_, float p_225628_7_, float p_225628_8_, float p_225628_9_, float p_225628_10_) {
		ItemStack itemstack = entity.getItemBySlot(EquipmentSlot.CHEST);
		if (shouldRender(itemstack, entity)) {
			ResourceLocation loc = getElytraTexture(itemstack, entity);
			if (entity.displayCape()) {
				loc = RenderingUtils.getPlayerTexture(entity.getPlayerUUID(), Type.ELYTRA);
			}

			poseStack.pushPose();
			poseStack.translate(0.0D, 0.0D, 0.125D);
			getParentModel().copyPropertiesTo(elytraModel);
			elytraModel.setupAnim(entity, p_225628_5_, p_225628_6_, p_225628_8_, p_225628_9_, p_225628_10_);
			VertexConsumer ivertexbuilder = ItemRenderer.getArmorFoilBuffer(buffers, RenderType.armorCutoutNoCull(loc), false, itemstack.hasFoil());
			elytraModel.renderToBuffer(poseStack, ivertexbuilder, size, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
			poseStack.popPose();
		}
	}

}
