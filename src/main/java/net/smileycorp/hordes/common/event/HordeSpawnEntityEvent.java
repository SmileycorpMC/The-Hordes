package net.smileycorp.hordes.common.event;

import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.eventbus.api.Cancelable;
import net.smileycorp.hordes.hordeevent.capability.HordeEvent;

@Cancelable
public class HordeSpawnEntityEvent extends HordePlayerEvent {
	
	protected MobEntity entity;
	protected Vector3d pos;

	public HordeSpawnEntityEvent(ServerPlayerEntity player, MobEntity entity, Vector3d pos, HordeEvent horde) {
		super(player, horde);
		this.entity = entity;
		this.pos = pos;
	}
	
	@Override
	public MobEntity getEntity() {
		return entity;
	}
	
	public void setEntity(MobEntity entity) {
		this.entity = entity;
	}
	
	public Vector3d getPos() {
		return pos;
	}
	
	public void setPos(Vector3d pos) {
		this.pos = pos;
	}

}
