package net.smileycorp.hordes.common.ai;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.pathfinding.PathNodeType;
import net.smileycorp.hordes.config.HordeEventConfig;

public class EntityAIHordeTrackPlayer extends EntityAIBase {
    
    protected final EntityLiving entity;
    protected final Entity target;
    protected final double speed;
    protected PathNavigate pather;
    protected int timeToRecalcPath;
    protected float waterCost;
    
    public EntityAIHordeTrackPlayer(EntityLiving entity, Entity target, double speed) {
        timeToRecalcPath = entity.getRNG().nextInt(HordeEventConfig.hordePathingInterval);
        this.entity = entity;
        this.target = target;
        this.speed = speed;
        pather = entity.getNavigator();
        setMutexBits(3);
    }
    
    @Override
    public boolean shouldExecute() {
        return target != null && target.isEntityAlive() && entity.getAttackTarget() == null;
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
        entity.setPathPriority(PathNodeType.WATER, waterCost);
    }
    
    @Override
    public void updateTask() {
        if (timeToRecalcPath-- <= 0) {
            timeToRecalcPath = HordeEventConfig.hordePathingInterval;
            pather = entity.getNavigator();
            pather.tryMoveToEntityLiving(target, speed);
        }
        
    }
    
}
