package net.smileycorp.hordes.infection.client;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.smileycorp.atlas.api.data.Pair;
import net.smileycorp.atlas.api.util.RecipeUtils;
import net.smileycorp.hordes.common.Constants;
import net.smileycorp.hordes.config.ClientConfigHandler;
import net.smileycorp.hordes.config.data.infection.InfectionConversionEntry;
import net.smileycorp.hordes.config.data.infection.InfectionData;
import net.smileycorp.hordes.infection.HordesInfection;
import net.smileycorp.hordes.infection.jei.JEIPluginInfection;
import net.smileycorp.hordes.infection.network.CureEntityMessage;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class InfectionClientHandler {

	public static final InfectionClientHandler INSTANCE = new InfectionClientHandler();

	@SubscribeEvent
	public void renderOverlay(RenderGameOverlayEvent.Post event){
		if (!ClientConfigHandler.playerInfectionVisuals) return;
		Minecraft mc = Minecraft.getMinecraft();
		EntityPlayer player = mc.player;
		if (player == null || event.getType() != ElementType.VIGNETTE) return;
		if (!player.isPotionActive(HordesInfection.INFECTED)) return;
		int a = player.getActivePotionEffect(HordesInfection.INFECTED).getAmplifier();
		if (a == 0) return;
		Color colour = new Color(0.4745f, 0.6117f, 0.3961f, Math.min(0.005f * a, 0.1f));
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		GL11.glDepthMask(false);
		GL11.glDisable(GL11.GL_ALPHA_TEST);
		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		Gui.drawRect(0, 0, mc.displayWidth, mc.displayHeight, colour.getRGB());
		GL11.glDepthMask(true);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glEnable(GL11.GL_ALPHA_TEST);
	}

	@SubscribeEvent
	public void preRenderEntity(RenderLivingEvent.Pre<EntityLivingBase> event) {
		if (!ClientConfigHandler.playerInfectionVisuals) return;
		Minecraft mc = Minecraft.getMinecraft();
		EntityLivingBase entity = event.getEntity();
		EntityPlayer player = mc.player;
		if (player == null) return;
		if (player.isPotionActive(HordesInfection.INFECTED) && entity != player) {
			int a = player.getActivePotionEffect(HordesInfection.INFECTED).getAmplifier();
			if (a > 2) GlStateManager.color(1, 0.3f, 0.3f);
			else if (a == 2) GlStateManager.color(1, 0.5f, 0.5f);
			else if (a == 1) GlStateManager.color(1, 0.7f, 0.7f);
			if (a > 0) return;
		}
		if (entity.isPotionActive(HordesInfection.INFECTED)) {
			int a = entity.getActivePotionEffect(HordesInfection.INFECTED).getAmplifier();
			GlStateManager.color((float) Math.pow(0.95f, a + 1), 1, (float) Math.pow(0.8f, a + 1));
		}
	}

	@SubscribeEvent
	public void postRenderEntity(RenderLivingEvent.Post<EntityLivingBase> event){
		GlStateManager.color(1, 1, 1);
	}
	
	@SubscribeEvent(priority = EventPriority.HIGH)
	public void tooltip(ItemTooltipEvent event) {
		ItemStack stack = event.getItemStack();
		List<String> tooltips = event.getToolTip();
		if (ClientConfigHandler.cureTooltip && InfectionData.INSTANCE.isCure(stack))
			tooltips.add(new TextComponentTranslation("tooltip.hordes.cure").getFormattedText());
		if (ClientConfigHandler.immunityTooltip) {
			int immunity = InfectionData.INSTANCE.getImmunityLength(stack);
			if (immunity > 0) tooltips.add(TextFormatting.BLUE + I18n.translateToLocal("effect.hordes.immunity") + " (" +
						Potion.getPotionDurationString(new PotionEffect(HordesInfection.IMMUNITY, immunity * 20), 1) + ")");
		}
	}

	public void onInfect(boolean prevented) {
		SoundEvent event = (prevented && ClientConfigHandler.infectionProtectSound) ? Constants.IMMUNE_SOUND :
				(!prevented && ClientConfigHandler.playerInfectSound) ? Constants.INFECT_SOUND : null;
		EntityPlayerSP player = Minecraft.getMinecraft().player;
		player.world.playSound(player, player.getPosition(), event, SoundCategory.PLAYERS, 0.75f, player.getRNG().nextFloat());
	}

	public void processCureEntity(CureEntityMessage message) {
		Minecraft mc = Minecraft.getMinecraft();
		World world = mc.world;
		Entity entity = message.getEntity(world);
		world.playSound(entity.posX, entity.posY, entity.posZ, SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, entity.getSoundCategory(), 1f, 1f, true);
		Random rand = world.rand;
		for (int i = 0; i < 10; ++i) world.spawnParticle(EnumParticleTypes.VILLAGER_HAPPY, entity.posX + (rand.nextDouble() - 0.5D) * entity.width * 1.5,
					entity.posY + rand.nextDouble() * entity.height, entity.posZ + (rand.nextDouble() - 0.5D) * entity.width * 1.5, 0.0D, 0.3D, 0.0D);
	}

}
