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
	public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, T entity,  float limbSwing, float limbSwingAmount, float pt, float age, float headYaw, float headPitch) {
		if (!entity.displayCape()) return;
		ItemStack itemstack = entity.getItemBySlot(EquipmentSlot.CHEST);
		if (itemstack.getItem() == Items.ELYTRA) return;
		ResourceLocation loc = PlayerTextureRenderer.getTexture(entity.getPlayerUUID(), Type.CAPE);
		if (loc == null) return;
		poseStack.pushPose();
		poseStack.translate(0.0D, 0.0D, 0.125D);
		double d0 = Mth.lerp(pt, entity.getXCloakO(), entity.getXCloak()) - Mth.lerp(pt, entity.xo, entity.getX());
		double d1 = Mth.lerp(pt, entity.getYCloakO(), entity.getYCloak()) - Mth.lerp(pt, entity.yo, entity.getY());
		double d2 = Mth.lerp(pt, entity.getZCloakO(), entity.getZCloak()) - Mth.lerp(pt, entity.zo, entity.getZ());
		float f = entity.yBodyRotO + (entity.yBodyRot - entity.yBodyRotO);
		double d3 = Mth.sin(f * ((float)Math.PI / 180F));
		double d4 = -Mth.cos(f * ((float)Math.PI / 180F));
		float f1 = (float)d1 * 10.0F;
		f1 = Mth.clamp(f1, -6.0F, 32.0F);
		float f2 = (float)(d0 * d3 + d2 * d4) * 100.0F;
		f2 = Mth.clamp(f2, 0.0F, 150.0F);
		float f3 = (float)(d0 * d4 - d2 * d3) * 100.0F;
		f3 = Mth.clamp(f3, -20.0F, 20.0F);
		if (f2 < 0.0F) f2 = 0.0F;
		f1 = f1 + Mth.sin(Mth.lerp(pt, entity.walkDistO, entity.walkDist) * 6.0F) * 32.0F * pt;
		if (entity.isCrouching()) f1 += 25.0F;
		poseStack.mulPose(Axis.XP.rotationDegrees(6.0F + f2 / 2.0F + f1));
		poseStack.mulPose(Axis.ZP.rotationDegrees(f3 / 2.0F));
		poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - f3 / 2.0F));
		VertexConsumer consumer = buffer.getBuffer(RenderType.entitySolid(loc));
		getParentModel().renderCloak(poseStack, consumer, packedLight, OverlayTexture.NO_OVERLAY);
		poseStack.popPose();
	}

}
