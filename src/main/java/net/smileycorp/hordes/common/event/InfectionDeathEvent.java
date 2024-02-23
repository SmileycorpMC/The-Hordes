package net.smileycorp.hordes.common.event;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent;

public class InfectionDeathEvent extends LivingEvent implements ICancellableEvent {

	protected final DamageSource source;

	public InfectionDeathEvent(LivingEntity entity, DamageSource source) {
		super(entity);
		this.source = source;
	}

	public DamageSource getSource() {
		return source;
	}

}
