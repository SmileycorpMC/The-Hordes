package net.smileycorp.hordes.client.render;


import com.google.common.base.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemElytra;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.smileycorp.atlas.api.util.MathUtils;
import net.smileycorp.hordes.common.entities.PlayerZombie;

import java.util.UUID;

public class LayerZombiePlayerCape<T extends EntityMob & PlayerZombie<T>> implements LayerRenderer<T> {
	
	private final RenderZombiePlayer<T> renderer;
	
	public LayerZombiePlayerCape(RenderZombiePlayer<T> renderer) {
		this.renderer = renderer;
	}

	@Override
	public void doRenderLayer(T entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
		if (!entity.displayCape()) return;
		ItemStack stack = entity.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
		if (stack.getItem() instanceof ItemElytra) return;
		Optional<UUID> optional = entity.getPlayerUUID();
		if (!optional.isPresent()) return;
		NetworkPlayerInfo playerInfo = Minecraft.getMinecraft().getConnection().getPlayerInfo(optional.get());
		if (playerInfo == null) return;
		ResourceLocation loc = playerInfo.getLocationCape();
		if (loc == null) return;
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		renderer.bindTexture(loc);
		GlStateManager.pushMatrix();
		GlStateManager.translate(0.0F, 0.0F, 0.125F);
		double d0 = MathUtils.lerp(partialTicks, entity.getXCloakO(), entity.getXCloak()) - MathUtils.lerp(partialTicks, entity.lastTickPosX, entity.posX);
		double d1 = MathUtils.lerp(partialTicks, entity.getYCloakO(), entity.getYCloak()) - MathUtils.lerp(partialTicks, entity.lastTickPosY, entity.posY);
		double d2 = MathUtils.lerp(partialTicks, entity.getZCloakO(), entity.getZCloak()) - MathUtils.lerp(partialTicks, entity.lastTickPosZ, entity.posZ);
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
	
	@Override
	public boolean shouldCombineTextures()
	{
		return false;
	}

}
