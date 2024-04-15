package net.smileycorp.hordes.infection.client;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.IngameGui;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.EffectUtils;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.smileycorp.hordes.config.ClientConfigHandler;
import net.smileycorp.hordes.infection.HordesInfection;

import java.awt.*;
import java.util.List;
import java.util.Map;

public class ClientInfectionEventHandler {
	
	private static Map<Item, Integer> immunityItems = Maps.newHashMap();
	private static Map<Item, Integer> wearableProtection = Maps.newHashMap();
	
	@SubscribeEvent
	public void renderOverlay(RenderGameOverlayEvent event){
		if (!ClientConfigHandler.playerInfectionVisuals.get()) return;
		Minecraft mc = Minecraft.getInstance();
		ClientPlayerEntity player = mc.player;
		if (player == null) return;
		if (!player.hasEffect(HordesInfection.INFECTED.get())) return;
		int level = player.getEffect(HordesInfection.INFECTED.get()).getAmplifier();
		Color colour = new Color(0.4745f, 0.6117f, 0.3961f, 0.01f*level);
		MainWindow window = mc.getWindow();
		IngameGui.fill(event.getMatrixStack(), 0, 0, window.getWidth(), window.getHeight(), colour.getRGB());
	}
	
	@SubscribeEvent
	public void preRenderEntity(RenderLivingEvent.Pre event){
		if (ClientConfigHandler.playerInfectionVisuals.get()) {
			Minecraft mc = Minecraft.getInstance();
			ClientPlayerEntity player = mc.player;
			if (player.hasEffect(HordesInfection.INFECTED.get()) && event.getEntity() != player) {
				if (ClientConfigHandler.playerInfectionVisuals.get() && player != null && player.hasEffect(HordesInfection.INFECTED.get()) && entity != player) {
					int a = player.getEffect(HordesInfection.INFECTED.get()).getAmplifier();
					if (a > 2) GlStateManager._color4f(1, 0, 0, 1);
					else if (a == 2) GlStateManager._color4f(1, 0.4f, 0.4f, 1);
				}
			}
		}
	}
	
	@SubscribeEvent
	public void postRenderEntity(RenderLivingEvent.Post event){
		if (ClientConfigHandler.playerInfectionVisuals.get()) {
			Minecraft mc = Minecraft.getInstance();
			PlayerEntity player = mc.player;
			if (player.hasEffect(HordesInfection.INFECTED.get()) && event.getEntity() != player) {
				if (player.getEffect(HordesInfection.INFECTED.get()).getAmplifier() >= 2) {
					GlStateManager._color4f(1, 1, 1, 1);
				}
			}
		}
	}
	
	@SubscribeEvent(priority = EventPriority.HIGH)
	public void tooltip(ItemTooltipEvent event) {
		ItemStack stack = event.getItemStack();
		Item item = stack.getItem();
		if (ClientConfigHandler.cureTooltip.get() && stack.getItem().is(HordesInfection.INFECTION_CURES_TAG))
			event.getToolTip().add(new TranslationTextComponent("tooltip.hordes.cure"));
		if (ClientConfigHandler.immunityTooltip.get() && immunityItems.containsKey(item)) {
			event.getToolTip().add(new TranslationTextComponent("potion.withDuration", new TranslationTextComponent("effect.hordes.immunity"),
					EffectUtils.formatDuration(new EffectInstance(HordesInfection.IMMUNITY.get(),immunityItems.get(item) * 20), 1))
					.withStyle(TextFormatting.BLUE));
		}
		if (ClientConfigHandler.wearableProtectionTooltip.get() && wearableProtection.containsKey(item)) {
			int value = wearableProtection.get(item);
			if (value == 0) return;
			String str = value + "%";
			if (value > 0) str = "+" + str;
			event.getToolTip().add(new TranslationTextComponent("tooltip.hordes.wearableProtection", str).withStyle(TextFormatting.BLUE));
		}
	}
	
	@SubscribeEvent
	public void logOut(PlayerEvent.PlayerLoggedOutEvent event) {
		immunityItems.clear();
		wearableProtection.clear();
	}
	
	public static void readImmunityItems(List<Map.Entry<Item, Integer>> data) {
		immunityItems.clear();
		data.forEach(e -> immunityItems.put(e.getKey(), e.getValue()));
	}
	
	public static void readWearableProtection(List<Pair<Item, Integer>> data) {
		wearableProtection.clear();
		data.forEach(e -> wearableProtection.put(e.getFirst(), e.getSecond()));
	}
	
}
