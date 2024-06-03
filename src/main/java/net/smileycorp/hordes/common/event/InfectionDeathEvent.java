package net.smileycorp.hordes.common.event;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.eventhandler.Cancelable;

@Cancelable
public class InfectionDeathEvent extends LivingEvent {

	protected final DamageSource source;

	public InfectionDeathEvent(EntityLivingBase entity, DamageSource source) {
		super(entity);
		this.source = source;
	}

	public DamageSource getSource() {
		return source;
	}

}
