package net.smileycorp.hordes.infection;

import com.mojang.datafixers.util.Pair;
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
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event.Result;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.smileycorp.atlas.api.util.DirectionUtils;
import net.smileycorp.hordes.common.Constants;
import net.smileycorp.hordes.common.capability.HordesCapabilities;
import net.smileycorp.hordes.common.event.InfectionDeathEvent;
import net.smileycorp.hordes.config.InfectionConfig;
import net.smileycorp.hordes.infection.capability.Infection;
import net.smileycorp.hordes.infection.data.InfectionData;
import net.smileycorp.hordes.infection.network.CureEntityMessage;
import net.smileycorp.hordes.infection.network.InfectMessage;
import net.smileycorp.hordes.infection.network.InfectionPacketHandler;

import java.util.UUID;

public class InfectionEventHandler {

	//attach required entity capabilities for event to function
	@SubscribeEvent
	public void attachCapabilities(AttachCapabilitiesEvent<Entity> event) {
		Entity entity = event.getObject();
		if (entity instanceof Player && !(entity instanceof FakePlayer) || InfectionData.INSTANCE.canBeInfected(entity)) {
			event.addCapability(Constants.loc("InfectionCounter"), new Infection.Provider());
		}
	}

	//register data listeners
	@SubscribeEvent
	public void addResourceReload(AddReloadListenerEvent event) {
		event.addListener(InfectionData.INSTANCE);
	}

	@SubscribeEvent
	public void onEntityAdded(EntityJoinLevelEvent event) {
		if (!(event.getEntity() instanceof LivingEntity)) return;
		LivingEntity entity = (LivingEntity) event.getEntity();
		if (entity.level().isClientSide) return;
		if (InfectionData.INSTANCE.canBeInfected(entity))
			entity.getAttribute(HordesInfection.INFECTION_RESISTANCE.get()).setBaseValue(InfectionData.INSTANCE.getProtection(entity.getType()));
		if (!InfectionData.INSTANCE.hasInfectAttribute(entity)) return;
		entity.getAttribute(HordesInfection.INFECTIVITY.get()).setBaseValue(InfectionData.INSTANCE.getInfectionChance(entity.getType()));
		if (!(entity instanceof Mob && InfectionConfig.infectionEntitiesAggroConversions.get())) return;
		((Mob) entity).targetSelector.addGoal(3, new NearestAttackableTargetGoal<>((Mob) entity,
				LivingEntity.class, 10, true, false, InfectionData.INSTANCE::infectedTarget));
	}
	
	@SubscribeEvent
	public void logIn(PlayerEvent.PlayerLoggedInEvent event) {
		if (event.getEntity() instanceof ServerPlayer)
			InfectionData.INSTANCE.syncData(((ServerPlayer)event.getEntity()));
	}

	@SubscribeEvent
	public void onItemStackConsume(LivingEntityUseItemEvent.Finish event) {
		LivingEntity entity = event.getEntity();
		ItemStack stack = event.getItem();
		if (InfectionData.INSTANCE.applyImmunity(entity, stack.getItem())) return;
		if (!(entity.hasEffect(HordesInfection.INFECTED.get()) && HordesInfection.isCure(stack))) return;
		LazyOptional<Infection> optional = entity.getCapability(HordesCapabilities.INFECTION);
		if (optional.isPresent()) optional.orElseGet(null).increaseInfection();
		if (!entity.removeEffect(HordesInfection.INFECTED.get())) return;
		if (entity.level().isClientSide) return;
		InfectionPacketHandler.sendTracking(new CureEntityMessage(entity), entity);
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onItemUse(PlayerInteractEvent.RightClickItem event) {
		ItemStack stack = event.getItemStack();
		HitResult ray = DirectionUtils.getEntityRayTrace(event.getLevel(), event.getEntity(), 5);
		if (!(ray instanceof EntityHitResult)) return;
		if (!(((EntityHitResult) ray).getEntity() instanceof LivingEntity)) return;
		LivingEntity entity = (LivingEntity) ((EntityHitResult) ray).getEntity();
		if (entity instanceof Player |!(entity.hasEffect(HordesInfection.INFECTED.get()) || HordesInfection.isCure(stack))) return;
		entity.removeEffect(HordesInfection.INFECTED.get());
		LazyOptional<Infection> optional = entity.getCapability(HordesCapabilities.INFECTION);
		if (optional.isPresent()) optional.orElseGet(null).increaseInfection();
		event.setCanceled(true);
		event.setCancellationResult(InteractionResult.FAIL);
	}

	@SubscribeEvent
	public void onDamage(LivingDamageEvent event) {
		LivingEntity entity = event.getEntity();
		Entity attacker = event.getSource().getDirectEntity();
		Level level = entity.level();
		if (level.isClientSide |! (entity instanceof LivingEntity && attacker instanceof LivingEntity)) return;
		if (!InfectionData.INSTANCE.canCauseInfection(attacker) || entity.hasEffect(HordesInfection.INFECTED.get())) return;
		if (InfectionData.INSTANCE.canBeInfected(entity))
			InfectionData.INSTANCE.tryToInfect(entity, (LivingEntity) attacker, event.getSource(), event.getAmount());
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled=true)
	public void onDeath(LivingDeathEvent event) {
		LivingEntity entity = event.getEntity();
		DamageSource source = event.getSource();
		Level level = entity.level();
		if (level.isClientSide || !(source.is(HordesInfection.INFECTION_DAMAGE) || entity.hasEffect(HordesInfection.INFECTED.get()))) return;
		if (MinecraftForge.EVENT_BUS.post(new InfectionDeathEvent(entity, event.getSource()))) {
			event.setCanceled(true);
			return;
		}
		if (!(entity instanceof OwnableEntity)) return;
		LivingEntity owner = ((OwnableEntity) entity).getOwner();
		if (!(owner instanceof ServerPlayer)) return;
		owner.sendSystemMessage(Component.translatable("death.attack.infection.zombified", entity.getDisplayName()));
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
		if (event.getEffectInstance().getEffect() != HordesInfection.INFECTED.get()) return;
		if (InfectedEffect.preventInfection(entity)) {
			event.setResult(Result.DENY);
			if (entity instanceof ServerPlayer)
				InfectionPacketHandler.sendTo(new InfectMessage(true), (ServerPlayer) entity);
		} else if (entity.hasEffect(HordesInfection.INFECTED.get()) && entity.getEffect(HordesInfection.INFECTED.get()).getAmplifier()
				< event.getEffectInstance().getAmplifier()) entity.removeEffect(HordesInfection.INFECTED.get());
	}

	@SubscribeEvent
	public void applyEffect(MobEffectEvent.Added event) {
		LivingEntity entity = event.getEntity();
		if (entity.level().isClientSide()) return;
		if (event.getEffectInstance().getEffect() == HordesInfection.IMMUNITY.get() && entity.hasEffect(HordesInfection.INFECTED.get()))
			if (entity.removeEffect(HordesInfection.INFECTED.get())) InfectionPacketHandler.sendTracking(new CureEntityMessage(entity), entity);
	}

	@SubscribeEvent
	public void effectExpired(MobEffectEvent.Expired event) {
		LivingEntity entity = event.getEntity();
		MobEffectInstance instance = event.getEffectInstance();
		if (instance == null) return;
		if (instance.getEffect() == HordesInfection.INFECTED.get() && InfectionConfig.enableMobInfection.get()) {
			int amplifier = instance.getAmplifier();
			if (amplifier < 3) {
				entity.addEffect(new MobEffectInstance(HordesInfection.INFECTED.get(), InfectedEffect.getInfectionTime(entity), amplifier + 1));
				if (entity instanceof ServerPlayer) InfectionPacketHandler.sendTo(new InfectMessage(false), (ServerPlayer) entity);
			}
			else entity.hurt(HordesInfection.getInfectionDamage(entity), Float.MAX_VALUE);
		}
	}

	@SubscribeEvent
	public void addItemAttributes(ItemAttributeModifierEvent event) {
		ItemStack stack = event.getItemStack();
		EquipmentSlot slot = LivingEntity.getEquipmentSlotForItem(stack);
		if (event.getSlotType() != slot) return;
		Pair<Float, AttributeModifier.Operation> pair = InfectionData.INSTANCE.getProtection(stack);
		if (pair == null) return;
		String name = Constants.locStr("infection_resistance", slot.getName());
		event.addModifier(HordesInfection.INFECTION_RESISTANCE.get(), new AttributeModifier(UUID.nameUUIDFromBytes(name.getBytes()), name,
				pair.getFirst(), pair.getSecond()));
	}

	public static void addEntityAttributes(EntityAttributeModificationEvent event) {
		for (EntityType<?> type : event.getTypes()) {
			event.add((EntityType<? extends LivingEntity>) type, HordesInfection.INFECTION_RESISTANCE.get(), 0);
			event.add((EntityType<? extends LivingEntity>) type, HordesInfection.INFECTIVITY.get(), 0);
		}
	}

}
