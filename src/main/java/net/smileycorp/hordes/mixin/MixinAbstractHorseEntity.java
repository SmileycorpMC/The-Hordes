package net.smileycorp.hordes.mixin;

import net.minecraft.entity.CreatureAttribute;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.horse.AbstractHorseEntity;
import net.minecraft.entity.passive.horse.SkeletonHorseEntity;
import net.minecraft.entity.passive.horse.ZombieHorseEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.smileycorp.hordes.common.ai.HorseFleeGoal;
import net.smileycorp.hordes.config.CommonConfigHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractHorseEntity.class)
public abstract class MixinAbstractHorseEntity extends AnimalEntity {
	
	@Shadow public Inventory inventory;
	
	protected MixinAbstractHorseEntity(World level) {
		super(null, level);
	}

	@Inject(at=@At("HEAD"), method = "aiStep()V", cancellable = true)
	public void aiStep(CallbackInfo callback) {
		if ((AnimalEntity)this instanceof ZombieHorseEntity) {
			if (CommonConfigHandler.aggressiveZombieHorses.get()) {
				updateSwingTime();
				if (getBrightness() > 0.5F) noActionTime += 2;
			}
			if (CommonConfigHandler.zombieHorsesBurn.get()) tryBurn();
		}
		else if ((AnimalEntity)this instanceof SkeletonHorseEntity && CommonConfigHandler.skeletonHorsesBurn.get()) tryBurn();
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
		if (getMobType() != CreatureAttribute.UNDEAD && CommonConfigHandler.zombiesScareHorses.get()) goalSelector.addGoal(1, new HorseFleeGoal(this));
	}
	
	@Inject(at=@At("HEAD"), method = "canEatGrass", cancellable = true)
	public void canEatGrass(CallbackInfoReturnable<Boolean> callback) {
		if ((AnimalEntity)this instanceof ZombieHorseEntity && CommonConfigHandler.aggressiveZombieHorses.get()) callback.setReturnValue(false);
	}

}
