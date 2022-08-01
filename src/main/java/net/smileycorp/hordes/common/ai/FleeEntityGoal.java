package net.smileycorp.hordes.common.ai;

import java.util.EnumSet;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.pathfinding.PathNavigator;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.gen.Heightmap.Type;
import net.smileycorp.atlas.api.util.DirectionUtils;

public class FleeEntityGoal extends Goal  {


	protected final MobEntity entity;
	protected final World world;
	protected final PathNavigator pather;
	protected int timeToRecalcPath = 0;
	protected float waterCost;
	protected final double speed, range;
	protected final Predicate<LivingEntity> predicate;

	public FleeEntityGoal(MobEntity entity, double speed, double range, Predicate<LivingEntity> predicate) {
		super();
		this.entity=entity;
		world=entity.level;
		pather=entity.getNavigation();
		this.predicate = predicate;
		this.speed = speed;
		this.range = range;
		setFlags(EnumSet.of(Flag.LOOK, Flag.MOVE));
	}

	@Override
	public boolean canUse() {
		for (LivingEntity entity : getEntities()) if (this.entity.canSee(entity)) return true;
		return false;
	}

	private List<LivingEntity> getEntities() {
		return world.getEntitiesOfClass(LivingEntity.class, new AxisAlignedBB(entity.getX()-range, entity.getY()-range, entity.getZ()-range, entity.getX()+range, entity.getZ()+range, entity.getZ()+range), predicate);
	}

	@Override
	public void start() {
		waterCost = entity.getPathfindingMalus(PathNodeType.WATER);
	}

	@Override
	public boolean canContinueToUse() {
		return true;
	}

	@Override
	public void stop() {
		pather.stop();
		entity.setPathfindingMalus(PathNodeType.WATER, waterCost);
	}

	@Override
	public void tick() {
		if (--this.timeToRecalcPath <= 0)  {
			this.timeToRecalcPath = 5;
			pather.moveTo(pather.createPath(findSafePos(), 1), speed);
		}
	}

	private Stream<BlockPos> findSafePos() {
		Vector3d pos = entity.position();
		Vector3d resultDir = new Vector3d(0, 0, 0);
		for (LivingEntity entity : getEntities()) if (this.entity.canSee(entity)) {
			Vector3d dir = DirectionUtils.getDirectionVecXZ(this.entity, entity);
			resultDir = new Vector3d((dir.x + resultDir.x)/2, (dir.y + resultDir.y)/2, (dir.z + resultDir.z)/2);
		}
		return Stream.of(world.getHeightmapPos(Type.WORLD_SURFACE, new BlockPos(pos.add(resultDir.reverse().multiply(5, 0, 5)))));
	}

}
