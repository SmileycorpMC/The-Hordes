package net.smileycorp.hordes.common.event;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.smileycorp.hordes.hordeevent.capability.HordeEvent;

@Cancelable
public class HordeSpawnEntityEvent extends HordePlayerEvent {
	
	protected EntityLiving entity;
	protected Vec3d pos;

	public HordeSpawnEntityEvent(EntityPlayerMP player, EntityLiving entity, Vec3d pos, HordeEvent horde) {
		super(player, horde);
		this.entity = entity;
		this.pos = pos;
	}
	
	@Override
	public EntityLiving getEntity() {
		return entity;
	}
	
	public void setEntity(EntityLiving entity) {
		this.entity = entity;
	}
	
	public Vec3d getPos() {
		return pos;
	}
	
	public void setPos(Vec3d pos) {
		this.pos = pos;
	}

}
