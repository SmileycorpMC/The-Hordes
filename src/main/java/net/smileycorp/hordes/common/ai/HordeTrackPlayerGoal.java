package net.smileycorp.hordes.common.ai;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.pathfinder.PathType;
import net.smileycorp.hordes.config.HordeEventConfig;

import java.util.EnumSet;

public class HordeTrackPlayerGoal extends Goal {

    protected final Mob entity;
    protected final Entity target;
    protected final double speed;
    protected PathNavigation pather;
    protected int timeToRecalcPath;
    protected float waterCost;

    public HordeTrackPlayerGoal(Mob entity, Entity target, double speed) {
        timeToRecalcPath = entity.getRandom().nextInt(HordeEventConfig.hordePathingInterval.get());
        this.entity = entity;
        this.target = target;
        this.speed = speed;
        pather = entity.getNavigation();
        setFlags(EnumSet.of(Flag.LOOK, Flag.MOVE));
    }

    public boolean canUse() {
        return target != null && target.isAlive() && entity.getTarget() == null;
    }

    public void start() {
        waterCost = entity.getPathfindingMalus(PathType.WATER);
        entity.setPathfindingMalus(PathType.WATER, 0);
    }

    public boolean canContinueToUse() {
        return true;
    }

    public void stop() {
        pather.stop();
        entity.setPathfindingMalus(PathType.WATER, waterCost);
    }

    public void tick() {
        if (timeToRecalcPath-- <= 0) {
            timeToRecalcPath = HordeEventConfig.hordePathingInterval.get();
            pather = entity.getNavigation();
            pather.moveTo(target, speed);
        }

    }

}
