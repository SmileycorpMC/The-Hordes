package net.smileycorp.hordes.infection;

import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.TranslatableComponent;
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
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.living.PotionEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event.Result;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.PacketDistributor;
import net.smileycorp.atlas.api.util.DirectionUtils;
import net.smileycorp.hordes.common.Constants;
import net.smileycorp.hordes.common.capability.HordesCapabilities;
import net.smileycorp.hordes.common.event.InfectionDeathEvent;
import net.smileycorp.hordes.config.InfectionConfig;
import net.smileycorp.hordes.infection.capability.Infection;
import net.smileycorp.hordes.infection.data.InfectionDataLoader;
import net.smileycorp.hordes.infection.network.CureEntityMessage;
import net.smileycorp.hordes.infection.network.InfectMessage;
import net.smileycorp.hordes.infection.network.InfectionPacketHandler;

public class InfectionEventHandler {

	//attach required entity capabilities for event to function
	@SubscribeEvent
	public void attachCapabilities(AttachCapabilitiesEvent<Entity> event) {
		Entity entity = event.getObject();
		if (entity instanceof Player && !(entity instanceof FakePlayer) || entity instanceof Villager || InfectionDataLoader.INSTANCE.canBeInfected(entity)) {
			event.addCapability(Constants.loc("InfectionCounter"), new Infection.Provider());
		}
	}

	//register data listeners
	@SubscribeEvent
	public void addResourceReload(AddReloadListenerEvent event) {
		event.addListener(InfectionDataLoader.INSTANCE);
	}

	@SubscribeEvent
	public void onEntityAdded(EntityJoinWorldEvent event) {
		Entity entity = event.getEntity();
		if (!(entity instanceof Mob && InfectionConfig.infectionEntitiesAggroConversions.get()) || entity.level.isClientSide) return;
		if (HordesInfection.canCauseInfection((LivingEntity) entity)) {
			((Mob) entity).targetSelector.addGoal(3, new NearestAttackableTargetGoal<>((Mob) entity, LivingEntity.class,
					10, true, false, InfectionDataLoader.INSTANCE::canBeInfected));
		}
	}
	
	@SubscribeEvent
	public void logIn(PlayerEvent.PlayerLoggedInEvent event) {
		if (event.getEntity() instanceof ServerPlayer)
			InfectionDataLoader.INSTANCE.syncData(((ServerPlayer)event.getEntity()).connection.connection);
	}

	@SubscribeEvent
	public void onItemStackConsume(LivingEntityUseItemEvent.Finish event) {
		LivingEntity entity = event.getEntityLiving();
		ItemStack stack = event.getItem();
		if (InfectionDataLoader.INSTANCE.applyImmunity(entity, stack.getItem())) return;
		if (!(entity.hasEffect(HordesInfection.INFECTED.get()) && HordesInfection.isCure(stack))) return;
		LazyOptional<Infection> optional = entity.getCapability(HordesCapabilities.INFECTION);
		if (optional.isPresent()) optional.orElseGet(null).increaseInfection();
		if (!entity.removeEffect(HordesInfection.INFECTED.get())) return;
		if (entity.level.isClientSide) return;
		InfectionPacketHandler.send(PacketDistributor.TRACKING_CHUNK.with(()-> entity.level.getChunkAt(entity.getOnPos())),
				new CureEntityMessage(entity));
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onItemUse(PlayerInteractEvent.RightClickItem event) {
		ItemStack stack = event.getItemStack();
		HitResult ray = DirectionUtils.getEntityRayTrace(event.getWorld(), event.getEntity(), 5);
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
		LivingEntity entity = event.getEntityLiving();
		Entity attacker = event.getSource().getDirectEntity();
		Level level = entity.level;
		if (level.isClientSide |! (entity instanceof LivingEntity && attacker instanceof LivingEntity)) return;
		if (!HordesInfection.canCauseInfection((LivingEntity) attacker) || entity.hasEffect(HordesInfection.INFECTED.get())) return;
		if (InfectionDataLoader.INSTANCE.canBeInfected(entity))
			InfectionDataLoader.INSTANCE.tryToInfect(entity, (LivingEntity) attacker, event.getSource(), event.getAmount());
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled=true)
	public void onDeath(LivingDeathEvent event) {
		LivingEntity entity = event.getEntityLiving();
		DamageSource source = event.getSource();
		Level level = entity.level;
		if (level.isClientSide || !(source == HordesInfection.INFECTION_DAMAGE || entity.hasEffect(HordesInfection.INFECTED.get()))) return;
		InfectionDeathEvent newevent = new InfectionDeathEvent(entity, event.getSource());
		MinecraftForge.EVENT_BUS.post(newevent);
		if (newevent.getResult() == Result.DENY) {
			event.setCanceled(true);
			if (!(entity instanceof OwnableEntity)) return;
			Entity owner = ((OwnableEntity) entity).getOwner();
			if (!(owner instanceof ServerPlayer)) return;
			owner.sendMessage(new TranslatableComponent("death.attack.infection.zombified", entity.getDisplayName()), null);
		}
	}

	@SubscribeEvent
	public void onInfectDeath(InfectionDeathEvent event) {
		LivingEntity entity = event.getEntityLiving();
		Level level = entity.level;
		if (entity instanceof Player) return;
		if (entity instanceof Villager && level instanceof ServerLevel) {
			Villager villager = (Villager) entity;
			ZombieVillager zombie = villager.convertTo(EntityType.ZOMBIE_VILLAGER, false);
			if (zombie != null) {
				zombie.finalizeSpawn((ServerLevel) level, level.getCurrentDifficultyAt(zombie.blockPosition()), MobSpawnType.CONVERSION,
						new Zombie.ZombieGroupData(false, true), null);
				zombie.setVillagerData(villager.getVillagerData());
				zombie.setGossips(villager.getGossips().store(NbtOps.INSTANCE).getValue());
				zombie.setTradeOffers(villager.getOffers().createTag());
				zombie.setVillagerXp(villager.getVillagerXp());
				ForgeEventFactory.onLivingConvert(villager, zombie);
			}
			event.setResult(Result.DENY);
		} else if (InfectionDataLoader.INSTANCE.canBeInfected(entity))  {
			if (InfectionDataLoader.INSTANCE.convertEntity((Mob) entity)) return;
			event.setResult(Result.DENY);
		}
	}

	@SubscribeEvent
	public void canApplyEffect(PotionEvent.PotionApplicableEvent event) {
		LivingEntity entity = event.getEntityLiving();
		if (entity.level.isClientSide()) return;
		if (event.getPotionEffect().getEffect() == HordesInfection.INFECTED.get()
				&& InfectedEffect.preventInfection(entity)) {
			event.setResult(Result.DENY);
			if (entity instanceof ServerPlayer) InfectionPacketHandler.sendTo(new InfectMessage(true),
					((ServerPlayer) entity).connection.connection, NetworkDirection.PLAY_TO_CLIENT);
		}
	}

	@SubscribeEvent
	public void applyEffect(PotionEvent.PotionAddedEvent event) {
		LivingEntity entity = event.getEntityLiving();
		if (entity.level.isClientSide()) return;
		if (event.getPotionEffect().getEffect() == HordesInfection.IMMUNITY.get() && entity.hasEffect(HordesInfection.INFECTED.get())) {
			if (entity.removeEffect(HordesInfection.INFECTED.get()))
				InfectionPacketHandler.send(PacketDistributor.TRACKING_CHUNK.with(()->entity.level.getChunkAt(entity.getOnPos())),
					new CureEntityMessage(entity));
		}
	}

}
