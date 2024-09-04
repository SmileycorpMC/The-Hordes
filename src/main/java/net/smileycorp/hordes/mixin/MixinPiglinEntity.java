package net.smileycorp.hordes.mixin;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.monster.piglin.AbstractPiglinEntity;
import net.minecraft.entity.monster.piglin.PiglinEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import net.smileycorp.hordes.config.CommonConfigHandler;
import net.smileycorp.hordes.config.InfectionConfig;
import net.smileycorp.hordes.infection.HordesInfection;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PiglinEntity.class)
public abstract class MixinPiglinEntity extends AbstractPiglinEntity {
	
	@Shadow @Final private Inventory inventory;
	
	public MixinPiglinEntity(EntityType<? extends AbstractPiglinEntity> p_i241915_1_, World p_i241915_2_) {
		super(p_i241915_1_, p_i241915_2_);
	}
	
	@Inject(at=@At("TAIL"), method = "customServerAiStep()V", cancellable = true)
	public void customServerAiStep(CallbackInfo callback) {
		if (!(InfectionConfig.enableMobInfection.get() && CommonConfigHandler.piglinsCureThemself.get())) return;
		if (!hasEffect(HordesInfection.INFECTED.get())) return;
		if (!getBrain().checkMemory(MemoryModuleType.ATTACK_TARGET, MemoryModuleStatus.VALUE_ABSENT)) return;
		if (!getItemBySlot(EquipmentSlotType.OFFHAND).isEmpty()) return;
		for (int i = 0; i < inventory.getContainerSize(); i++) {
			if (!getItemBySlot(EquipmentSlotType.OFFHAND).isEmpty()) return;
			ItemStack stack = inventory.getItem(i).copy();
			if (stack.getItem().is(HordesInfection.INFECTION_CURES_TAG)) {
				if (getItemBySlot(EquipmentSlotType.OFFHAND).isEmpty()) {
					stack.setCount(1);
					inventory.getItem(i).shrink(1);
					setItemSlot(EquipmentSlotType.OFFHAND, stack);
					startUsingItem(Hand.OFF_HAND);
					return;
				}
			}
		}
	}

}
