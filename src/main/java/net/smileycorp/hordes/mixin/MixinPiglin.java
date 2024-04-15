package net.smileycorp.hordes.mixin;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.smileycorp.hordes.config.CommonConfigHandler;
import net.smileycorp.hordes.config.InfectionConfig;
import net.smileycorp.hordes.infection.HordesInfection;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Piglin.class)
public abstract class MixinPiglin extends AbstractPiglin {

	@Shadow public abstract Brain<Piglin> getBrain();

	@Shadow @Final private SimpleContainer inventory;

	protected MixinPiglin(Level level) {
		super(null, level);
	}

	@Inject(at=@At("TAIL"), method = "customServerAiStep()V", cancellable = true)
	public void customServerAiStep(CallbackInfo callback) {
		if (!(InfectionConfig.enableMobInfection.get() && CommonConfigHandler.piglinsCureThemself.get())) return;
		if (!hasEffect(HordesInfection.INFECTED.get())) return;
		if (!getBrain().checkMemory(MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_ABSENT)) return;
		if (!getItemBySlot(EquipmentSlot.OFFHAND).isEmpty()) return;
		for (int i = 0; i < inventory.getContainerSize(); i++) {
			if (!getItemBySlot(EquipmentSlot.OFFHAND).isEmpty()) return;
			ItemStack stack = inventory.getItem(i).copy();
			if (stack.m_204117_(HordesInfection.INFECTION_CURES_TAG)) {
				if (getItemBySlot(EquipmentSlot.OFFHAND).isEmpty()) {
					stack.setCount(1);
					inventory.getItem(i).shrink(1);
					setItemSlot(EquipmentSlot.OFFHAND, stack);
					startUsingItem(InteractionHand.OFF_HAND);
					return;
				}
			}
		}
	}

}
