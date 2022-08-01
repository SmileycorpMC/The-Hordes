package net.smileycorp.hordes.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.network.play.NetworkPlayerInfo;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.layers.ElytraLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.monster.ZombieEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.smileycorp.hordes.common.entities.IZombiePlayer;

public class ZombiePlayerElytraLayer<T extends ZombieEntity & IZombiePlayer> extends ElytraLayer<T, ZombiePlayerModel<T>> {

	public ZombiePlayerElytraLayer(IEntityRenderer<T, ZombiePlayerModel<T>> renderer) {
		super(renderer);
	}

	@Override
	public void render(MatrixStack matrixStack, IRenderTypeBuffer buffers, int size, T entity, float p_225628_5_, float p_225628_6_, float p_225628_7_, float p_225628_8_, float p_225628_9_, float p_225628_10_) {
		ItemStack itemstack = entity.getItemBySlot(EquipmentSlotType.CHEST);
		if (shouldRender(itemstack, entity)) {
			ResourceLocation resourcelocation = getElytraTexture(itemstack, entity);
			NetworkPlayerInfo playerInfo = ((ZombiePlayerRenderer<T>) renderer).getPlayerInfo(entity);
			if (playerInfo != null) {
				if (playerInfo.getElytraLocation() != null) playerInfo.getElytraLocation();
				else if (entity.displayCape() && playerInfo.getCapeLocation() != null) resourcelocation = playerInfo.getCapeLocation();
			}

			matrixStack.pushPose();
			matrixStack.translate(0.0D, 0.0D, 0.125D);
			getParentModel().copyPropertiesTo(elytraModel);
			elytraModel.setupAnim(entity, p_225628_5_, p_225628_6_, p_225628_8_, p_225628_9_, p_225628_10_);
			IVertexBuilder ivertexbuilder = ItemRenderer.getArmorFoilBuffer(buffers, RenderType.armorCutoutNoCull(resourcelocation), false, itemstack.hasFoil());
			elytraModel.renderToBuffer(matrixStack, ivertexbuilder, size, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
			matrixStack.popPose();
		}
	}

}
