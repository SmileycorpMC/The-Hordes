package net.smileycorp.hordes.common.event;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.smileycorp.hordes.hordeevent.capability.HordeEvent;

public class HordeFindSpawnPosEvent extends HordePlayerEvent {

	protected final Vec3 dir;
	protected final BlockPos originalPos;
	protected BlockPos pos;
	private final boolean checkLight;

	public HordeFindSpawnPosEvent(ServerPlayer player, HordeEvent horde, Vec3 dir, BlockPos pos, boolean checkLight) {
		super(player, horde);
		this.dir = dir;
		this.originalPos = pos;
		this.pos = pos;
		this.checkLight = checkLight;
	}

	//the direction vector from the player the horde is attempting to spawn a wave
	public Vec3 getDir() {
		return dir;
	}

	//original position calculated by the horde event spawner
	public BlockPos getOriginalPos() {
		return originalPos;
	}

	//position after modified by the event
	public BlockPos getPos() {
		return pos;
	}

	//change the position horde mobs spawn around
	public void setPos(BlockPos pos) {
		this.pos = pos;
	}

	//whether the horde is currently checking for light sources
	public boolean checksLight() {
		return checkLight;
	}

}
