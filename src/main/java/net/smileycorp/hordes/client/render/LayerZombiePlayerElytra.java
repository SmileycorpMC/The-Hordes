package net.smileycorp.hordes.client.render;

import com.google.common.base.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.layers.LayerArmorBase;
import net.minecraft.client.renderer.entity.layers.LayerElytra;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.smileycorp.hordes.common.entities.EntityZombiePlayer;
import net.smileycorp.hordes.common.entities.PlayerZombie;

import java.util.UUID;

public class LayerZombiePlayerElytra<T extends EntityMob & PlayerZombie<T>> extends LayerElytra {
	
	public LayerZombiePlayerElytra(RenderZombiePlayer renderer) {
		super(renderer);
	}
	
	@Override
	public void doRenderLayer(EntityLivingBase entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
		ItemStack itemstack = entity.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
		if (itemstack.getItem() != Items.ELYTRA |! (entity instanceof PlayerZombie)) return;
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		EntityZombiePlayer zombie = (EntityZombiePlayer) entity;
		Optional<UUID> optional = zombie.getPlayerUUID();
		if (!optional.isPresent()) return;
		NetworkPlayerInfo playerInfo = Minecraft.getMinecraft().getConnection().getPlayerInfo(optional.get());
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
