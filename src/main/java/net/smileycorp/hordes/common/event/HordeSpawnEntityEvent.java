package net.smileycorp.hordes.common.event;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.ICancellableEvent;
import net.smileycorp.hordes.hordeevent.capability.HordeEvent;

public class HordeSpawnEntityEvent extends HordePlayerEvent implements ICancellableEvent {
	
	protected Mob entity;
	protected Vec3 pos;

	public HordeSpawnEntityEvent(ServerPlayer player, Mob entity, Vec3 pos, HordeEvent horde) {
		super(player, horde);
		this.entity = entity;
		this.pos = pos;
	}
	
	@Override
	public Mob getEntity() {
		return entity;
	}
	
	public void setEntity(Mob entity) {
		this.entity = entity;
	}
	
	public Vec3 getPos() {
		return pos;
	}
	
	public void setPos(Vec3 pos) {
		this.pos = pos;
	}

}
