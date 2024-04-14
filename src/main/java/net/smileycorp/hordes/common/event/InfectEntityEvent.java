package net.smileycorp.hordes.common.event;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.Cancelable;

@Cancelable
public class InfectEntityEvent extends LivingEvent {

    private final LivingEntity attacker;
    private final DamageSource source;
    private final float amount;

    public InfectEntityEvent(LivingEntity entity, LivingEntity attacker, DamageSource source, float amount) {
        super(entity);
        this.attacker = attacker;
        this.source = source;
        this.amount = amount;
    }

    public LivingEntity getAttacker() {
        return attacker;
    }

    public DamageSource getDamageSource() {
        return source;
    }

    public float getDamageAmount() {
        return amount;
    }

}
