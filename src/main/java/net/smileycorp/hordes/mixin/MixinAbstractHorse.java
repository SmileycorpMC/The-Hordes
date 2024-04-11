package net.smileycorp.hordes.mixin;

import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.animal.horse.SkeletonHorse;
import net.minecraft.world.entity.animal.horse.ZombieHorse;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.smileycorp.hordes.common.ai.HorseFleeGoal;
import net.smileycorp.hordes.config.CommonConfigHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractHorse.class)
public abstract class MixinAbstractHorse extends Animal {

	@Shadow
	protected SimpleContainer inventory;

	protected MixinAbstractHorse(Level level) {
		super(null, level);
	}

	@Inject(at=@At("HEAD"), method = "aiStep()V", cancellable = true)
	public void aiStep(CallbackInfo callback) {
		if ((Animal)this instanceof ZombieHorse) {
			if (CommonConfigHandler.aggressiveZombieHorses.get()) {
				updateSwingTime();
				if (getBrightness() > 0.5F) noActionTime += 2;
			}
			if (CommonConfigHandler.zombieHorsesBurn.get()) tryBurn();
		}
		else if ((Animal)this instanceof SkeletonHorse && CommonConfigHandler.skeletonHorsesBurn.get()) tryBurn();
	}
	
	protected void tryBurn() {
		boolean burn = isSunBurnTick();
		if (burn && getPassengers().isEmpty()) {
			ItemStack itemstack = inventory.getItem(1);
			if (!itemstack.isEmpty()) {
				if (itemstack.isDamageableItem()) {
					itemstack.setDamageValue(itemstack.getDamageValue() + random.nextInt(2));
					if (itemstack.getDamageValue() >= itemstack.getMaxDamage()) inventory.setItem(1, ItemStack.EMPTY);
				}
				burn = false;
			}
			if (burn) setSecondsOnFire(8);
		}
	}
	
	@Inject(at=@At("HEAD"), method = "registerGoals", cancellable = true)
	public void registerGoals(CallbackInfo callback) {
		if (getMobType() != MobType.UNDEAD && CommonConfigHandler.zombiesScareHorses.get()) goalSelector.addGoal(1, new HorseFleeGoal(this));
	}
	
	@Inject(at=@At("HEAD"), method = "canEatGrass", cancellable = true)
	public void canEatGrass(CallbackInfoReturnable<Boolean> callback) {
		if ((Animal)this instanceof ZombieHorse && CommonConfigHandler.aggressiveZombieHorses.get()) callback.setReturnValue(false);
	}

}
