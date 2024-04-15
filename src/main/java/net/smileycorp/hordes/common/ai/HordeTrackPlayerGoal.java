package net.smileycorp.hordes.common.ai;

import net.minecraft.entity.Entity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.pathfinding.PathNavigator;
import net.minecraft.pathfinding.PathNodeType;
import net.smileycorp.hordes.config.HordeEventConfig;

import java.util.EnumSet;

public class HordeTrackPlayerGoal extends Goal {

    protected final MobEntity entity;
    protected final Entity target;
    protected final double speed;
    protected PathNavigator pather;
    protected int timeToRecalcPath;
    protected float waterCost;

    public HordeTrackPlayerGoal(MobEntity entity, Entity target, double speed) {
        timeToRecalcPath = entity.getRandom().nextInt(HordeEventConfig.hordePathingInterval.get());
        this.entity = entity;
        this.target = target;
        this.speed = speed;
        pather = entity.getNavigation();
        this.setFlags(EnumSet.of(Flag.LOOK, Flag.MOVE));
    }

    public boolean canUse() {
        return target != null && target.isAlive() && entity.getTarget() == null;
    }

    public void start() {
        waterCost = entity.getPathfindingMalus(PathNodeType.WATER);
    }

    public boolean canContinueToUse() {
        return true;
    }

    public void stop() {
        pather.stop();
        entity.setPathfindingMalus(PathNodeType.WATER, waterCost);
    }

    public void tick() {
        if (timeToRecalcPath-- <= 0) {
            timeToRecalcPath = HordeEventConfig.hordePathingInterval.get();
            pather = entity.getNavigation();
            pather.moveTo(pather.createPath(target.blockPosition(), 1), speed);
        }

    }

}
