package net.smileycorp.hordes.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableWitchTargetGoal;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.smileycorp.hordes.common.ai.FleeEntityGoal;
import net.smileycorp.hordes.config.CommonConfigHandler;
import net.smileycorp.hordes.infection.HordesInfection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Witch.class)
public abstract class MixinWitch extends Raider implements RangedAttackMob {
    
    private LivingEntity entity;
    
    protected MixinWitch(EntityType<? extends Raider> p_37839_, Level p_37840_) {
        super(p_37839_, p_37840_);
    }
    
    @Inject(method = "performRangedAttack", at = @At("HEAD"))
    public void performRangedAttack$Head(LivingEntity entity, float p_34144_, CallbackInfo ci) {
        this.entity = entity;
    }
    
    @Inject(method = "performRangedAttack", at = @At("TAIL"))
    public void performRangedAttack$Tail(LivingEntity entity, float p_34144_, CallbackInfo ci) {
        this.entity = null;
    }
    
    @WrapOperation(method = "performRangedAttack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/alchemy/PotionUtils;setPotion(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/alchemy/Potion;)Lnet/minecraft/world/item/ItemStack;"))
    public ItemStack performRangedAttack$setPotion(ItemStack stack, Potion potion, Operation<ItemStack> original) {
        if (CommonConfigHandler.illagersHuntZombies.get() && (potion == Potions.HARMING || potion == Potions.POISON) && entity.getMobType() == MobType.UNDEAD && HordesInfection.canCauseInfection(entity))
            return original.call(stack, entity.hasEffect(MobEffects.REGENERATION) && entity.getHealth() >= 8.0F ? Potions.REGENERATION : Potions.HEALING);
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
            targetSelector.addGoal(2, new NearestAttackableWitchTargetGoal<>(this, LivingEntity.class, 10, true, false, HordesInfection::canCauseInfection));
            goalSelector.addGoal(1, new FleeEntityGoal(this, 1.5, 5, HordesInfection::canCauseInfection));
        }
    }
    
}
