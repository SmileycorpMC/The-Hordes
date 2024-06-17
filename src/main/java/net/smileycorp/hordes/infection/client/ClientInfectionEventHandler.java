package net.smileycorp.hordes.infection.client;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.Tuple;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.smileycorp.atlas.api.util.RecipeUtils;
import net.smileycorp.hordes.config.ClientConfigHandler;
import net.smileycorp.hordes.infection.HordesInfection;
import net.smileycorp.hordes.infection.jei.JEIPluginInfection;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.List;
import java.util.Map;

public class ClientInfectionEventHandler {
	
	private static final List<ItemStack> cures = Lists.newArrayList();
	private static final Map<ItemStack, Integer> immunityItems = Maps.newHashMap();
	private static final Map<Item, Integer> wearableProtection = Maps.newHashMap();
	
	@SubscribeEvent
	public void renderOverlay(RenderGameOverlayEvent.Post event){
		if (ClientConfigHandler.playerInfectionVisuals) {
			Minecraft mc = Minecraft.getMinecraft();
			EntityPlayer player = mc.player;
			if (player!= null && event.getType() == ElementType.VIGNETTE) {
				if (player.isPotionActive(HordesInfection.INFECTED)) {
					int level = player.getActivePotionEffect(HordesInfection.INFECTED).getAmplifier();
					Color colour = new Color(0.4745f, 0.6117f, 0.3961f, 0.04f*level*level);
					GL11.glDisable(GL11.GL_DEPTH_TEST);
					GL11.glDepthMask(false);
					GL11.glDisable(GL11.GL_ALPHA_TEST);
					GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
					Gui.drawRect(0, 0, mc.displayWidth, mc.displayHeight, colour.getRGB());
					GL11.glDepthMask(true);
					GL11.glEnable(GL11.GL_DEPTH_TEST);
					GL11.glEnable(GL11.GL_ALPHA_TEST);
				}
			}
		}
	}

	@SubscribeEvent
	public void preRenderEntity(RenderLivingEvent.Pre<EntityLivingBase> event){
		if (ClientConfigHandler.playerInfectionVisuals) {
			Minecraft mc = Minecraft.getMinecraft();
			EntityPlayer player = mc.player;
			if (player.isPotionActive(HordesInfection.INFECTED) && event.getEntity() != player) {
				if (player.getActivePotionEffect(HordesInfection.INFECTED).getAmplifier()>2) {
					GlStateManager.colorLogicOp(GlStateManager.LogicOp.SET);
					GlStateManager.color(1, 0, 0);
				} else if (player.getActivePotionEffect(HordesInfection.INFECTED).getAmplifier() == 2) {
					GlStateManager.colorLogicOp(GlStateManager.LogicOp.EQUIV);
					GlStateManager.color(1, 0.4f, 0.4f);
				}
			}
		}
	}

	@SubscribeEvent
	public void postRenderEntity(RenderLivingEvent.Post<EntityLivingBase> event){
		if (ClientConfigHandler.playerInfectionVisuals) {
			Minecraft mc = Minecraft.getMinecraft();
			EntityPlayer player = mc.player;
			if (player.isPotionActive(HordesInfection.INFECTED) && event.getEntity() != player) {
				if (player.getActivePotionEffect(HordesInfection.INFECTED).getAmplifier() >= 2) {
					GlStateManager.color(1, 1, 1);
					GlStateManager.colorLogicOp(GlStateManager.LogicOp.AND);
				}
			}
		}
	}
	
	@SubscribeEvent(priority = EventPriority.HIGH)
	public void tooltip(ItemTooltipEvent event) {
		ItemStack stack = event.getItemStack();
		Item item = stack.getItem();
		if (ClientConfigHandler.cureTooltip && isCure(stack))
			event.getToolTip().add(new TextComponentTranslation("tooltip.hordes.cure").getFormattedText());
		if (ClientConfigHandler.wearableProtectionTooltip && wearableProtection.containsKey(item)) {
			int value = wearableProtection.get(item);
			if (value == 0) return;
			String str = value + "%";
			if (value > 0) str = "+" + str;
			event.getToolTip().add(new TextComponentTranslation("tooltip.hordes.wearableProtection", str)
					.setStyle(new Style().setColor(TextFormatting.BLUE)).getFormattedText());
		}
		if (ClientConfigHandler.immunityTooltip) {
			for (Map.Entry<ItemStack, Integer> entry : immunityItems.entrySet()) {
				ItemStack cure = entry.getKey();
				if (!RecipeUtils.compareItemStacks(stack, cure, cure.getTagCompound() != null)) continue;
				event.getToolTip().add(TextFormatting.BLUE + I18n.translateToLocal("effect.hordes.Immunity") + " (" +
						Potion.getPotionDurationString(new PotionEffect(HordesInfection.IMMUNITY, entry.getValue() * 20), 1) + ")");
			}
		}
	}
	
	public static boolean isCure(ItemStack stack) {
		for (ItemStack cure : cures) if (RecipeUtils.compareItemStacks(stack, cure, cure.getTagCompound() != null)) return true;
		return false;
	}
	
	@SubscribeEvent
	public void logOut(PlayerEvent.PlayerLoggedOutEvent event) {
		cures.clear();
		immunityItems.clear();
		wearableProtection.clear();
	}
	
	public static void readCures(List<ItemStack> data) {
		cures.addAll(data);
		if (Loader.isModLoaded("jei")) JEIPluginInfection.setRecipes(cures);
	}
	
	public static void readImmunityItems(List<Map.Entry<ItemStack, Integer>> data) {
		immunityItems.clear();
		data.forEach(e -> immunityItems.put(e.getKey(), e.getValue()));
	}
	
	public static void readWearableProtection(List<Tuple<Item, Integer>> data) {
		wearableProtection.clear();
		data.forEach(e -> wearableProtection.put(e.getFirst(), e.getSecond()));
	}

}
