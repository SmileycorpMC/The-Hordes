package net.smileycorp.hordes.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.horse.ZombieHorse;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.server.ServerLifecycleHooks;
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
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

@Mixin(Mob.class)
public abstract class MixinMob extends LivingEntity {

	@Shadow
	public GoalSelector goalSelector;

	public MixinMob(Level level) {
		super(null, level);
	}

	//apply infection curing before any other interactions are handled
	@Inject(at=@At("HEAD"), method = "checkAndHandleImportantInteractions", cancellable = true)
	public void checkAndHandleImportantInteractions(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> callback) {
		ItemStack stack = player.getItemInHand(hand);
		if (!hasEffect(HordesInfection.INFECTED.get())) return;
		if (!HordesInfection.isCure(stack)) return;
		removeEffect(HordesInfection.INFECTED.get());
		if (!player.level().isClientSide) InfectionPacketHandler.send(
				PacketDistributor.TRACKING_CHUNK.with(() -> player.level().getChunkAt(getOnPos())),
				new CureEntityMessage(this));
		if (!player.isCreative()) {
			ItemStack container = stack.getItem().getCraftingRemainingItem(stack);
			if (stack.isDamageableItem() && player instanceof ServerPlayer) {
				stack.hurt(1, player.level().random, (ServerPlayer) player);
			} else {
				stack.shrink(1);
			}
			if (stack.isEmpty() && !container.isEmpty()) {
				player.setItemInHand(hand, container);
			}
		}
		callback.setReturnValue(InteractionResult.sidedSuccess(player.level().isClientSide));
		callback.cancel();
	}

	//disables skeletons burning based on the config
	@Inject(at=@At("HEAD"), method = "isSunBurnTick", cancellable = true)
	public void isSunBurnTick(CallbackInfoReturnable<Boolean> callback) {
		if ((LivingEntity)this instanceof AbstractSkeleton &! CommonConfigHandler.skeletonsBurn.get()) {
			callback.setReturnValue(false);
			callback.cancel();
		}
	}

	//despawns zombie horses in peaceful if they are set as aggressive in the config
	@Inject(at=@At("HEAD"), method = "shouldDespawnInPeaceful", cancellable = true)
	public void shouldDespawnInPeaceful(CallbackInfoReturnable<Boolean> callback) {
		if ((LivingEntity)this instanceof ZombieHorse && CommonConfigHandler.aggressiveZombieHorses.get()) {
			callback.setReturnValue(true);
			callback.cancel();
		}
	}

	//copy horde data to converted entities after conversion before capabilities are cleared
	@WrapOperation(method = "convertTo", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/EntityType;create(Lnet/minecraft/world/level/Level;)Lnet/minecraft/world/entity/Entity;"))
	private Entity convertTo(EntityType instance, Level level, Operation<Entity> original) {
		Entity entity = original.call(instance, level);
		if (!(entity instanceof Mob)) return entity;
		Mob converted = (Mob) entity;
		LazyOptional<HordeSpawn> beforeOptional = getCapability(HordesCapabilities.HORDESPAWN);
		LazyOptional<HordeSpawn> afterOptional = converted.getCapability(HordesCapabilities.HORDESPAWN);
		if (!(beforeOptional.isPresent() || afterOptional.isPresent() || beforeOptional.orElseGet(null).isHordeSpawned())) return converted;
		String uuid = beforeOptional.orElseGet(null).getPlayerUUID();
		if (!DataUtils.isValidUUID(uuid)) return converted;
		afterOptional.orElseGet(null).setPlayerUUID(uuid);
		beforeOptional.orElseGet(null).setPlayerUUID("");
		HordeEvent horde = HordeSavedData.getData((ServerLevel) level()).getEvent(UUID.fromString(uuid));
		if (horde != null) {
			ServerPlayer player = ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayer(UUID.fromString(uuid));
			horde.registerEntity(converted, player);
			horde.removeEntity((Mob) (LivingEntity) this);
			converted.targetSelector.getRunningGoals().forEach(WrappedGoal::stop);
			if (converted instanceof PathfinderMob) converted.targetSelector.addGoal(1, new HurtByTargetGoal((PathfinderMob) converted));
			converted.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(converted, Player.class, true));
		}
		return converted;
	}

	//add horde ai to converted mobs
	@Inject(at=@At("TAIL"), method = "convertTo", cancellable = true)
	public void convertTo(EntityType<?> type, boolean keepEquipment, CallbackInfoReturnable<Mob> callback) {
		Mob converted = callback.getReturnValue();
		LazyOptional<HordeSpawn> optional = converted.getCapability(HordesCapabilities.HORDESPAWN);
		if (!optional.isPresent()) return;
		if (!optional.orElseGet(null).isHordeSpawned()) return;
		String uuid = optional.orElseGet(null).getPlayerUUID();
		if (DataUtils.isValidUUID(uuid)) {
			ServerPlayer player = ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayer(UUID.fromString(uuid));
			if (player == null) return;
			HordeEvent horde = HordeSavedData.getData((ServerLevel)level()).getEvent(player);
			if (horde == null) return;
			if (!horde.isActive(player)) return;
			horde.registerEntity((Mob)(LivingEntity)this, player);
		}
	}

	@Inject(at=@At("HEAD"), method = "registerGoals", cancellable = true)
	public void registerGoals(CallbackInfo callback) {
		if (CommonConfigHandler.piglinsHuntZombies.get() && ((LivingEntity)this) instanceof Piglin) {
			goalSelector.addGoal(1, new FleeEntityGoal((Mob)(LivingEntity)this, 1.5, 5, HordesInfection::canCauseInfection));
		}
	}

	@Inject(at = @At("HEAD"), method = "canBeLeashed")
	public void canBeLeashed(Player player, CallbackInfoReturnable<Boolean> callback) {
		if (((LivingEntity)this) instanceof ZombieHorse && CommonConfigHandler.aggressiveZombieHorses.get()) {
			callback.setReturnValue(false);
		}
	}
}
