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
import net.smileycorp.atlas.api.client.PlayerTextureRenderer;
import net.smileycorp.hordes.common.entities.PlayerZombie;

public class ZombiePlayerElytraLayer<T extends Zombie & PlayerZombie> extends ElytraLayer<T, ZombiePlayerModel<T>> {

	public ZombiePlayerElytraLayer(RenderLayerParent<T, ZombiePlayerModel<T>> renderer, EntityModelSet models) {
		super(renderer, models);
	}

	@Override
	public void render(PoseStack poseStack, MultiBufferSource buffers, int packedLight, T entity,  float limbSwing, float limbSwingAmount, float pt, float age, float headYaw, float headPitch) {
		ItemStack itemstack = entity.getItemBySlot(EquipmentSlot.CHEST);
		if (shouldRender(itemstack, entity)) {
			ResourceLocation loc = getElytraTexture(itemstack, entity);
			if (entity.displayCape()) {
				ResourceLocation texture = PlayerTextureRenderer.getTexture(entity.getPlayerUUID(), Type.ELYTRA);
				if (texture != null) loc = texture;
			}
			poseStack.pushPose();
			poseStack.translate(0.0D, 0.0D, 0.125D);
			getParentModel().copyPropertiesTo(elytraModel);
			elytraModel.setupAnim(entity, limbSwing, limbSwingAmount, age, headYaw, headPitch);
			VertexConsumer vertexconsumer = ItemRenderer.getArmorFoilBuffer(buffers, RenderType.armorCutoutNoCull(loc), itemstack.hasFoil());
			elytraModel.renderToBuffer(poseStack, vertexconsumer, packedLight, OverlayTexture.NO_OVERLAY);
			poseStack.popPose();
		}
	}

}
