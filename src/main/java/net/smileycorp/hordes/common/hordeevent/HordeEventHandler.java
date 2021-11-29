package net.smileycorp.hordes.common.hordeevent;

import java.util.UUID;

import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.HurtByTargetGoal;
import net.minecraft.entity.ai.goal.NearestAttackableTargetGoal;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerEntity.SleepResult;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.event.TickEvent.ServerTickEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.eventbus.api.Event.Result;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import net.smileycorp.atlas.api.entity.ai.GoToEntityPositionGoal;
import net.smileycorp.atlas.api.util.DataUtils;
import net.smileycorp.hordes.common.CommonConfigHandler;
import net.smileycorp.hordes.common.Hordes;
import net.smileycorp.hordes.common.ModDefinitions;
import net.smileycorp.hordes.common.hordeevent.capability.HordeWorldData;
import net.smileycorp.hordes.common.hordeevent.capability.IHordeSpawn;
import net.smileycorp.hordes.common.hordeevent.capability.IOngoingHordeEvent;

@EventBusSubscriber(modid=ModDefinitions.MODID)
public class HordeEventHandler {


	@SubscribeEvent
	public void serverTick(ServerTickEvent event) {
		if (event.phase == Phase.END) {
			MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
			ServerWorld world = server.overworld();
			if ((world.getGameRules().getBoolean(GameRules.RULE_DAYLIGHT) || !CommonConfigHandler.pauseEventServer.get())) {
				int day = (int) Math.floor(world.getGameTime()/CommonConfigHandler.dayLength.get());
				int time = Math.round(world.getGameTime()%CommonConfigHandler.dayLength.get());
				HordeWorldData data = HordeWorldData.getData(world);
				if (((time >= CommonConfigHandler.hordeStartTime.get() && day == data.getNextDay()) || day > data.getNextDay())) {
					data.setNextDay(world.random.nextInt(CommonConfigHandler.hordeSpawnVariation.get() + 1) + CommonConfigHandler.hordeSpawnDays.get() + data.getNextDay());
				}
				data.setDirty();
			}
		}
	}

	@SubscribeEvent
	public void playerTick(PlayerTickEvent event) {
		PlayerEntity player = event.player;
		if (event.phase == Phase.END && player != null && !(player instanceof FakePlayer)) {
			World world = player.level;
			if (!world.isClientSide && (world.getGameRules().getBoolean(GameRules.RULE_DAYLIGHT) || !CommonConfigHandler.pauseEventServer.get())) {
				LazyOptional<IOngoingHordeEvent> optional = player.getCapability(Hordes.HORDE_EVENT, null);
				if (optional.isPresent()) {
					IOngoingHordeEvent horde = optional.resolve().get();
					int day = (int) Math.floor(world.getDayTime() / CommonConfigHandler.dayLength.get());
					int time = Math.round(world.getDayTime() % CommonConfigHandler.dayLength.get());
					if (horde != null && !horde.isActive(player)) {
						if (time >= CommonConfigHandler.hordeStartTime.get() && day >= horde.getNextDay() && (day!=0 || CommonConfigHandler.spawnFirstDay.get())) {
							horde.tryStartEvent(player, CommonConfigHandler.hordeSpawnDuration.get(), false);
						}
					}
					if (horde.isActive(player)) {
						horde.update(player);
					}
				}
			}
		}
	}

	@SubscribeEvent
	public void tryDespawn(LivingSpawnEvent.AllowDespawn event) {
		LivingEntity entity = event.getEntityLiving();
		LazyOptional<IHordeSpawn> optional = entity.getCapability(Hordes.HORDESPAWN, null);
		if (optional.isPresent()) {
			IHordeSpawn cap = optional.resolve().get();
			if (cap.isHordeSpawned()) {
				String uuid = cap.getPlayerUUID();
				if (DataUtils.isValidUUID(uuid)) {
					PlayerEntity player = ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayer(UUID.fromString(uuid));
					LazyOptional<IOngoingHordeEvent> optionalp = player.getCapability(Hordes.HORDE_EVENT, null);
					if (optionalp.isPresent()) {
						if (optionalp.resolve().get().isActive(player)) {
							event.setResult(Result.DENY);
						}
					}
				}
			}
		}
	}

	@SubscribeEvent(priority=EventPriority.LOWEST)
	public void update(LivingUpdateEvent event) {
		World world = event.getEntity().level;
		if (!world.isClientSide && event.getEntity() instanceof MobEntity && world.dimension() == World.OVERWORLD && event.getEntity().tickCount%5==0) {
			MobEntity entity = (MobEntity) event.getEntity();
			LazyOptional<IHordeSpawn> optional = entity.getCapability(Hordes.HORDESPAWN, null);
			if (optional.isPresent()) {
				IHordeSpawn cap = optional.resolve().get();
				if (cap.isHordeSpawned() &! cap.isSynced()) {
					String uuid = cap.getPlayerUUID();
					if (DataUtils.isValidUUID(uuid)) {
						PlayerEntity player = ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayer(UUID.fromString(uuid));
						if (player!=null) {
							entity.targetSelector.getRunningGoals().forEach((goal) -> goal.stop());
							if (entity instanceof CreatureEntity) {
								entity.targetSelector.addGoal(1, new HurtByTargetGoal((CreatureEntity) entity));
							}
							entity.targetSelector.addGoal(2, new NearestAttackableTargetGoal<PlayerEntity>(entity, PlayerEntity.class, true));
							LazyOptional<IOngoingHordeEvent> optionalp = player.getCapability(Hordes.HORDE_EVENT, null);
							if (optionalp.isPresent()) {
								optionalp.resolve().get().registerEntity(entity);
								entity.goalSelector.addGoal(6, new GoToEntityPositionGoal(entity, player));
							}
							cap.setSynced();
						}
					}
				}
			}
		}
	}

	@SubscribeEvent
	public void playerJoin(PlayerLoggedInEvent event) {
		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		if (CommonConfigHandler.pauseEventServer.get()) {
			server.getAllLevels().forEach( (world) -> {
				if (world.getGameRules().getBoolean(GameRules.RULE_DAYLIGHT) == false) {
					world.getGameRules().getRule(GameRules.RULE_DAYLIGHT).set(true, server);
				}
			});
		}
	}

	@SubscribeEvent
	public void playerLeave(PlayerLoggedOutEvent event) {
		if (CommonConfigHandler.pauseEventServer.get()) {
			MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
			if (server.getPlayerCount() == 0) {
				server.getAllLevels().forEach( (world) -> {
					if (world.getGameRules().getBoolean(GameRules.RULE_DAYLIGHT) == true) {
						world.getGameRules().getRule(GameRules.RULE_DAYLIGHT).set(false, server);
					}
				});
			}
		}
	}

	@SubscribeEvent
	public void attachCapabilities(AttachCapabilitiesEvent<Entity> event) {
		Entity entity = event.getObject();
		if (entity instanceof MobEntity && !(entity instanceof PlayerEntity)) {
			event.addCapability(ModDefinitions.getResource("HordeSpawn"), new IHordeSpawn.Provider());
		}
		if (entity instanceof PlayerEntity && !(entity instanceof FakePlayer)) {
			event.addCapability(ModDefinitions.getResource("HordeEvent"), new IOngoingHordeEvent.Provider());
		}
	}

	@SubscribeEvent
	public void trySleep(PlayerSleepInBedEvent event) {
		PlayerEntity player = event.getPlayer();
		World world = player.level;
		if (!CommonConfigHandler.canSleepDuringHorde.get()) {
			if (!world.isClientSide) {
				LazyOptional<IOngoingHordeEvent> optional = player.getCapability(Hordes.HORDE_EVENT, null);
				if (optional.isPresent()) {
					IOngoingHordeEvent horde = optional.resolve().get();
					if ((horde.isHordeDay(player) && world.dimensionType().bedWorks() &! world.isDay()) || horde.isActive(player)) {
						event.setResult(SleepResult.OTHER_PROBLEM);
						player.displayClientMessage(new TranslationTextComponent(ModDefinitions.hordeTrySleep), true);
					}
				}
			}
		}
	}

	@SubscribeEvent(receiveCanceled = true)
	public void playerClone(PlayerEvent.Clone event) {
		PlayerEntity player = event.getPlayer();
		PlayerEntity original = event.getOriginal();
		if (player != null && original != null &!(player instanceof FakePlayer || original instanceof FakePlayer)) {
			LazyOptional<IOngoingHordeEvent> optionalp = player.getCapability(Hordes.HORDE_EVENT, null);
			LazyOptional<IOngoingHordeEvent> optionalo = original.getCapability(Hordes.HORDE_EVENT, null);
			if (optionalp.isPresent() && optionalo.isPresent()) {
				Hordes.logInfo("Copying horde data from " + original + " to " + player);
				IOngoingHordeEvent horde = optionalp.resolve().get();
				CompoundNBT nbt = optionalo.resolve().get().writeToNBT(new CompoundNBT());
				Hordes.logInfo(nbt);
				horde.readFromNBT(nbt);
				horde.setPlayer(player);
			}
		}
	}
}
