package net.smileycorp.hordes.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.layers.LayerArmorBase;
import net.minecraft.client.renderer.entity.layers.LayerElytra;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.smileycorp.hordes.common.entities.EntityZombiePlayer;

public class LayerZombiePlayerElytra<T extends EntityZombiePlayer> extends LayerElytra {
	
	public LayerZombiePlayerElytra(RenderZombiePlayer renderer) {
		super(renderer);
	}
	
	@Override
	public void doRenderLayer(EntityLivingBase entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
		ItemStack itemstack = entity.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
		if (itemstack.getItem() != Items.ELYTRA |! (entity instanceof EntityZombiePlayer)) return;
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		EntityZombiePlayer zombie = (EntityZombiePlayer) entity;
		NetworkPlayerInfo playerInfo = Minecraft.getMinecraft().getConnection().getPlayerInfo(zombie.getPlayerUUID());
		if (playerInfo.getLocationElytra() != null) playerInfo.getLocationElytra();
		else if (zombie.displayCape() && playerInfo.getLocationCape() != null) playerInfo.getLocationElytra();
		else renderPlayer.bindTexture(TEXTURE_ELYTRA);
		GlStateManager.pushMatrix();
		GlStateManager.translate(0.0F, 0.0F, 0.125F);
		modelElytra.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, entity);
		modelElytra.render(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
		if (itemstack.isItemEnchanted()) LayerArmorBase.renderEnchantedGlint(this.renderPlayer, entity, this.modelElytra, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, scale);
		GlStateManager.disableBlend();
		GlStateManager.popMatrix();
	}
	
	public boolean shouldCombineTextures()
	{
		return false;
	}

}
