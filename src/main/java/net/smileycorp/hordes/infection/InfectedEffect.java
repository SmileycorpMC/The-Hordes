package net.smileycorp.hordes.infection;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.network.NetworkDirection;
import net.smileycorp.hordes.common.CommonConfigHandler;
import net.smileycorp.hordes.common.Constants;
import net.smileycorp.hordes.common.capability.HordesCapabilities;
import net.smileycorp.hordes.infection.capability.Infection;
import net.smileycorp.hordes.infection.network.InfectMessage;
import net.smileycorp.hordes.infection.network.InfectionPacketHandler;

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
		if (entity instanceof Player) ((Player)entity).causeFoodExhaustion(0.03F * (amplifier + 1));
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
				getAttributeModifierValue(amplifier - 1, SPEED_MOD), AttributeModifier.Operation.MULTIPLY_TOTAL));
	}

	@Override
	public void removeAttributeModifiers(LivingEntity entity, AttributeMap map, int amplifier) {
		AttributeInstance attribute = map.getInstance(Attributes.MOVEMENT_SPEED);
		if (attribute != null) attribute.removeModifier(SPEED_MOD_UUID);
	}

	public static void apply(LivingEntity entity) {
		boolean prevented = preventInfection(entity);
		if (entity instanceof ServerPlayer) InfectionPacketHandler.NETWORK_INSTANCE.sendTo(new InfectMessage(prevented),
				((ServerPlayer) entity).connection.connection, NetworkDirection.PLAY_TO_CLIENT);
		if (!prevented) entity.addEffect(new MobEffectInstance(HordesInfection.INFECTED.get(), getInfectionTime(entity)));
	}

	public static boolean preventInfection(LivingEntity entity) {
		if (entity.hasEffect(HordesInfection.IMMUNITY.get())) return true;
		for (EquipmentSlot slot : EquipmentSlot.values()) if (slot.isArmor() &! entity.getItemBySlot(slot).is(HordesInfection.IMMUNE_WEARABLES_TAG)) return false;
		return false;
	}

	public static int getInfectionTime(LivingEntity entity) {
		int time = CommonConfigHandler.ticksForEffectStage.get();
		LazyOptional<Infection> optional = entity.getCapability(HordesCapabilities.INFECTION);
		if (optional.isPresent()) time = (int)((double)time * Math.pow(CommonConfigHandler.effectStageTickReduction.get(), optional.orElseGet(null).getInfectionCount()));
		return time;
	}

}
