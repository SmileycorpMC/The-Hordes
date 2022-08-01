package net.smileycorp.hordes.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.entity.CreatureAttribute;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.passive.horse.AbstractHorseEntity;
import net.minecraft.entity.passive.horse.SkeletonHorseEntity;
import net.minecraft.entity.passive.horse.ZombieHorseEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.smileycorp.hordes.common.CommonConfigHandler;
import net.smileycorp.hordes.common.ai.HorseFleeGoal;

@Mixin(AbstractHorseEntity.class)
public abstract class MixinAbstractHorseEntity extends CreatureEntity {

	@Shadow
	protected Inventory inventory;

	protected MixinAbstractHorseEntity(World world) {
		super(null, world);
	}

	@Inject(at=@At("HEAD"), method = "aiStep()V", cancellable = true)
	public void aiStep(CallbackInfo callback) {
		if ((CreatureEntity)this instanceof ZombieHorseEntity) {
			if (CommonConfigHandler.aggressiveZombieHorses.get()) {
				updateSwingTime();
				float f = getBrightness();
				if (f > 0.5F) noActionTime += 2;
			}
			if (CommonConfigHandler.zombieHorsesBurn.get()) {
				tryBurn();
			}
		}
		else if ((CreatureEntity)this instanceof SkeletonHorseEntity) {
			if (CommonConfigHandler.skeletonHorsesBurn.get()) {
				tryBurn();
			}
		}
	}

	protected void tryBurn() {
		boolean flag = this.isSunBurnTick();
		if (flag && getPassengers().isEmpty()) {
			ItemStack itemstack = inventory.getItem(1);
			if (!itemstack.isEmpty()) {
				if (itemstack.isDamageableItem()) {
					itemstack.setDamageValue(itemstack.getDamageValue() + this.random.nextInt(2));
					if (itemstack.getDamageValue() >= itemstack.getMaxDamage()) {
						inventory.setItem(1, ItemStack.EMPTY);
					}
				}

				flag = false;
			}

			if (flag) {
				this.setSecondsOnFire(8);
			}
		}
	}

	@Inject(at=@At("HEAD"), method = "registerGoals()V", cancellable = true)
	public void registerGoals(CallbackInfo callback) {
		if (getMobType() != CreatureAttribute.UNDEAD && CommonConfigHandler.zombiesScareHorses.get()) {
			goalSelector.addGoal(1, new HorseFleeGoal(this));
		}
	}

	@Inject(at=@At("HEAD"), method = "canEatGrass()Z", cancellable = true)
	public void canEatGrass(CallbackInfoReturnable<Boolean> callback) {
		if ((CreatureEntity)this instanceof ZombieHorseEntity) {
			if (CommonConfigHandler.aggressiveZombieHorses.get()) {
				callback.setReturnValue(false);
				callback.cancel();
			}
		}
	}

}
