package net.smileycorp.hordes.infection;

import net.minecraft.entity.*;
import net.minecraft.entity.ai.goal.NearestAttackableTargetGoal;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.entity.monster.ZombieEntity;
import net.minecraft.entity.monster.ZombieVillagerEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTDynamicOps;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
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
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.PacketDistributor;
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
		if (entity instanceof PlayerEntity && !(entity instanceof FakePlayer) || entity instanceof VillagerEntity || InfectionDataLoader.INSTANCE.canBeInfected(entity)) {
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
		if (!(entity instanceof MobEntity && InfectionConfig.infectionEntitiesAggroConversions.get()) || entity.level.isClientSide) return;
		if (HordesInfection.canCauseInfection((LivingEntity) entity)) {
			((MobEntity) entity).targetSelector.addGoal(3, new NearestAttackableTargetGoal<>((MobEntity) entity, LivingEntity.class,
					10, true, false, InfectionDataLoader.INSTANCE::canBeInfected));
		}
	}
	
	@SubscribeEvent
	public void logIn(PlayerEvent.PlayerLoggedInEvent event) {
		if (event.getEntity() instanceof ServerPlayerEntity)
			InfectionDataLoader.INSTANCE.syncData(((ServerPlayerEntity)event.getEntity()).connection.connection);
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
		RayTraceResult ray = DirectionUtils.getPlayerRayTrace(event.getWorld(), event.getPlayer(), 5);
		if (!(ray.getType() == RayTraceResult.Type.ENTITY)) return;
		if (!(((EntityRayTraceResult) ray).getEntity() instanceof LivingEntity)) return;
		LivingEntity entity = (LivingEntity) ((EntityRayTraceResult) ray).getEntity();
		if (entity instanceof PlayerEntity |!(entity.hasEffect(HordesInfection.INFECTED.get()) || HordesInfection.isCure(stack))) return;
		entity.removeEffect(HordesInfection.INFECTED.get());
		LazyOptional<Infection> optional = entity.getCapability(HordesCapabilities.INFECTION);
		if (optional.isPresent()) optional.orElseGet(null).increaseInfection();
		event.setCanceled(true);
		event.setCancellationResult(ActionResultType.FAIL);
	}

	@SubscribeEvent
	public void onDamage(LivingDamageEvent event) {
		LivingEntity entity = event.getEntityLiving();
		Entity attacker = event.getSource().getDirectEntity();
		World level = entity.level;
		if (level.isClientSide |! (entity instanceof LivingEntity && attacker instanceof LivingEntity)) return;
		if (!HordesInfection.canCauseInfection((LivingEntity) attacker) || entity.hasEffect(HordesInfection.INFECTED.get())) return;
		if (InfectionDataLoader.INSTANCE.canBeInfected(entity))
			InfectionDataLoader.INSTANCE.tryToInfect(entity, (LivingEntity) attacker, event.getSource(), event.getAmount());
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled=true)
	public void onDeath(LivingDeathEvent event) {
		LivingEntity entity = event.getEntityLiving();
		DamageSource source = event.getSource();
		World level = entity.level;
		if (level.isClientSide || !(source == HordesInfection.INFECTION_DAMAGE || entity.hasEffect(HordesInfection.INFECTED.get()))) return;
		InfectionDeathEvent newevent = new InfectionDeathEvent(entity, event.getSource());
		MinecraftForge.EVENT_BUS.post(newevent);
		if (newevent.getResult() == Result.DENY) {
			event.setCanceled(true);
			if (!(entity instanceof TameableEntity)) return;
			LivingEntity owner = ((TameableEntity) entity).getOwner();
			if (!(owner instanceof ServerPlayerEntity)) return;
			owner.sendMessage(new TranslationTextComponent("death.attack.infection.zombified", entity.getDisplayName()), null);
		}
	}

	@SubscribeEvent
	public void onInfectDeath(InfectionDeathEvent event) {
		LivingEntity entity = event.getEntityLiving();
		World level = entity.level;
		if (entity instanceof PlayerEntity) return;
		if (entity instanceof VillagerEntity && level instanceof ServerWorld) {
			VillagerEntity villager = (VillagerEntity) entity;
			ZombieVillagerEntity zombie = villager.convertTo(EntityType.ZOMBIE_VILLAGER, false);
			if (zombie != null) {
				zombie.finalizeSpawn((ServerWorld) level, level.getCurrentDifficultyAt(zombie.blockPosition()), SpawnReason.CONVERSION,
						new ZombieEntity.GroupData(false, true), null);
				zombie.setVillagerData(villager.getVillagerData());
				zombie.setGossips(villager.getGossips().store(NBTDynamicOps.INSTANCE.INSTANCE).getValue());
				zombie.setTradeOffers(villager.getOffers().createTag());
				zombie.setVillagerXp(villager.getVillagerXp());
				ForgeEventFactory.onLivingConvert(villager, zombie);
			}
			event.setResult(Result.DENY);
		} else if (InfectionDataLoader.INSTANCE.canBeInfected(entity))  {
			if (InfectionDataLoader.INSTANCE.convertEntity((MobEntity) entity)) return;
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
			if (entity instanceof ServerPlayerEntity) InfectionPacketHandler.sendTo(new InfectMessage(true),
					((ServerPlayerEntity) entity).connection.connection, NetworkDirection.PLAY_TO_CLIENT);
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
