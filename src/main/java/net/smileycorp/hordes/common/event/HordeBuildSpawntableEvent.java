package net.smileycorp.hordes.common.event;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.smileycorp.atlas.api.recipe.WeightedOutputs;

public class HordeBuildSpawntableEvent extends PlayerEvent {
	
	protected final EntityPlayer entityPlayer;
	public final WeightedOutputs<Class<? extends EntityLiving>> spawntable;
	public BlockPos pos;
	
	public HordeBuildSpawntableEvent(EntityPlayer player, WeightedOutputs<Class<? extends EntityLiving>> spawntable, BlockPos pos) {
		super(player);
		this.entityPlayer = player;
		this.spawntable = spawntable;
		this.pos=pos;
	}
	
	public World getEntityWorld() {
		return entityPlayer.world;
	}

}
