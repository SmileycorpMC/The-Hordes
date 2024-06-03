package net.smileycorp.hordes.common.event;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.eventhandler.Cancelable;

@Cancelable
public class InfectEntityEvent extends LivingEvent {

    private final EntityLiving attacker;
    private final DamageSource source;
    private final float amount;

    public InfectEntityEvent(EntityLivingBase entity, EntityLiving attacker, DamageSource source, float amount) {
        super(entity);
        this.attacker = attacker;
        this.source = source;
        this.amount = amount;
    }

    public EntityLiving getAttacker() {
        return attacker;
    }

    public DamageSource getDamageSource() {
        return source;
    }

    public float getDamageAmount() {
        return amount;
    }

}
