package net.smileycorp.hordes.mixin;

import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.monster.EntityIronGolem;
import net.minecraft.entity.monster.EntityPigZombie;
import net.minecraft.entity.passive.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ContainerHorseChest;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.smileycorp.hordes.common.ai.EntityAIHorseFlee;
import net.smileycorp.hordes.config.CommonConfigHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractHorse.class)
public abstract class MixinAbstractHorse extends EntityAnimal {

	@Shadow
	protected ContainerHorseChest horseChest;

	public MixinAbstractHorse(World worldIn) {
		super(worldIn);
	}

	@Inject(at=@At("TAIL"), method = "initEntityAI()V", cancellable = true)
	protected void initEntityAI(CallbackInfo callback) {
		if (CommonConfigHandler.aggressiveZombieHorses && ((EntityAnimal)this) instanceof EntityZombieHorse) {
			tasks.addTask(0, new EntityAISwimming(this));
			tasks.addTask(2, new EntityAIAttackMelee(this, 1.0D, false));
			tasks.addTask(5, new EntityAIMoveTowardsRestriction(this, 1.0D));
			tasks.addTask(7, new EntityAIWanderAvoidWater(this, 1.0D));
			tasks.addTask(8, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
			tasks.addTask(8, new EntityAILookIdle(this));
			tasks.addTask(6, new EntityAIMoveThroughVillage(this, 1.0D, false));
			targetTasks.addTask(1, new EntityAIHurtByTarget(this, true, new Class[] {EntityPigZombie.class}));
			targetTasks.addTask(2, new EntityAINearestAttackableTarget<>(this, EntityPlayer.class, true));
			targetTasks.addTask(3, new EntityAINearestAttackableTarget<>(this, EntityVillager.class, false));
			targetTasks.addTask(3, new EntityAINearestAttackableTarget<>(this, EntityIronGolem.class, true));
			tasks.taskEntries.removeIf(g->g.action instanceof EntityAIPanic);
			tasks.taskEntries.removeIf(g->g.action instanceof EntityAIPanic);
		}
		if (getCreatureAttribute() != EnumCreatureAttribute.UNDEAD && CommonConfigHandler.zombiesScareHorses) {
			tasks.addTask(1, new EntityAIHorseFlee(this));
		}
	}

	@Inject(at=@At("HEAD"), method = "onLivingUpdate()V", cancellable = true)
	public void onLivingUpdate(CallbackInfo callback) {
		if ((EntityAnimal)this instanceof EntityZombieHorse) {
			if (CommonConfigHandler.aggressiveZombieHorses) {
				updateArmSwingProgress();
				if (getBrightness() > 0.5F) idleTime += 2;
			}
			if (CommonConfigHandler.zombieHorsesBurn) tryBurn();
		}
		else if ((EntityAnimal)this instanceof EntitySkeletonHorse && CommonConfigHandler.skeletonHorsesBurn) tryBurn();
	}

	protected void tryBurn() {
		boolean burn = world.isDaytime() && !world.isRemote;
		if (burn && getPassengers().isEmpty()) {
			ItemStack itemstack = horseChest.getStackInSlot(1);
			if (!itemstack.isEmpty()) {
				if (itemstack.isItemDamaged()) {
					itemstack.setItemDamage(itemstack.getItemDamage() + rand.nextInt(2));
					if (itemstack.getItemDamage() >= itemstack.getMaxDamage()) horseChest.decrStackSize(1, 1);
				}
				burn = false;
			}
			if (burn) setFire(8);
		}
	}

	@Inject(at=@At("HEAD"), method = "canEatGrass()Z", cancellable = true)
	public void canEatGrass(CallbackInfoReturnable<Boolean> callback) {
		if ((EntityAnimal)this instanceof EntityZombieHorse && CommonConfigHandler.aggressiveZombieHorses) callback.setReturnValue(false);
	}

}
