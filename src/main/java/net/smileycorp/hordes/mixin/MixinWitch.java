package net.smileycorp.hordes.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.entity.CreatureAttribute;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.ToggleableNearestAttackableTargetGoal;
import net.minecraft.entity.monster.AbstractRaiderEntity;
import net.minecraft.entity.monster.WitchEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Effects;
import net.minecraft.potion.Potion;
import net.minecraft.potion.Potions;
import net.minecraft.world.World;
import net.smileycorp.hordes.common.ai.FleeEntityGoal;
import net.smileycorp.hordes.config.CommonConfigHandler;
import net.smileycorp.hordes.infection.HordesInfection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WitchEntity.class)
public abstract class MixinWitch extends AbstractRaiderEntity {
    
    private LivingEntity entity;
    
    protected MixinWitch(EntityType<? extends AbstractRaiderEntity> p_i50143_1_, World p_i50143_2_) {
        super(p_i50143_1_, p_i50143_2_);
    }
    
    @Inject(method = "performRangedAttack", at = @At("HEAD"))
    public void performRangedAttack$Head(LivingEntity entity, float p_34144_, CallbackInfo ci) {
        this.entity = entity;
    }
    
    @Inject(method = "performRangedAttack", at = @At("TAIL"))
    public void performRangedAttack$Tail(LivingEntity entity, float p_34144_, CallbackInfo ci) {
        this.entity = null;
    }
    
    @WrapOperation(method = "performRangedAttack", at = @At(value = "INVOKE", target = "Lnet/minecraft/potion/PotionUtils;setPotion(Lnet/minecraft/item/ItemStack;Lnet/minecraft/potion/Potion;)Lnet/minecraft/item/ItemStack;"))
    public ItemStack performRangedAttack$setPotion(ItemStack stack, Potion potion, Operation<ItemStack> original) {
        if (CommonConfigHandler.illagersHuntZombies.get() && (potion == Potions.HARMING || potion == Potions.POISON) && entity.getMobType() == CreatureAttribute.UNDEAD && HordesInfection.canCauseInfection(entity))
            return original.call(stack, entity.hasEffect(Effects.REGENERATION) && entity.getHealth() >= 8.0F ? Potions.REGENERATION : Potions.HEALING);
        return original.call(stack, potion);
    }
    
    /*@WrapOperation(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;distanceToSqr(Lnet/minecraft/world/entity/Entity;)D"))
    public double aiStep$distanceToSqr(LivingEntity witch, Operation<Double> original) {
        double distance = original.call(witch);
        if (CommonConfigHandler.illagersHuntZombies.get() && entity.getMobType() == MobType.UNDEAD)
            return HordesInfection.canCauseInfection(entity) && distance < 100 ? 122 : 0;
        return distance;
    }*/
    
    @Inject(at=@At("HEAD"), method = "registerGoals", cancellable = true)
    public void registerGoals(CallbackInfo callback) {
        if (CommonConfigHandler.illagersHuntZombies.get()) {
            targetSelector.addGoal(2, new ToggleableNearestAttackableTargetGoal<>(this, LivingEntity.class, 10, true, false, HordesInfection::canCauseInfection));
            goalSelector.addGoal(1, new FleeEntityGoal(this, 1.5, 5, HordesInfection::canCauseInfection));
        }
    }
    
}
