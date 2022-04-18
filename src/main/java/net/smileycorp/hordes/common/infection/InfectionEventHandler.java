package net.smileycorp.hordes.common.infection;

import java.util.Random;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.ZombieVillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event.Result;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.network.NetworkDirection;
import net.smileycorp.atlas.api.network.SimpleStringMessage;
import net.smileycorp.atlas.api.util.DirectionUtils;
import net.smileycorp.hordes.common.CommonConfigHandler;
import net.smileycorp.hordes.common.ModDefinitions;
import net.smileycorp.hordes.common.event.InfectionDeathEvent;
import net.smileycorp.hordes.common.infection.network.InfectMessage;
import net.smileycorp.hordes.common.infection.network.InfectionPacketHandler;

@EventBusSubscriber(modid=ModDefinitions.MODID)
public class InfectionEventHandler {

	@SubscribeEvent
	public void playerJoin(PlayerLoggedInEvent event) {
		Player player = event.getPlayer();
		if (player != null) {
			if (player instanceof ServerPlayer) {
				InfectionPacketHandler.NETWORK_INSTANCE.sendTo(new SimpleStringMessage(InfectionRegister.getCurePacketData()), ((ServerPlayer) player).connection.connection, NetworkDirection.PLAY_TO_CLIENT);
			}
		}
	}

	@SubscribeEvent
	public void onItemStackConsume(LivingEntityUseItemEvent.Finish event) {
		LivingEntity entity = event.getEntityLiving();
		ItemStack stack = event.getItem();
		if (entity.hasEffect(HordesInfection.INFECTED.get())) {
			if (InfectionRegister.isCure(stack)) {
				entity.removeEffect(HordesInfection.INFECTED.get());
			}
		}
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onItemUse(PlayerInteractEvent.RightClickItem event) {
		ItemStack stack = event.getItemStack();
		HitResult ray = DirectionUtils.getEntityRayTrace(event.getWorld(), event.getPlayer(), 5);
		if (ray instanceof EntityHitResult) {
			if (((EntityHitResult) ray).getEntity() instanceof LivingEntity) {
				LivingEntity entity = (LivingEntity) ((EntityHitResult) ray).getEntity();
				if (entity.hasEffect(HordesInfection.INFECTED.get())) {
					if (InfectionRegister.isCure(stack)) {
						entity.removeEffect(HordesInfection.INFECTED.get());
						event.setCanceled(true);
						event.setCancellationResult(InteractionResult.FAIL);
					}
				}
			}
		}
	}

	@SubscribeEvent
	public void onDamage(LivingDamageEvent event) {
		LivingEntity entity = event.getEntityLiving();
		Entity attacker = event.getSource().getDirectEntity();
		Level level = entity.level;
		Random rand = level.random;
		if (!level.isClientSide && InfectionRegister.canCauseInfection(attacker)) {
			if (!entity.hasEffect(HordesInfection.INFECTED.get())) {
				if ((entity instanceof Player && CommonConfigHandler.infectPlayers.get())) {
					int c = rand.nextInt(100);
					if (c <= CommonConfigHandler.playerInfectChance.get()) {
						entity.addEffect(new MobEffectInstance(HordesInfection.INFECTED.get(), 10000, 0));
						//PacketHandler.NETWORK_INSTANCE.sendTo(new DenyFollowMessage(entity), ((ServerPlayerEntity)user).connection.connection, NetworkDirection.PLAY_TO_CLIENT);
						InfectionPacketHandler.NETWORK_INSTANCE.sendTo(new InfectMessage(), ((ServerPlayer) entity).connection.connection, NetworkDirection.PLAY_TO_CLIENT);
					}
				} else if ((entity instanceof Villager && CommonConfigHandler.infectVillagers.get())) {
					int c = rand.nextInt(100);
					if (c <= CommonConfigHandler.villagerInfectChance.get()) {
						entity.addEffect(new MobEffectInstance(HordesInfection.INFECTED.get(), 10000, 0));
					}
				}
			}
		}
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled=true)
	public void onDeath(LivingDeathEvent event) {
		LivingEntity entity = event.getEntityLiving();
		DamageSource source = event.getSource();
		Level level = entity.level;
		if (!level.isClientSide && (source == HordesInfection.INFECTION_DAMAGE || entity.hasEffect(HordesInfection.INFECTED.get()))) {
			InfectionDeathEvent newevent = new InfectionDeathEvent(entity, event.getSource());
			MinecraftForge.EVENT_BUS.post(newevent);
			if (newevent.getResult() == Result.DENY) event.setCanceled(true);
		}
	}

	@SubscribeEvent
	public void onInfectDeath(InfectionDeathEvent event) {
		LivingEntity entity = event.getEntityLiving();
		Level level = entity.level;
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
		}
	}

	@SubscribeEvent
	public static void onTick(LivingUpdateEvent event) {
		LivingEntity entity = event.getEntityLiving();
		Level level = entity.level;
		if (!level.isClientSide && entity.hasEffect(HordesInfection.INFECTED.get())) {
			MobEffectInstance effect = entity.getEffect(HordesInfection.INFECTED.get());
			if (effect.getDuration() < 10000 - CommonConfigHandler.ticksForEffectStage.get()) {
				int a = effect.getAmplifier();
				if (a < 3) {
					entity.addEffect(new MobEffectInstance(HordesInfection.INFECTED.get(), 10000, a+1));
				} else {
					entity.hurt(HordesInfection.INFECTION_DAMAGE, Float.MAX_VALUE);
				}
			}
		}
	}

}
