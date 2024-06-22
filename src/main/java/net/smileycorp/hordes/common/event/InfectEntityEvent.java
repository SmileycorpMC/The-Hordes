package net.smileycorp.hordes.common.event;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent;


public class InfectEntityEvent extends LivingEvent implements ICancellableEvent {

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
