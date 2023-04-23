package net.smileycorp.hordes.common.ai;

import java.util.List;

import com.google.common.base.Predicate;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.smileycorp.atlas.api.util.DirectionUtils;

public class FleeEntityTask extends EntityAIBase {

	protected final EntityLiving entity;
	protected final World world;
	protected final PathNavigate pather;
	protected int timeToRecalcPath = 0;
	protected float waterCost;
	protected final double speed, range;
	protected final Predicate<EntityLivingBase> predicate;

	public FleeEntityTask(EntityLiving entity, double speed, double range, Predicate<EntityLivingBase> predicate) {
		super();
		this.entity=entity;
		world = entity.world;
		pather = entity.getNavigator();
		this.predicate = predicate;
		this.speed = speed;
		this.range = range;
		this.setMutexBits(1);
	}

	@Override
	public boolean shouldExecute() {
		return !getEntities().isEmpty();
	}

	@Override
	public void startExecuting() {
		waterCost = entity.getPathPriority(PathNodeType.WATER);
	}

	@Override
	public boolean shouldContinueExecuting() {
		return true;
	}

	@Override
	public void resetTask() {
		pather.clearPath();
		entity.setPathPriority(PathNodeType.WATER, this.waterCost);
	}

	@Override
	public void updateTask() {
		if (--this.timeToRecalcPath <= 0)  {
			this.timeToRecalcPath = 5;
			BlockPos pos = findSafePos();
			pather.tryMoveToXYZ(pos.getX(), pos.getY(), pos.getZ(), speed);
		}
	}

	private BlockPos findSafePos() {
		Vec3d pos = entity.getPositionVector();
		Vec3d resultDir = new Vec3d(0, 0, 0);
		for (EntityLivingBase entity : getEntities()) {
			Vec3d dir = DirectionUtils.getDirectionVecXZ(this.entity, entity);
			resultDir = new Vec3d((dir.x + resultDir.x)/2, (dir.y + resultDir.y)/2, (dir.z + resultDir.z)/2);
		}
		return new BlockPos(pos.add(resultDir));
	}

	private List<EntityLivingBase> getEntities() {
		return world.getEntitiesWithinAABB(EntityLivingBase.class, new AxisAlignedBB(entity.posX-range, entity.posY-range, entity.posZ-range, entity.posX+range, entity.posY+range, entity.posZ+range), predicate);
	}

}
