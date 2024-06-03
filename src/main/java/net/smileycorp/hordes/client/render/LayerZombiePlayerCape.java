package net.smileycorp.hordes.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.smileycorp.hordes.common.entities.EntityZombiePlayer;

public class LayerZombiePlayerCape<T extends EntityZombiePlayer> implements LayerRenderer<T> {
	
	private final RenderZombiePlayer renderer;
	
	public LayerZombiePlayerCape(RenderZombiePlayer renderer) {
		this.renderer = renderer;
	}

	@Override
	public void doRenderLayer(T entity,  float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
		NetworkPlayerInfo playerInfo = Minecraft.getMinecraft().getConnection().getPlayerInfo(entity.getPlayerUUID());
		if (playerInfo != null && !entity.isInvisible() && entity.displayCape() && playerInfo.getLocationCape() != null) {
			ItemStack itemstack = entity.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
			if (itemstack.getItem() != Items.ELYTRA) {
				GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
				renderer.bindTexture(playerInfo.getLocationCape());
				GlStateManager.pushMatrix();
				GlStateManager.translate(0.0F, 0.0F, 0.125F);
				double d0 = entity.xCloakO + (entity.xCloak - entity.xCloakO) * (double)partialTicks - (entity.prevPosX + (entity.posX - entity.prevPosX) * (double)partialTicks);
				double d1 = entity.yCloakO + (entity.yCloak- entity.yCloakO) * (double)partialTicks - (entity.prevPosY + (entity.posY - entity.prevPosY) * (double)partialTicks);
				double d2 = entity.zCloakO + (entity.zCloak - entity.zCloakO) * (double)partialTicks - (entity.prevPosZ + (entity.posZ - entity.prevPosZ) * (double)partialTicks);
				float f = entity.prevRenderYawOffset + (entity.renderYawOffset - entity.prevRenderYawOffset) * partialTicks;
				double d3 = MathHelper.sin(f * 0.017453292F);
				double d4 = -MathHelper.cos(f * 0.017453292F);
				float f1 = (float)d1 * 10.0F;
				f1 = MathHelper.clamp(f1, -6.0F, 32.0F);
				float f2 = (float)(d0 * d3 + d2 * d4) * 100.0F;
				float f3 = (float)(d0 * d4 - d2 * d3) * 100.0F;
				if (f2 < 0.0F) f2 = 0.0F;
				f1 = f1 + MathHelper.sin((entity.prevDistanceWalkedModified + (entity.distanceWalkedModified - entity.prevDistanceWalkedModified) * partialTicks) * 6.0F) * 32.0F * netHeadYaw;
				if (entity.isSneaking())f1 += 25.0F;
				GlStateManager.rotate(6.0F + f2 / 2.0F + f1, 1.0F, 0.0F, 0.0F);
				GlStateManager.rotate(f3 / 2.0F, 0.0F, 0.0F, 1.0F);
				GlStateManager.rotate(-f3 / 2.0F, 0.0F, 1.0F, 0.0F);
				GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
				((ModelZombiePlayer)renderer.getMainModel()).renderCape(0.0625F);
				GlStateManager.popMatrix();
			}
		}
	}
	
	@Override
	public boolean shouldCombineTextures()
	{
		return false;
	}

}
