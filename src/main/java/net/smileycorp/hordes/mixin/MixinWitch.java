package net.smileycorp.hordes.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.Holder;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableWitchTargetGoal;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.item.Item;
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
    
    protected MixinWitch(EntityType<? extends Raider> p_37839_, Level p_37840_) {
        super(p_37839_, p_37840_);
    }
    
    @WrapOperation(method = "performRangedAttack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/alchemy/PotionContents;createItemStack(Lnet/minecraft/world/item/Item;Lnet/minecraft/core/Holder;)Lnet/minecraft/world/item/ItemStack;"))
    public ItemStack performRangedAttack$setPotion(Item item, Holder<Potion> potion, Operation<ItemStack> original, @Local(ordinal = 0) LivingEntity entity) {
        if (CommonConfigHandler.illagersHuntZombies.get() && (potion == Potions.HARMING || potion == Potions.POISON) && entity.getType().is(EntityTypeTags.UNDEAD)
                && HordesInfection.canCauseInfection(entity))
            return original.call(item, entity.hasEffect(MobEffects.REGENERATION) && entity.getHealth() >= 8.0F ? Potions.REGENERATION : Potions.HEALING);
        return original.call(item, potion);
    }
    
    @WrapOperation(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;distanceToSqr(Lnet/minecraft/world/entity/Entity;)D"))
    public double aiStep$distanceToSqr(LivingEntity instance, Entity entity, Operation<Double> original) {
        double distance = original.call(instance, entity);
        if (CommonConfigHandler.illagersHuntZombies.get() && entity.getType().is(EntityTypeTags.UNDEAD))
            return HordesInfection.canCauseInfection(instance) && distance < 100 ? 122 : 0;
        return distance;
    }
    
    @Inject(at=@At("HEAD"), method = "registerGoals", cancellable = true)
    public void registerGoals(CallbackInfo callback) {
        if (CommonConfigHandler.illagersHuntZombies.get()) {
            targetSelector.addGoal(2, new NearestAttackableWitchTargetGoal<>(this, LivingEntity.class, 10, true, false, HordesInfection::canCauseInfection));
            goalSelector.addGoal(1, new FleeEntityGoal(this, 1.5, 5, HordesInfection::canCauseInfection));
        }
    }
    
}
