package net.smileycorp.hordes.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.goal.GoalSelector;
import net.minecraft.entity.ai.goal.HurtByTargetGoal;
import net.minecraft.entity.ai.goal.NearestAttackableTargetGoal;
import net.minecraft.entity.ai.goal.PrioritizedGoal;
import net.minecraft.entity.monster.AbstractSkeletonEntity;
import net.minecraft.entity.monster.piglin.PiglinEntity;
import net.minecraft.entity.passive.horse.ZombieHorseEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import net.smileycorp.atlas.api.util.DataUtils;
import net.smileycorp.hordes.common.ai.FleeEntityGoal;
import net.smileycorp.hordes.common.capability.HordesCapabilities;
import net.smileycorp.hordes.config.CommonConfigHandler;
import net.smileycorp.hordes.hordeevent.capability.HordeEvent;
import net.smileycorp.hordes.hordeevent.capability.HordeSavedData;
import net.smileycorp.hordes.hordeevent.capability.HordeSpawn;
import net.smileycorp.hordes.infection.HordesInfection;
import net.smileycorp.hordes.infection.network.CureEntityMessage;
import net.smileycorp.hordes.infection.network.InfectionPacketHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

@Mixin(MobEntity.class)
public abstract class MixinMobEntity extends LivingEntity {
	
	@Shadow @Final public GoalSelector goalSelector;
	
	public MixinMobEntity(World level) {
		super(null, level);
	}
	
	//apply infection curing before any other interactions are handled
	@Inject(at=@At("HEAD"), method = "checkAndHandleImportantInteractions", cancellable = true)
	public void checkAndHandleImportantInteractions(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResultType> callback) {
		ItemStack stack = player.getItemInHand(hand);
		if (!hasEffect(HordesInfection.INFECTED.get())) return;
		if (!HordesInfection.isCure(stack)) return;
		removeEffect(HordesInfection.INFECTED.get());
		if (!player.level.isClientSide) InfectionPacketHandler.send(
				PacketDistributor.TRACKING_CHUNK.with(() -> player.level.getChunkAt(getOnPos())),
				new CureEntityMessage(this));
		if (!player.isCreative()) {
			ItemStack container = stack.getItem().getCraftingRemainingItem().getDefaultInstance();
			if (stack.isDamageableItem() && player instanceof ServerPlayerEntity) {
				stack.hurt(1, player.level.random, (ServerPlayerEntity) player);
			} else {
				stack.shrink(1);
			}
			if (stack.isEmpty() && !container.isEmpty()) {
				player.setItemInHand(hand, container);
			}
		}
		callback.setReturnValue(ActionResultType.sidedSuccess(player.level.isClientSide));
	}
	
	//disables skeletons burning based on the config
	@Inject(at=@At("HEAD"), method = "isSunBurnTick", cancellable = true)
	public void isSunBurnTick(CallbackInfoReturnable<Boolean> callback) {
		if ((LivingEntity)this instanceof AbstractSkeletonEntity &! CommonConfigHandler.skeletonsBurn.get()) callback.setReturnValue(false);
	}
	
	//despawns zombie horses in peaceful if they are set as aggressive in the config
	@Inject(at=@At("HEAD"), method = "shouldDespawnInPeaceful", cancellable = true)
	public void shouldDespawnInPeaceful(CallbackInfoReturnable<Boolean> callback) {
		if ((LivingEntity)this instanceof ZombieHorseEntity && CommonConfigHandler.aggressiveZombieHorses.get()) callback.setReturnValue(true);
	}
	
	//copy horde data to converted entities after conversion before capabilities are cleared
	@WrapOperation(method = "convertTo", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/EntityType;create(Lnet/minecraft/world/World;)Lnet/minecraft/entity/Entity;"))
	private Entity convertTo(EntityType instance, World level, Operation<Entity> original) {
		Entity entity = original.call(instance, level);
		if (!(entity instanceof MobEntity)) return entity;
		MobEntity converted = (MobEntity) entity;
		LazyOptional<HordeSpawn> beforeOptional = getCapability(HordesCapabilities.HORDESPAWN);
		LazyOptional<HordeSpawn> afterOptional = converted.getCapability(HordesCapabilities.HORDESPAWN);
		if (!(beforeOptional.isPresent() || afterOptional.isPresent())) return converted;
		if (!beforeOptional.orElseGet(null).isHordeSpawned()) return converted;
		String uuid = beforeOptional.orElseGet(null).getPlayerUUID();
		if (!DataUtils.isValidUUID(uuid)) return converted;
		afterOptional.orElseGet(null).setPlayerUUID(uuid);
		beforeOptional.orElseGet(null).setPlayerUUID("");
		HordeEvent horde = HordeSavedData.getData((ServerWorld) level).getEvent(UUID.fromString(uuid));
		if (horde != null) {
			ServerPlayerEntity player = ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayer(UUID.fromString(uuid));
			horde.registerEntity(converted, player);
			horde.removeEntity((MobEntity) (LivingEntity) this);
			converted.targetSelector.getRunningGoals().forEach(PrioritizedGoal::stop);
			if (converted instanceof CreatureEntity) converted.targetSelector.addGoal(1, new HurtByTargetGoal((CreatureEntity) converted));
			converted.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(converted, PlayerEntity.class, true));
		}
		return converted;
	}
	
	//add horde ai to converted mobs
	@Inject(at=@At("TAIL"), method = "convertTo", cancellable = true)
	public void convertTo(EntityType<?> type, boolean keepEquipment, CallbackInfoReturnable<MobEntity> callback) {
		MobEntity converted = callback.getReturnValue();
		LazyOptional<HordeSpawn> optional = converted.getCapability(HordesCapabilities.HORDESPAWN);
		if (!optional.isPresent()) return;
		if (!optional.orElseGet(null).isHordeSpawned()) return;
		String uuid = optional.orElseGet(null).getPlayerUUID();
		if (DataUtils.isValidUUID(uuid)) {
			ServerPlayerEntity player = ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayer(UUID.fromString(uuid));
			if (player == null) return;
			HordeEvent horde = HordeSavedData.getData((ServerWorld) level).getEvent(player);
			if (horde == null) return;
			if (!horde.isActive(player)) return;
			horde.registerEntity((MobEntity) (LivingEntity)this, player);
		}
	}
	
	@Inject(at=@At("HEAD"), method = "registerGoals", cancellable = true)
	public void registerGoals(CallbackInfo callback) {
		if (CommonConfigHandler.piglinsHuntZombies.get() && ((LivingEntity)this) instanceof PiglinEntity)
			goalSelector.addGoal(1, new FleeEntityGoal((MobEntity) (LivingEntity)this, 1.5, 5, HordesInfection::canCauseInfection));
	}
	
	@Inject(at = @At("HEAD"), method = "canBeLeashed")
	public void canBeLeashed(PlayerEntity player, CallbackInfoReturnable<Boolean> callback) {
		if (((LivingEntity)this) instanceof ZombieHorseEntity && CommonConfigHandler.aggressiveZombieHorses.get()) callback.setReturnValue(false);
	}
}
