package net.smileycorp.hordes.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.network.play.NetworkPlayerInfo;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.monster.ZombieEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3f;
import net.smileycorp.hordes.common.entities.PlayerZombie;

public class ZombiePlayerCapeLayer<T extends ZombieEntity & PlayerZombie> extends LayerRenderer<T, ZombiePlayerModel<T>> {

	public ZombiePlayerCapeLayer(ZombiePlayerRenderer<T> renderer) {
		super(renderer);
	}

	@Override
	public void render(MatrixStack matrixStack, IRenderTypeBuffer buffer, int size, T entity, float p_225628_5_, float p_225628_6_, float p_225628_7_, float p_225628_8_, float p_225628_9_, float p_225628_10_) {
		NetworkPlayerInfo playerInfo = ((ZombiePlayerRenderer<T>) renderer).getPlayerInfo(entity);
		if (playerInfo != null && !entity.isInvisible() && entity.displayCape() && playerInfo.getCapeLocation() != null) {
			ItemStack itemstack = entity.getItemBySlot(EquipmentSlotType.CHEST);
			if (itemstack.getItem() != Items.ELYTRA) {
				matrixStack.pushPose();
				matrixStack.translate(0.0D, 0.0D, 0.125D);
				double d0 = MathHelper.lerp(p_225628_7_, entity.getXCloakO(), entity.getXCloak()) - MathHelper.lerp(p_225628_7_, entity.xo, entity.getX());
				double d1 = MathHelper.lerp(p_225628_7_, entity.getYCloakO(), entity.getYCloak()) - MathHelper.lerp(p_225628_7_, entity.yo, entity.getY());
				double d2 = MathHelper.lerp(p_225628_7_, entity.getZCloakO(), entity.getZCloak()) - MathHelper.lerp(p_225628_7_, entity.zo, entity.getZ());
				float f = entity.yBodyRotO + (entity.yBodyRot - entity.yBodyRotO);
				double d3 = MathHelper.sin(f * ((float)Math.PI / 180F));
				double d4 = -MathHelper.cos(f * ((float)Math.PI / 180F));
				float f1 = (float)d1 * 10.0F;
				f1 = MathHelper.clamp(f1, -6.0F, 32.0F);
				float f2 = (float)(d0 * d3 + d2 * d4) * 100.0F;
				f2 = MathHelper.clamp(f2, 0.0F, 150.0F);
				float f3 = (float)(d0 * d4 - d2 * d3) * 100.0F;
				f3 = MathHelper.clamp(f3, -20.0F, 20.0F);
				if (f2 < 0.0F) f2 = 0.0F;
				f1 = f1 + MathHelper.sin(MathHelper.lerp(p_225628_7_, entity.walkDistO, entity.walkDist) * 6.0F) * 32.0F * p_225628_7_;
				if (entity.isCrouching()) f1 += 25.0F;
				matrixStack.mulPose(Vector3f.XP.rotationDegrees(6.0F + f2 / 2.0F + f1));
				matrixStack.mulPose(Vector3f.ZP.rotationDegrees(f3 / 2.0F));
				matrixStack.mulPose(Vector3f.YP.rotationDegrees(180.0F - f3 / 2.0F));
				IVertexBuilder ivertexbuilder = buffer.getBuffer(RenderType.entitySolid(playerInfo.getCapeLocation()));
				this.getParentModel().renderCloak(matrixStack, ivertexbuilder, size, OverlayTexture.NO_OVERLAY);
				matrixStack.popPose();
			}
		}
	}

}
