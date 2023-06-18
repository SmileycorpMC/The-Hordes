package net.smileycorp.hordes.common.infection;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.LazyOptional;
import net.smileycorp.hordes.common.CommonConfigHandler;
import net.smileycorp.hordes.common.Constants;
import net.smileycorp.hordes.common.Hordes;
import net.smileycorp.hordes.common.infection.capability.IInfection;

import java.util.List;
import java.util.UUID;

public class InfectedEffect extends MobEffect {

	private final UUID SPEED_MOD_UUID = UUID.fromString("05d68949-cb8b-4031-92a6-bd75e42b5cdd");
	private final String SPEED_MOD_NAME = Constants.name("Infected");
	private final AttributeModifier SPEED_MOD = new AttributeModifier(SPEED_MOD_NAME, -0.1, AttributeModifier.Operation.MULTIPLY_TOTAL);

	public InfectedEffect() {
		super(MobEffectCategory.HARMFUL, 0x00440002);
	}

	@Override
	public List<ItemStack> getCurativeItems() {
		return CommonConfigHandler.enableMobInfection.get() ? HordesInfection.getCureList() : super.getCurativeItems();
	}

	@Override
	public void applyEffectTick(LivingEntity entity, int amplifier) {
		if (entity instanceof Player) ((Player)entity).causeFoodExhaustion(0.007F * (amplifier+1));
	}

	@Override
	public boolean isDurationEffectTick(int duration, int amplifier) {
		return CommonConfigHandler.infectHunger.get();
	}

	@Override
	public void addAttributeModifiers(LivingEntity entity, AttributeMap map, int amplifier) {
		if (amplifier < 0 |! CommonConfigHandler.infectSlowness.get()) return;
			AttributeInstance attribute = map.getInstance(Attributes.MOVEMENT_SPEED);
		if (attribute == null) return;
		attribute.removeModifier(SPEED_MOD_UUID);
		attribute.addPermanentModifier(new AttributeModifier(SPEED_MOD_UUID, SPEED_MOD_NAME + " " + amplifier,
				getAttributeModifierValue(amplifier-1, SPEED_MOD), AttributeModifier.Operation.MULTIPLY_TOTAL));
	}

	@Override
	public void removeAttributeModifiers(LivingEntity entity, AttributeMap map, int amplifier) {
		AttributeInstance attribute = map.getInstance(Attributes.MOVEMENT_SPEED);
		if (attribute != null) attribute.removeModifier(SPEED_MOD_UUID);
	}

	public static void apply(LivingEntity entity) {
		entity.addEffect(new MobEffectInstance(HordesInfection.INFECTED.get(), getInfectionTime(entity)));
	}

	public static int getInfectionTime(LivingEntity entity) {
		int time = CommonConfigHandler.ticksForEffectStage.get();
		LazyOptional<IInfection> optional = entity.getCapability(Hordes.INFECTION);
		if (optional.isPresent()) time = (int)((double)time * Math.pow(CommonConfigHandler.effectStageTickReduction.get(), optional.resolve().get().getInfectionCount()));
		return time;
	}

}
