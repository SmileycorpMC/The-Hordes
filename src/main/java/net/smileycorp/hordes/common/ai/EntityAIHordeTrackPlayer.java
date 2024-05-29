package net.smileycorp.hordes.common.ai;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.world.World;
import net.smileycorp.hordes.config.HordeEventConfig;

public class EntityAIHordeTrackPlayer extends EntityAIBase {
    
    protected final EntityLiving entity;
    protected final Entity target;
    protected final World world;
    protected final PathNavigate pather;
    protected int timeToRecalcPath;
    protected float waterCost;
    
    public EntityAIHordeTrackPlayer(EntityLiving entity, Entity target) {
        timeToRecalcPath = entity.getRNG().nextInt(HordeEventConfig.hordePathingInterval);
        this.entity = entity;
        world = entity.world;
        this.target = target;
        pather = entity.getNavigator();
        setMutexBits(1);
    }
    
    @Override
    public void startExecuting() {
        waterCost = entity.getPathPriority(PathNodeType.WATER);
    }
    
    @Override
    public boolean shouldExecute() {
        return target != null && target.isEntityAlive();
    }
    
    @Override
    public boolean shouldContinueExecuting() {
        return shouldExecute();
    }
    
    @Override
    public void resetTask() {
        pather.clearPath();
        entity.setPathPriority(PathNodeType.WATER, waterCost);
    }
    
    @Override
    public void updateTask() {
        if (timeToRecalcPath-- <= 0)  {
            timeToRecalcPath = HordeEventConfig.hordePathingInterval;
            pather.tryMoveToXYZ(target.posX, target.posY, target.posZ, 1f);
        }
    }
    
}
