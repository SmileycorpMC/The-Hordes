package net.smileycorp.hordes.infection;

import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.monster.ZombieVillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.smileycorp.atlas.api.util.VecMath;
import net.smileycorp.hordes.common.capability.HordesCapabilities;
import net.smileycorp.hordes.common.event.InfectionDeathEvent;
import net.smileycorp.hordes.config.InfectionConfig;
import net.smileycorp.hordes.infection.capability.Infection;
import net.smileycorp.hordes.infection.data.InfectionDataLoader;
import net.smileycorp.hordes.infection.network.CureEntityMessage;
import net.smileycorp.hordes.infection.network.InfectMessage;
import net.smileycorp.hordes.infection.network.InfectionPacketHandler;

public class InfectionEventHandler {

	//register data listeners
	@SubscribeEvent
	public void addResourceReload(AddReloadListenerEvent event) {
		event.addListener(InfectionDataLoader.INSTANCE);
	}

	@SubscribeEvent
	public void onEntityAdded(EntityJoinLevelEvent event) {
		Entity entity = event.getEntity();
		if (!(entity instanceof Mob && InfectionConfig.infectionEntitiesAggroConversions.get()) || entity.level().isClientSide) return;
		if (HordesInfection.canCauseInfection((LivingEntity) entity)) {
			((Mob) entity).targetSelector.addGoal(3, new NearestAttackableTargetGoal<>((Mob) entity, LivingEntity.class,
					10, true, false, InfectionDataLoader.INSTANCE::canBeInfected));
		}
	}
	
	@SubscribeEvent
	public void logIn(PlayerEvent.PlayerLoggedInEvent event) {
		if (event.getEntity() instanceof ServerPlayer)
			InfectionDataLoader.INSTANCE.syncData((ServerPlayer)event.getEntity());
	}

	@SubscribeEvent
	public void onItemStackConsume(LivingEntityUseItemEvent.Finish event) {
		LivingEntity entity = event.getEntity();
		ItemStack stack = event.getItem();
		if (InfectionDataLoader.INSTANCE.applyImmunity(entity, stack.getItem())) return;
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

	@SubscribeEvent
	public void onDamage(LivingDamageEvent event) {
		LivingEntity entity = event.getEntity();
		Entity attacker = event.getSource().getDirectEntity();
		Level level = entity.level();
		if (level.isClientSide |! (entity instanceof LivingEntity && attacker instanceof LivingEntity)) return;
		if (!HordesInfection.canCauseInfection((LivingEntity) attacker) || entity.hasEffect(HordesInfection.INFECTED)) return;
		if (InfectionDataLoader.INSTANCE.canBeInfected(entity))
			InfectionDataLoader.INSTANCE.tryToInfect(entity, (LivingEntity) attacker, event.getSource(), event.getAmount());
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled=true)
	public void onDeath(LivingDeathEvent event) {
		LivingEntity entity = event.getEntity();
		DamageSource source = event.getSource();
		Level level = entity.level();
		if (level.isClientSide || !(source.is(HordesInfection.INFECTION_DAMAGE) || entity.hasEffect(HordesInfection.INFECTED))) return;
		InfectionDeathEvent newevent = new InfectionDeathEvent(entity, event.getSource());
		NeoForge.EVENT_BUS.post(newevent);
		if (newevent.isCanceled()) {
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
		Level level = entity.level();
		if (entity instanceof Player) return;
		if (entity instanceof Villager && level instanceof ServerLevel) {
			Villager villager = (Villager) entity;
			ZombieVillager zombie = villager.convertTo(EntityType.ZOMBIE_VILLAGER, false);
			if (zombie != null) {
				zombie.finalizeSpawn((ServerLevel) level, level.getCurrentDifficultyAt(zombie.blockPosition()), MobSpawnType.CONVERSION,
						new Zombie.ZombieGroupData(false, true));
				zombie.setVillagerData(villager.getVillagerData());
				zombie.setGossips(villager.getGossips().store(NbtOps.INSTANCE));
				zombie.setTradeOffers(villager.getOffers());
				zombie.setVillagerXp(villager.getVillagerXp());
				EventHooks.onLivingConvert(villager, zombie);
			}
			event.setCanceled(true);
		} else if (InfectionDataLoader.INSTANCE.canBeInfected(entity))  {
			if (InfectionDataLoader.INSTANCE.convertEntity((Mob) entity)) return;
			event.setCanceled(true);
		}
	}

	@SubscribeEvent
	public void canApplyEffect(MobEffectEvent.Applicable event) {
		LivingEntity entity = event.getEntity();
		if (entity.level().isClientSide()) return;
		if (event.getEffectInstance().getEffect() == HordesInfection.INFECTED
				&& InfectedEffect.preventInfection(entity)) {
			event.setResult(MobEffectEvent.Applicable.Result.DO_NOT_APPLY);
			if (entity instanceof ServerPlayer) InfectionPacketHandler.sendTo(new InfectMessage(true), (ServerPlayer) entity);
		}
	}

	@SubscribeEvent
	public void applyEffect(MobEffectEvent.Added event) {
		LivingEntity entity = event.getEntity();
		if (entity.level().isClientSide()) return;
		if (event.getEffectInstance().getEffect() == HordesInfection.IMMUNITY.get() && entity.hasEffect(HordesInfection.INFECTED))
			if (entity.removeEffect(HordesInfection.INFECTED)) InfectionPacketHandler.sendTracking(new CureEntityMessage(entity), entity);
	}

}
