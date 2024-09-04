package net.smileycorp.hordes.mixin;

import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.monster.piglin.AbstractPiglinEntity;
import net.minecraft.entity.monster.piglin.PiglinBruteEntity;
import net.minecraft.entity.monster.piglin.PiglinEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import net.smileycorp.hordes.config.CommonConfigHandler;
import net.smileycorp.hordes.config.InfectionConfig;
import net.smileycorp.hordes.infection.HordesInfection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PiglinBruteEntity.class)
public abstract class MixinPiglinBruteEntity extends AbstractPiglinEntity {

	@Shadow public abstract Brain<PiglinEntity> getBrain();

	protected MixinPiglinBruteEntity(World level) {
		super(null, level);
	}

	@Inject(at=@At("HEAD"), method = "customServerAiStep()V", cancellable = true)
	public void customServerAiStep(CallbackInfo callback) {
		if (!(InfectionConfig.enableMobInfection.get() && CommonConfigHandler.piglinsCureThemself.get())) return;
		if (!hasEffect(HordesInfection.INFECTED.get())) return;
		if(!getBrain().checkMemory(MemoryModuleType.ATTACK_TARGET, MemoryModuleStatus.VALUE_ABSENT)) return;
		if (!getOffhandItem().isEmpty()) return;
		ItemStack stack = new ItemStack(Items.GOLDEN_APPLE);
		if (stack.getItem().is(HordesInfection.INFECTION_CURES_TAG)) {
			if (getOffhandItem().isEmpty()) {
				setItemInHand(Hand.OFF_HAND, stack);
				startUsingItem(Hand.OFF_HAND);
			}
		}
	}

}
