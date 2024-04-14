package net.smileycorp.hordes.mixin;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Vex;
import net.minecraft.world.level.Level;
import net.smileycorp.hordes.config.CommonConfigHandler;
import net.smileycorp.hordes.infection.HordesInfection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Vex.class)
public abstract class MixinVex extends Monster {
    
    protected MixinVex(EntityType<? extends Monster> p_32105_, Level p_32106_) {
        super(p_32105_, p_32106_);
    }
    
    @Inject(at=@At("HEAD"), method = "registerGoals", cancellable = true)
    public void registerGoals(CallbackInfo callback) {
        if (CommonConfigHandler.illagersHuntZombies.get())
            targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, LivingEntity.class, 10, true, false, HordesInfection::canCauseInfection));
    }
    
}
