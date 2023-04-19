package net.smileycorp.hordes.common.infection;

import java.util.List;
import java.util.UUID;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.smileycorp.hordes.common.CommonConfigHandler;
import net.smileycorp.hordes.common.Constants;

public class InfectedEffect extends MobEffect {

	private final UUID SPEED_MOD_UUID = UUID.fromString("05d68949-cb8b-4031-92a6-bd75e42b5cdd");
	private final String SPEED_MOD_NAME = Constants.name("Infected");
	private final AttributeModifier SPEED_MOD = new AttributeModifier(SPEED_MOD_NAME, -0.1, AttributeModifier.Operation.MULTIPLY_TOTAL);

	public InfectedEffect() {
		super(MobEffectCategory.HARMFUL, 0x00440002);
	}

	@Override
	public List<ItemStack> getCurativeItems() {
		return CommonConfigHandler.enableMobInfection.get() ? InfectionRegister.getCureList() : super.getCurativeItems();
	}

	@Override
	public void applyEffectTick(LivingEntity entity, int amplifier) {
		if (entity instanceof Player) {
			((Player)entity).causeFoodExhaustion(0.007F * (amplifier+1));
		}
	}

	@Override
	public boolean isDurationEffectTick(int duration, int amplifier) {
		return CommonConfigHandler.infectHunger.get();
	}

	@Override
	public void addAttributeModifiers(LivingEntity entity, AttributeMap map, int amplifier) {
		if (amplifier > 0 && CommonConfigHandler.infectSlowness.get()) {
			AttributeInstance attribute = map.getInstance(Attributes.MOVEMENT_SPEED);
			if (attribute != null) {
				attribute.removeModifier(SPEED_MOD_UUID);
				attribute.addPermanentModifier(new AttributeModifier(SPEED_MOD_UUID, SPEED_MOD_NAME + " " + amplifier, this.getAttributeModifierValue(amplifier-1, SPEED_MOD), AttributeModifier.Operation.MULTIPLY_TOTAL));
			}
		}
	}

	@Override
	public void removeAttributeModifiers(LivingEntity entity, AttributeMap map, int amplifier) {
		AttributeInstance attribute = map.getInstance(Attributes.MOVEMENT_SPEED);
		if (attribute != null) attribute.removeModifier(SPEED_MOD_UUID);
	}

}
