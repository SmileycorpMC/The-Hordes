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
		poseStack.translate(0, 0, 0.125);
		double d0 = Mth.lerp(pt, entity.getXCloakO(), entity.getXCloak()) - Mth.lerp(pt, entity.xo, entity.getX());
		double d2 = Mth.lerp(pt, entity.getZCloakO(), entity.getZCloak()) - Mth.lerp(pt, entity.zo, entity.getZ());
		float f = entity.yBodyRotO + (entity.yBodyRot - entity.yBodyRotO);
		double d3 = Mth.sin(f * ((float)Math.PI / 180f));
		double d4 = -Mth.cos(f * ((float)Math.PI / 180f));
		float f1 = Mth.clamp((float)(Mth.lerp(pt, entity.getYCloakO(), entity.getYCloak()) - Mth.lerp(pt, entity.yo, entity.getY())) * 10f, -6, 32)
				+ Mth.sin(Mth.lerp(pt, entity.walkDistO, entity.walkDist) * 6f) * 32f * pt;
		float f3 = Mth.clamp((float)(d0 * d4 - d2 * d3) * 100f, -20, 20);
		if (entity.isCrouching()) f1 += 25;
		poseStack.mulPose(Axis.XP.rotationDegrees(6 + Mth.clamp((float)(d0 * d3 + d2 * d4) * 100f, 0, 150) / 2.f + f1));
		poseStack.mulPose(Axis.ZP.rotationDegrees(f3 / 2f));
		poseStack.mulPose(Axis.YP.rotationDegrees(180 - f3 / 2f));
		VertexConsumer consumer = buffer.getBuffer(RenderType.entitySolid(loc));
		getParentModel().renderCloak(poseStack, consumer, packedLight, OverlayTexture.NO_OVERLAY);
		poseStack.popPose();
	}

}
