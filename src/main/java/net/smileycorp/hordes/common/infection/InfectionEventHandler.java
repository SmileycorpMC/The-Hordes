package net.smileycorp.hordes.common.infection;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
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
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event.Result;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.PacketDistributor;
import net.smileycorp.atlas.api.util.DirectionUtils;
import net.smileycorp.hordes.common.CommonConfigHandler;
import net.smileycorp.hordes.common.Constants;
import net.smileycorp.hordes.common.Hordes;
import net.smileycorp.hordes.common.event.InfectionDeathEvent;
import net.smileycorp.hordes.common.infection.capability.IInfection;
import net.smileycorp.hordes.common.infection.data.InfectionConversionLoader;
import net.smileycorp.hordes.common.infection.network.CureEntityMessage;
import net.smileycorp.hordes.common.infection.network.InfectMessage;
import net.smileycorp.hordes.common.infection.network.InfectionPacketHandler;

@EventBusSubscriber(modid=Constants.MODID)
public class InfectionEventHandler {

	//attach required entity capabilities for event to function
	@SubscribeEvent
	public void attachCapabilities(AttachCapabilitiesEvent<Entity> event) {
		Entity entity = event.getObject();
		if (entity instanceof Player && !(entity instanceof FakePlayer) || entity instanceof Villager || InfectionConversionLoader.INSTANCE.canBeInfected(entity)) {
			event.addCapability(Constants.loc("InfectionCounter"), new IInfection.Provider());
		}
	}

	//register data listeners
	@SubscribeEvent
	public void addResourceReload(AddReloadListenerEvent event ) {
		event.addListener(InfectionConversionLoader.INSTANCE);
	}

	@SubscribeEvent
	public void onEntityAdded(EntityJoinLevelEvent event) {
		Entity entity = event.getEntity();
		if (!(entity instanceof Mob && CommonConfigHandler.infectionEntitiesAggroConversions.get()) || entity.level().isClientSide) return;
		if (HordesInfection.canCauseInfection((LivingEntity) entity)) {
			((Mob) entity).targetSelector.addGoal(3, new NearestAttackableTargetGoal<>((Mob) entity, LivingEntity.class,
					10, true, false, InfectionConversionLoader.INSTANCE::canBeInfected));
		}
	}

	@SubscribeEvent
	public void onItemStackConsume(LivingEntityUseItemEvent.Finish event) {
		LivingEntity entity = event.getEntity();
		ItemStack stack = event.getItem();
		if (!(entity.hasEffect(HordesInfection.INFECTED.get()) || HordesInfection.isCure(stack))) return;
		LazyOptional<IInfection> optional = entity.getCapability(Hordes.INFECTION);
		if (optional.isPresent()) optional.resolve().get().increaseInfection();
		entity.removeEffect(HordesInfection.INFECTED.get());
		InfectionPacketHandler.NETWORK_INSTANCE.send(PacketDistributor.TRACKING_CHUNK.with(()->entity.level().getChunkAt(entity.getOnPos())),
				new CureEntityMessage(entity));
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onItemUse(PlayerInteractEvent.RightClickItem event) {
		ItemStack stack = event.getItemStack();
		HitResult ray = DirectionUtils.getEntityRayTrace(event.getLevel(), event.getEntity(), 5);
		if (!(ray instanceof EntityHitResult && ((EntityHitResult) ray).getEntity() instanceof LivingEntity)) return;
		LivingEntity entity = (LivingEntity) ((EntityHitResult) ray).getEntity();
		if (!(entity.hasEffect(HordesInfection.INFECTED.get()) || HordesInfection.isCure(stack))) return;
		entity.removeEffect(HordesInfection.INFECTED.get());
		LazyOptional<IInfection> optional = entity.getCapability(Hordes.INFECTION);
		if (optional.isPresent()) optional.resolve().get().increaseInfection();
		event.setCanceled(true);
		event.setCancellationResult(InteractionResult.FAIL);
	}

	@SubscribeEvent
	public void onDamage(LivingDamageEvent event) {
		LivingEntity entity = event.getEntity();
		Entity attacker = event.getSource().getDirectEntity();
		Level level = entity.level();
		RandomSource rand = level.random;
		if (level.isClientSide |! (entity instanceof LivingEntity && attacker instanceof LivingEntity)) return;
		if (!HordesInfection.canCauseInfection((LivingEntity) attacker) || entity.hasEffect(HordesInfection.INFECTED.get())) return;
		if ((entity instanceof Player && CommonConfigHandler.infectPlayers.get())) {
			if (rand.nextFloat() <= CommonConfigHandler.playerInfectChance.get()) {
				InfectedEffect.apply(entity);
				InfectionPacketHandler.NETWORK_INSTANCE.sendTo(new InfectMessage(), ((ServerPlayer) entity).connection.connection, NetworkDirection.PLAY_TO_CLIENT);
			}
		} else if ((entity instanceof Villager && CommonConfigHandler.infectVillagers.get())) {
			if (rand.nextFloat() <= CommonConfigHandler.villagerInfectChance.get()) {
				InfectedEffect.apply(entity);
			}
		} else if (InfectionConversionLoader.INSTANCE.canBeInfected(entity)) {
			InfectionConversionLoader.INSTANCE.tryToInfect(entity);
		}
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled=true)
	public void onDeath(LivingDeathEvent event) {
		LivingEntity entity = event.getEntity();
		DamageSource source = event.getSource();
		Level level = entity.level();
		if (!level.isClientSide && (source.is(HordesInfection.INFECTION_DAMAGE) || entity.hasEffect(HordesInfection.INFECTED.get()))) {
			InfectionDeathEvent newevent = new InfectionDeathEvent(entity, event.getSource());
			MinecraftForge.EVENT_BUS.post(newevent);
			if (newevent.getResult() == Result.DENY) event.setCanceled(true);
		}
	}

	@SubscribeEvent
	public void onInfectDeath(InfectionDeathEvent event) {
		LivingEntity entity = event.getEntity();
		Level level = entity.level();
		if (entity instanceof Villager) {
			Villager villager = (Villager) entity;
			ZombieVillager zombie = EntityType.ZOMBIE_VILLAGER.create(level);
			zombie.setVillagerData(villager.getVillagerData());
			zombie.setVillagerXp(villager.getVillagerXp());
			zombie.setPos(entity.getX(), entity.getY(), entity.getZ());
			for (EquipmentSlot slot : EquipmentSlot.values()) {
				zombie.setItemSlot(slot, entity.getItemBySlot(slot));
			}
			if (entity.hasCustomName()) {
				zombie.setCustomName(entity.getCustomName());
			}
			level.addFreshEntity(zombie);
			entity.kill();
			event.setResult(Result.DENY);
		} else if (InfectionConversionLoader.INSTANCE.canBeInfected(entity))  {
			InfectionConversionLoader.INSTANCE.convertEntity(entity);
			event.setResult(Result.DENY);
		}
	}

}
