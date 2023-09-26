package net.smileycorp.hordes.client.render;

import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.smileycorp.atlas.api.client.PlayerTextureRenderer;
import net.smileycorp.hordes.common.entities.PlayerZombie;

public class ZombiePlayerCapeLayer<T extends Zombie & PlayerZombie> extends RenderLayer<T, ZombiePlayerModel<T>> {

	public ZombiePlayerCapeLayer(ZombiePlayerRenderer<T> renderer) {
		super(renderer);
	}

	@Override
	public void render(PoseStack poseStack, MultiBufferSource buffer, int size, T entity, float p_225628_5_, float p_225628_6_, float p_225628_7_, float p_225628_8_, float p_225628_9_, float p_225628_10_) {
		if (entity.displayCape()) {
			ItemStack itemstack = entity.getItemBySlot(EquipmentSlot.CHEST);
			if (itemstack.getItem() != Items.ELYTRA) {
				ResourceLocation loc = PlayerTextureRenderer.getTexture(entity.getPlayerUUID(), Type.CAPE);
				if (loc != null) {
					poseStack.pushPose();
					poseStack.translate(0.0D, 0.0D, 0.125D);
					double d0 = Mth.lerp((double)p_225628_7_, entity.getXCloakO(), entity.getXCloak()) - Mth.lerp((double)p_225628_7_, entity.xo, entity.getX());
					double d1 = Mth.lerp((double)p_225628_7_, entity.getYCloakO(), entity.getYCloak()) - Mth.lerp((double)p_225628_7_, entity.yo, entity.getY());
					double d2 = Mth.lerp((double)p_225628_7_, entity.getZCloakO(), entity.getZCloak()) - Mth.lerp((double)p_225628_7_, entity.zo, entity.getZ());
					float f = entity.yBodyRotO + (entity.yBodyRot - entity.yBodyRotO);
					double d3 = (double)Mth.sin(f * ((float)Math.PI / 180F));
					double d4 = (double)(-Mth.cos(f * ((float)Math.PI / 180F)));
					float f1 = (float)d1 * 10.0F;
					f1 = Mth.clamp(f1, -6.0F, 32.0F);
					float f2 = (float)(d0 * d3 + d2 * d4) * 100.0F;
					f2 = Mth.clamp(f2, 0.0F, 150.0F);
					float f3 = (float)(d0 * d4 - d2 * d3) * 100.0F;
					f3 = Mth.clamp(f3, -20.0F, 20.0F);
					if (f2 < 0.0F) {
						f2 = 0.0F;
					}

					f1 = f1 + Mth.sin(Mth.lerp(p_225628_7_, entity.walkDistO, entity.walkDist) * 6.0F) * 32.0F * p_225628_7_;
					if (entity.isCrouching()) {
						f1 += 25.0F;
					}
					poseStack.mulPose(Axis.XP.rotationDegrees(6.0F + f2 / 2.0F + f1));
					poseStack.mulPose(Axis.ZP.rotationDegrees(f3 / 2.0F));
					poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - f3 / 2.0F));
					VertexConsumer ivertexbuilder = buffer.getBuffer(RenderType.entitySolid(loc));
					this.getParentModel().renderCloak(poseStack, ivertexbuilder, size, OverlayTexture.NO_OVERLAY);
					poseStack.popPose();
				}
			}
		}
	}

}
