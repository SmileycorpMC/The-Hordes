package net.smileycorp.hordes.infection.client;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.CustomizeGuiOverlayEvent;
import net.neoforged.neoforge.client.event.RenderLivingEvent;
import net.neoforged.neoforge.client.event.RenderTooltipEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.smileycorp.hordes.config.ClientConfigHandler;
import net.smileycorp.hordes.infection.HordesInfection;

import java.awt.*;
import java.util.List;
import java.util.Map;

public class ClientInfectionEventHandler {
	
	private static Map<Item, Integer> immunityItems = Maps.newHashMap();
	private static Map<Item, Integer> wearableProtection = Maps.newHashMap();
	
	@SubscribeEvent
	public void renderOverlay(CustomizeGuiOverlayEvent event){
		if (!ClientConfigHandler.playerInfectionVisuals.get()) return;
		Minecraft mc = Minecraft.getInstance();
		LocalPlayer player = mc.player;
		if (player == null) return;
		if (!player.hasEffect(HordesInfection.INFECTED)) return;
		int a = player.getEffect(HordesInfection.INFECTED).getAmplifier();
		if (a == 0) return;
		Color colour = new Color(0.4745f, 0.6117f, 0.3961f, 0.01f * a);
		Window window = mc.getWindow();
		event.getGuiGraphics().fill(0, 0, window.getWidth(), window.getHeight(), colour.getRGB());
	}

	@SubscribeEvent
	public void preRenderEntity(RenderLivingEvent.Pre event){
		LivingEntity entity = event.getEntity();
		Player player = Minecraft.getInstance().player;
		if (ClientConfigHandler.playerInfectionVisuals.get() && player != null && player.hasEffect(HordesInfection.INFECTED) && entity != player) {
			int a = player.getEffect(HordesInfection.INFECTED).getAmplifier();
			if (a > 2) RenderSystem.setShaderColor(1, 0, 0, 1);
			else if (a == 2) RenderSystem.setShaderColor(1, 0.4f, 0.4f, 1);
		}
	}

	@SubscribeEvent
	public void postRenderEntity(RenderLivingEvent.Post event){
		if (RenderSystem.getShaderColor().equals(new float[]{1, 1, 1, 1})) return;
		RenderSystem.setShaderColor(1, 1, 1, 1);
	}
	
	@SubscribeEvent(priority = EventPriority.HIGH)
	public void tooltip(RenderTooltipEvent.GatherComponents event) {
		ItemStack stack = event.getItemStack();
		Item item = stack.getItem();
		List<Component> components = Lists.newArrayList();
		if (ClientConfigHandler.cureTooltip.get() && stack.is(HordesInfection.INFECTION_CURES_TAG))
			components.add(Component.translatable("tooltip.hordes.cure"));
		if (ClientConfigHandler.immunityTooltip.get() && immunityItems.containsKey(item))
			PotionContents.addPotionTooltip(Lists.newArrayList(new MobEffectInstance(HordesInfection.IMMUNITY,
					immunityItems.get(item) * 20)), components::add, 1, Minecraft.getInstance().level.tickRateManager().tickrate());
		if (ClientConfigHandler.wearableProtectionTooltip.get() && wearableProtection.containsKey(item)) {
			int value = wearableProtection.get(item);
			if (value == 0) return;
			String str = value + "%";
			if (value > 0) str = "+" + str;
			components.add(Component.translatable("tooltip.hordes.wearableProtection", str).withStyle(ChatFormatting.BLUE));
		}
		components.forEach(c -> event.getTooltipElements().add(Either.left(c)));
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
