package net.smileycorp.hordes.common.infection;

import java.util.Random;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.entity.monster.ZombieVillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
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
import net.minecraftforge.fml.network.NetworkDirection;
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
		PlayerEntity player = event.getPlayer();
		if (player != null) {
			if (player instanceof ServerPlayerEntity) {
				InfectionPacketHandler.NETWORK_INSTANCE.sendTo(new SimpleStringMessage(InfectionRegister.getCurePacketData()), ((ServerPlayerEntity) player).connection.connection, NetworkDirection.PLAY_TO_CLIENT);
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

	/*@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
		PlayerEntity player = event.getEntityPlayer();
		ItemStack stack = event.getItemStack();
		if (event.getTarget() instanceof LivingEntity) {
			LivingEntity entity = (LivingEntity) event.getTarget();
			if (entity.isPotionActive(HordesInfection.INFECTED)) {
				if (InfectionRegister.isCure(stack)) {
					entity.removePotionEffect(HordesInfection.INFECTED);
					if (!player.capabilities.isCreativeMode) {
						ItemStack container = stack.getItem().getContainerItem(stack);
						if (stack.isItemStackDamageable()) {
							stack.damageItem(1, player);
						} else {
							stack.splitStack(1);
						}
						if (stack.isEmpty() && !container.isEmpty()) {
							player.setItemStackToSlot(event.getHand() == EnumHand.OFF_HAND ? EntityEquipmentSlot.OFFHAND : EntityEquipmentSlot.MAINHAND, stack);
						}
					}
					event.setCanceled(true);
					event.setCancellationResult(EnumActionResult.FAIL);
				}
			}
		}
	}*/

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onItemUse(PlayerInteractEvent.RightClickItem event) {
		ItemStack stack = event.getItemStack();
		RayTraceResult ray = DirectionUtils.getPlayerRayTrace(event.getWorld(), event.getPlayer(), 5);
		if (ray instanceof EntityRayTraceResult) {
			if (((EntityRayTraceResult) ray).getEntity() instanceof LivingEntity) {
				LivingEntity entity = (LivingEntity) ((EntityRayTraceResult) ray).getEntity();
				if (entity.hasEffect(HordesInfection.INFECTED.get())) {
					if (InfectionRegister.isCure(stack)) {
						entity.removeEffect(HordesInfection.INFECTED.get());
						event.setCanceled(true);
						event.setCancellationResult(ActionResultType.FAIL);
					}
				}
			}
		}
	}

	@SubscribeEvent
	public void onDamage(LivingDamageEvent event) {
		LivingEntity entity = event.getEntityLiving();
		Entity attacker = event.getSource().getDirectEntity();
		World world = entity.level;
		Random rand = world.random;
		if (!world.isClientSide && InfectionRegister.canCauseInfection(attacker)) {
			if (!entity.hasEffect(HordesInfection.INFECTED.get())) {
				if ((entity instanceof PlayerEntity && CommonConfigHandler.infectPlayers.get())) {
					int c = rand.nextInt(100);
					if (c <= CommonConfigHandler.playerInfectChance.get()) {
						entity.addEffect(new EffectInstance(HordesInfection.INFECTED.get(), 10000, 0));
						//PacketHandler.NETWORK_INSTANCE.sendTo(new DenyFollowMessage(entity), ((ServerPlayerEntity)user).connection.connection, NetworkDirection.PLAY_TO_CLIENT);
						InfectionPacketHandler.NETWORK_INSTANCE.sendTo(new InfectMessage(), ((ServerPlayerEntity) entity).connection.connection, NetworkDirection.PLAY_TO_CLIENT);
					}
				} else if ((entity instanceof VillagerEntity && CommonConfigHandler.infectVillagers.get())) {
					int c = rand.nextInt(100);
					if (c <= CommonConfigHandler.villagerInfectChance.get()) {
						entity.addEffect(new EffectInstance(HordesInfection.INFECTED.get(), 10000, 0));
					}
				}
			}
		}
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled=true)
	public void onDeath(LivingDeathEvent event) {
		LivingEntity entity = event.getEntityLiving();
		DamageSource source = event.getSource();
		World world = entity.level;
		if (!world.isClientSide && (source == HordesInfection.INFECTION_DAMAGE || entity.hasEffect(HordesInfection.INFECTED.get()))) {
			InfectionDeathEvent newevent = new InfectionDeathEvent(entity, event.getSource());
			MinecraftForge.EVENT_BUS.post(newevent);
			if (newevent.getResult() == Result.DENY) event.setCanceled(true);
		}
	}

	@SubscribeEvent
	public void onInfectDeath(InfectionDeathEvent event) {
		LivingEntity entity = event.getEntityLiving();
		World world = entity.level;
		if (entity instanceof VillagerEntity) {
			VillagerEntity villager = (VillagerEntity) entity;
			ZombieVillagerEntity zombie = EntityType.ZOMBIE_VILLAGER.create(world);
			zombie.setVillagerData(villager.getVillagerData());
			zombie.setVillagerXp(villager.getVillagerXp());
			zombie.setPos(entity.getX(), entity.getY(), entity.getZ());
			for (EquipmentSlotType slot : EquipmentSlotType.values()) {
				zombie.setItemSlot(slot, entity.getItemBySlot(slot));
			}
			if (entity.hasCustomName()) {
				zombie.setCustomName(entity.getCustomName());
			}
			world.addFreshEntity(zombie);
			entity.kill();
			event.setResult(Result.DENY);
		}
	}

	@SubscribeEvent
	public static void onTick(LivingUpdateEvent event) {
		LivingEntity entity = event.getEntityLiving();
		World world = entity.level;
		if (!world.isClientSide && entity.hasEffect(HordesInfection.INFECTED.get())) {
			EffectInstance effect = entity.getEffect(HordesInfection.INFECTED.get());
			if (effect.getDuration() < 10000 - CommonConfigHandler.ticksForEffectStage.get()) {
				int a = effect.getAmplifier();
				if (a < 3) {
					entity.addEffect(new EffectInstance(HordesInfection.INFECTED.get(), 10000, a+1));
				} else {
					entity.hurt(HordesInfection.INFECTION_DAMAGE, Float.MAX_VALUE);
				}
			}
		}
	}

}
