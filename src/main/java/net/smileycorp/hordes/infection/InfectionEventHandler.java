package net.smileycorp.hordes.infection;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.ItemAttributeModifierEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.smileycorp.atlas.api.util.VecMath;
import net.smileycorp.hordes.common.Constants;
import net.smileycorp.hordes.common.capability.HordesCapabilities;
import net.smileycorp.hordes.common.event.InfectionDeathEvent;
import net.smileycorp.hordes.config.InfectionConfig;
import net.smileycorp.hordes.infection.capability.Infection;
import net.smileycorp.hordes.infection.data.InfectionData;
import net.smileycorp.hordes.infection.network.CureEntityMessage;
import net.smileycorp.hordes.infection.network.InfectMessage;
import net.smileycorp.hordes.infection.network.InfectionPacketHandler;

public class InfectionEventHandler {

	//register data listeners
	@SubscribeEvent
	public void addResourceReload(AddReloadListenerEvent event) {
		event.addListener(InfectionData.INSTANCE);
	}

	@SubscribeEvent
	public void onEntityAdded(EntityJoinLevelEvent event) {
		Entity entity = event.getEntity();
		if (InfectionData.INSTANCE.canBeInfected(entity) && entity instanceof LivingEntity)
			((LivingEntity) entity).getAttribute(HordesInfection.INFECTION_RESISTANCE).setBaseValue(InfectionData.INSTANCE.getProtection(entity.getType()));
		if (!(entity instanceof Mob && InfectionConfig.infectionEntitiesAggroConversions.get()) || entity.level().isClientSide) return;
		if (InfectionData.INSTANCE.canCauseInfection(entity)) {
			((Mob) entity).targetSelector.addGoal(3, new NearestAttackableTargetGoal<>((Mob) entity, LivingEntity.class,
					10, true, false, InfectionData.INSTANCE::canBeInfected));
		}
	}
	
	@SubscribeEvent
	public void logIn(PlayerEvent.PlayerLoggedInEvent event) {
		if (event.getEntity() instanceof ServerPlayer)
			InfectionData.INSTANCE.syncData((ServerPlayer)event.getEntity());
	}

	@SubscribeEvent
	public void onItemStackConsume(LivingEntityUseItemEvent.Finish event) {
		LivingEntity entity = event.getEntity();
		ItemStack stack = event.getItem();
		if (InfectionData.INSTANCE.applyImmunity(entity, stack.getItem())) return;
		if (!(entity.hasEffect(HordesInfection.INFECTED) && HordesInfection.isCure(stack))) return;
		Infection infection = entity.getCapability(HordesCapabilities.INFECTION);
		if (infection != null) infection.increaseInfection();
		if (!entity.removeEffect(HordesInfection.INFECTED)) return;
		if (entity.level().isClientSide) return;
		InfectionPacketHandler.sendTracking(new CureEntityMessage(entity), entity);
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onItemUse(PlayerInteractEvent.RightClickItem event) {
		ItemStack stack = event.getItemStack();
		HitResult ray = VecMath.entityRayTrace(event.getLevel(), event.getEntity(), 5);
		if (!(ray instanceof EntityHitResult)) return;
		if (!(((EntityHitResult) ray).getEntity() instanceof LivingEntity)) return;
		LivingEntity entity = (LivingEntity) ((EntityHitResult) ray).getEntity();
		if (entity instanceof Player |!(entity.hasEffect(HordesInfection.INFECTED) || HordesInfection.isCure(stack))) return;
		entity.removeEffect(HordesInfection.INFECTED);
		Infection infection = entity.getCapability(HordesCapabilities.INFECTION);
		if (infection != null) infection.increaseInfection();
		event.setCanceled(true);
		event.setCancellationResult(InteractionResult.FAIL);
	}

	@SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
	public void onDamage(LivingDamageEvent.Post event) {
		LivingEntity entity = event.getEntity();
		Entity attacker = event.getSource().getDirectEntity();
		Level level = entity.level();
		if (level.isClientSide) return;
		if (!InfectionData.INSTANCE.canCauseInfection(attacker) || entity.hasEffect(HordesInfection.INFECTED)) return;
		if (InfectionData.INSTANCE.canBeInfected(entity))
			InfectionData.INSTANCE.tryToInfect(entity, (LivingEntity) attacker, event.getSource(), event.getNewDamage());
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
	public void onDeath(LivingDeathEvent event) {
		LivingEntity entity = event.getEntity();
		DamageSource source = event.getSource();
		Level level = entity.level();
		if (level.isClientSide || !(source.is(HordesInfection.INFECTION_DAMAGE) || entity.hasEffect(HordesInfection.INFECTED))) return;
		InfectionDeathEvent deathevent = new InfectionDeathEvent(entity, event.getSource());
		NeoForge.EVENT_BUS.post(deathevent);
		if (deathevent.isCanceled()) {
			event.setCanceled(true);
			if (!(entity instanceof OwnableEntity)) return;
			LivingEntity owner = ((OwnableEntity) entity).getOwner();
			if (!(owner instanceof ServerPlayer)) return;
			owner.sendSystemMessage(Component.translatable("death.attack.infection.zombified", entity.getDisplayName()));
		}
	}

	@SubscribeEvent
	public void onInfectDeath(InfectionDeathEvent event) {
		LivingEntity entity = event.getEntity();
		if (entity instanceof Player) return;
		if (!InfectionData.INSTANCE.canBeInfected(entity)) return;
		if (InfectionData.INSTANCE.convertEntity((Mob) entity)) return;
		event.setCanceled(true);
	}

	@SubscribeEvent
	public void canApplyEffect(MobEffectEvent.Applicable event) {
		LivingEntity entity = event.getEntity();
		if (entity.level().isClientSide()) return;
		if (event.getEffectInstance().is(HordesInfection.INFECTED)) {
			if (InfectedEffect.preventInfection(entity)) {
				event.setResult(MobEffectEvent.Applicable.Result.DO_NOT_APPLY);
				if (entity instanceof ServerPlayer)
					InfectionPacketHandler.sendTo(new InfectMessage(true), (ServerPlayer) entity);
			} else if (entity.hasEffect(HordesInfection.INFECTED) && entity.getEffect(HordesInfection.INFECTED).getAmplifier()
					< event.getEffectInstance().getAmplifier())
				entity.removeEffect(HordesInfection.INFECTED);
		}
	}

	@SubscribeEvent
	public void applyEffect(MobEffectEvent.Added event) {
		LivingEntity entity = event.getEntity();
		if (entity.level().isClientSide()) return;
		if (event.getEffectInstance().getEffect() == HordesInfection.IMMUNITY.get() && entity.hasEffect(HordesInfection.INFECTED))
			if (entity.removeEffect(HordesInfection.INFECTED)) InfectionPacketHandler.sendTracking(new CureEntityMessage(entity), entity);
	}
	
	@SubscribeEvent
	public void effectExpired(MobEffectEvent.Expired event) {
		LivingEntity entity = event.getEntity();
		MobEffectInstance instance = event.getEffectInstance();
		if (instance == null) return;
		if (instance.is(HordesInfection.INFECTED) && InfectionConfig.enableMobInfection.get()) {
			event.setCanceled(true);
			int amplifier = instance.getAmplifier();
			if (amplifier < 3) {
				entity.addEffect(new MobEffectInstance(HordesInfection.INFECTED, InfectedEffect.getInfectionTime(entity), amplifier + 1));
				if (entity instanceof ServerPlayer) InfectionPacketHandler.sendTo(new InfectMessage(false), (ServerPlayer) entity);
			}
			else entity.hurt(HordesInfection.getInfectionDamage(entity), Float.MAX_VALUE);
		}
	}
	
	@SubscribeEvent
	public void addItemAttributes(ItemAttributeModifierEvent event) {
		ItemStack stack = event.getItemStack();
		EquipmentSlot slot = getSlot(stack);
		float value = InfectionData.INSTANCE.getProtectionMultiplier(stack);
		if (value == 0) return;
		event.addModifier(HordesInfection.INFECTION_RESISTANCE, new AttributeModifier(Constants.loc(slot.getName()), value,
				AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL), EquipmentSlotGroup.bySlot(slot));
	}
	
	public static EquipmentSlot getSlot(ItemStack stack) {
		EquipmentSlot slot = stack.getEquipmentSlot();
		if (slot != null) return slot;
		Equipable equip = Equipable.get(stack);
		return equip == null ? null : equip.getEquipmentSlot();
	}
	
	public static void addEntityAttributes(EntityAttributeModificationEvent event) {
		for (EntityType<?> type : event.getTypes()) event.add((EntityType<? extends LivingEntity>) type, HordesInfection.INFECTION_RESISTANCE, 0);
	}
	
}
