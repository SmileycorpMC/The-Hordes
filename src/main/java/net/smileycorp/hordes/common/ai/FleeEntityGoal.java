package net.smileycorp.hordes.common.ai;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.smileycorp.atlas.api.util.DirectionUtils;

import java.util.EnumSet;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class FleeEntityGoal extends Goal  {


	protected final Mob entity;
	protected final Level level;
	protected final PathNavigation pather;
	protected int timeToRecalcPath = 0;
	protected float waterCost;
	protected final double speed, range;
	protected final Predicate<LivingEntity> predicate;

	public FleeEntityGoal(Mob entity, double speed, double range, Predicate<LivingEntity> predicate) {
		super();
		this.entity = entity;
		level = entity.level();
		pather = entity.getNavigation();
		this.predicate = predicate;
		this.speed = speed;
		this.range = range;
		setFlags(EnumSet.of(Flag.LOOK, Flag.MOVE));
	}

	@Override
	public boolean canUse() {
		return !getEntities().isEmpty();
	}

	private List<LivingEntity> getEntities() {
		return level.getEntitiesOfClass(LivingEntity.class, new AABB(entity.getX() - range, entity.getY() - range, entity.getZ() - range,
				entity.getX() + range, entity.getZ() + range, entity.getZ() + range), predicate);
	}

	@Override
	public void start() {
		waterCost = entity.getPathfindingMalus(BlockPathTypes.WATER);
	}

	@Override
	public boolean canContinueToUse() {
		return true;
	}

	@Override
	public void stop() {
		pather.stop();
		entity.setPathfindingMalus(BlockPathTypes.WATER, waterCost);
	}

	@Override
	public void tick() {
		if (--timeToRecalcPath <= 0)  {
			timeToRecalcPath = 5;
			pather.moveTo(pather.createPath(findSafePos(), 1), speed);
		}
	}

	private Stream<BlockPos> findSafePos() {
		Vec3 pos = entity.position();
		Vec3 resultDir = new Vec3(0, 0, 0);
		for (LivingEntity entity : getEntities()) {
			Vec3 dir = DirectionUtils.getDirectionVecXZ(this.entity, entity);
			resultDir = new Vec3((dir.x + resultDir.x) * 0.5, (dir.y + resultDir.y) * 0.5, (dir.z + resultDir.z) * 0.5);
		}
		return Stream.of(level.getHeightmapPos(Types.WORLD_SURFACE, BlockPos.containing(pos.add(resultDir.reverse().multiply(5, 0, 5)))));
	}

}
