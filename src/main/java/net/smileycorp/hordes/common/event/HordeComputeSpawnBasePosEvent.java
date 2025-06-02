package net.smileycorp.hordes.common.event;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.smileycorp.hordes.hordeevent.capability.HordeEvent;

public class HordeComputeSpawnBasePosEvent extends HordePlayerEvent {

	protected final Vec3 baseDir;
	protected final BlockPos firstBasePos;
	protected BlockPos basePos;
	private final boolean checkLight;

	public HordeComputeSpawnBasePosEvent(ServerPlayer player, HordeEvent horde, Vec3 basedir, BlockPos basepos, boolean checkLight) {
		super(player, horde);
		this.baseDir = basedir;
		this.firstBasePos = basepos;
		this.basePos = basepos;
		this.checkLight = checkLight;
	}

	public Vec3 getBaseDir() {
		return baseDir;
	}

	public BlockPos getFirstBasePos() {
		return firstBasePos;
	}

	public BlockPos getBasePos() {
		return basePos;
	}

	public void setBasePos(BlockPos basePos) {
		this.basePos = basePos;
	}

	public boolean checksLight() {
		return checkLight;
	}

}
