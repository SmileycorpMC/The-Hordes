package net.smileycorp.hordes.gibbing;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.smileycorp.hordes.common.ModDefinitions;

public class GibbingEventHandler {
	
	@SubscribeEvent
	public void attachCapabilities(AttachCapabilitiesEvent<Entity> event) {
		if (event.getObject() instanceof EntityZombie) {
			event.addCapability(ModDefinitions.getResource("Gibbing"), new ZombieGibbingProvider(event.getObject()));
		}
	}
	
	@SubscribeEvent
	public void onHurt(LivingHurtEvent event) {
		EntityLivingBase entity = event.getEntityLiving();
		World world = entity.world;
		DamageSource source = event.getSource();
		if (!world.isRemote && entity.hasCapability(ZombieGibbingProvider.GIBBING, EnumFacing.UP)) {
			Entity attacker = source.getImmediateSource();
			if (attacker != null) {
				AxisAlignedBB hurtbox = attacker.getEntityBoundingBox();
				AxisAlignedBB box = entity.getEntityBoundingBox();
				//AxisAlignedBB headbox = box.contract(1, (box.minX - box.maxX), 1);
				IZombieGibbing gibbing = entity.getCapability(ZombieGibbingProvider.GIBBING, EnumFacing.UP);
				if (hurtbox.intersects(box)) {
					gibbing.removePart(EnumBodyPart.HEAD);
				}
			}
		}
	}
}
