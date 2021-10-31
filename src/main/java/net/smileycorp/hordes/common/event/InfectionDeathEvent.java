package net.smileycorp.hordes.common.event;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.DamageSource;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.Cancelable;

@Cancelable
public class InfectionDeathEvent extends LivingEvent {

	protected final DamageSource source;

	public InfectionDeathEvent(LivingEntity entity, DamageSource source) {
		super(entity);
		this.source=source;
	}

	public DamageSource getSource() {
		return source;
	}

}
