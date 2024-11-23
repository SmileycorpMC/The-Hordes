package net.smileycorp.hordes.infection.client;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Either;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RenderLivingEvent;
import net.neoforged.neoforge.client.event.RenderTooltipEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.smileycorp.hordes.common.Constants;
import net.smileycorp.hordes.config.ClientConfigHandler;
import net.smileycorp.hordes.infection.HordesInfection;
import net.smileycorp.hordes.infection.data.InfectionData;
import net.smileycorp.hordes.infection.network.CureEntityMessage;

import java.util.List;

public class InfectionClientHandler {
	
	public static final InfectionClientHandler INSTANCE = new InfectionClientHandler();
	
	public void registerOverlays(RegisterGuiLayersEvent event) {
		if (!ClientConfigHandler.playerInfectionVisuals.get()) return;
		event.registerBelowAll(Constants.loc("infection"), new InfectionLayer());
	}
	
	@SubscribeEvent
	public void preRenderEntity(RenderLivingEvent.Pre event) {
		LivingEntity entity = event.getEntity();
		Player player = Minecraft.getInstance().player;
		if (!ClientConfigHandler.playerInfectionVisuals.get()) return;
		if (player != null && player.hasEffect(HordesInfection.INFECTED) && entity != player) {
			int a = player.getEffect(HordesInfection.INFECTED).getAmplifier();
			if (a > 2) RenderSystem.setShaderColor(1, 0.3f, 0.3f, 1);
			else if (a == 2) RenderSystem.setShaderColor(1, 0.5f, 0.5f, 1);
			else if (a == 1) RenderSystem.setShaderColor(1, 0.7f, 0.7f, 1);
			if (a > 0) return;
		}
		if (entity.hasEffect(HordesInfection.INFECTED)) {
			int a = entity.getEffect(HordesInfection.INFECTED).getAmplifier();
			RenderSystem.setShaderColor((float) Math.pow(0.95f, a + 1), 1, (float) Math.pow(0.8f, a + 1), 1);
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
		List<Component> components = Lists.newArrayList();
		if (ClientConfigHandler.cureTooltip.get() && stack.is(HordesInfection.INFECTION_CURES_TAG))
			components.add(Component.translatable("tooltip.hordes.cure"));
		if (ClientConfigHandler.immunityTooltip.get()) {
			int immunity = InfectionData.INSTANCE.getImmunityLength(stack);
			if (immunity > 0) PotionContents.addPotionTooltip(Lists.newArrayList(new MobEffectInstance(HordesInfection.IMMUNITY,
					immunity * 20)), components::add, 1, Minecraft.getInstance().level.tickRateManager().tickrate());
		}
		components.forEach(c -> event.getTooltipElements().add(Either.left(c)));
	}
	
	@SubscribeEvent
	public void logOut(PlayerEvent.PlayerLoggedOutEvent event) {
		InfectionData.INSTANCE.clear();
	}
	
	public void onInfect(boolean prevented) {
		SoundEvent event = (prevented && ClientConfigHandler.infectionProtectSound.get()) ? Constants.IMMUNE_SOUND :
				(!prevented && ClientConfigHandler.playerInfectSound.get()) ? Constants.INFECT_SOUND : null;
		if (event == null) return;
		LocalPlayer player = Minecraft.getInstance().player;
		player.level().playSound(player, player.blockPosition(), event, SoundSource.PLAYERS, 0.75f, player.getRandom().nextFloat());
	}
	
	public void processCureEntity(CureEntityMessage message) {
		Minecraft mc = Minecraft.getInstance();
		Level level = mc.level;
		Entity entity = message.getEntity(level);
		level.playLocalSound(entity.getX(), entity.getY(), entity.getZ(), SoundEvents.EXPERIENCE_ORB_PICKUP, entity.getSoundSource(), 1f, 1f, true);
		RandomSource rand = level.random;
		for (int i = 0; i < 10; i++) level.addParticle(ParticleTypes.HAPPY_VILLAGER, entity.getX() + (rand.nextDouble() - 0.5D) * entity.getBbWidth() * 1.5,
				entity.getY() + rand.nextDouble() * entity.getBbHeight(), entity.getZ() + (rand.nextDouble() - 0.5D) * entity.getBbWidth() * 1.5, 0.0D, 0.3D, 0.0D);
	}

}
