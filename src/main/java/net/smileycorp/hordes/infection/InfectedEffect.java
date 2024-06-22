package net.smileycorp.hordes.infection;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.EffectCure;
import net.smileycorp.hordes.common.Constants;
import net.smileycorp.hordes.common.capability.HordesCapabilities;
import net.smileycorp.hordes.config.InfectionConfig;
import net.smileycorp.hordes.infection.capability.Infection;
import net.smileycorp.hordes.infection.network.InfectMessage;
import net.smileycorp.hordes.infection.network.InfectionPacketHandler;

import java.util.Set;

public class InfectedEffect extends MobEffect {
	
	private final ResourceLocation SPEED_MOD_NAME = Constants.loc("infected");
	private final double SPEED_MOD_AMOUNT = -0.1;
	
	public InfectedEffect() {
		super(MobEffectCategory.HARMFUL, 0x00440002);
	}
	
	@Override
	public void fillEffectCures(Set<EffectCure> cures, MobEffectInstance effectInstance) {}
	
	@Override
	public boolean applyEffectTick(LivingEntity entity, int amplifier) {
		if (entity instanceof Player) ((Player)entity).causeFoodExhaustion(0.03F * (amplifier + 1));
		return true;
	}
	
	@Override
	public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
		return InfectionConfig.infectHunger.get();
	}
	
	@Override
	public void addAttributeModifiers(AttributeMap map, int amplifier) {
		if (amplifier < 0 |! InfectionConfig.infectSlowness.get()) return;
		AttributeInstance attribute = map.getInstance(Attributes.MOVEMENT_SPEED);
		if (attribute == null) return;
		attribute.removeModifier(SPEED_MOD_NAME);
		attribute.addPermanentModifier(new AttributeModifier(SPEED_MOD_NAME,
				SPEED_MOD_AMOUNT * amplifier, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
	}
	
	@Override
	public void removeAttributeModifiers(AttributeMap map) {
		AttributeInstance attribute = map.getInstance(Attributes.MOVEMENT_SPEED);
		if (attribute != null) attribute.removeModifier(SPEED_MOD_NAME);
	}
	
	public static void apply(LivingEntity entity) {
		boolean prevented = preventInfection(entity);
		if (entity instanceof ServerPlayer) InfectionPacketHandler.sendTo(new InfectMessage(prevented), ((ServerPlayer) entity));
		if (!prevented) entity.addEffect(new MobEffectInstance(HordesInfection.INFECTED, getInfectionTime(entity)));
	}
	
	public static boolean preventInfection(LivingEntity entity) {
		return entity.hasEffect(HordesInfection.IMMUNITY);
	}
	
	public static int getInfectionTime(LivingEntity entity) {
		int time = InfectionConfig.ticksForEffectStage.get();
		Infection infection = entity.getCapability(HordesCapabilities.INFECTION);
		if (infection != null) time = (int)((double)time * Math.pow(InfectionConfig.effectStageTickReduction.get(), infection.getInfectionCount()));
		return time;
	}
	
}