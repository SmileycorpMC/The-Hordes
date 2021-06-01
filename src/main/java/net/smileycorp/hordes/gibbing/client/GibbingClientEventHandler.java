package net.smileycorp.hordes.gibbing.client;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.smileycorp.hordes.gibbing.EnumBodyPart;
import net.smileycorp.hordes.gibbing.IZombieGibbing;
import net.smileycorp.hordes.gibbing.ZombieGibbingProvider;

public class GibbingClientEventHandler {
	
	@SubscribeEvent
	public void renderEntity(RenderLivingEvent.Post<EntityZombie> event) {
		EntityLivingBase entity = event.getEntity();
		RenderLivingBase renderer = event.getRenderer();
		if (renderer.getMainModel() instanceof ModelBiped) {
			ModelBiped model = (ModelBiped) renderer.getMainModel();
			if (entity.hasCapability(ZombieGibbingProvider.GIBBING, EnumFacing.UP)) {
				IZombieGibbing gibbing = entity.getCapability(ZombieGibbingProvider.GIBBING, EnumFacing.UP);
				for (EnumBodyPart part : EnumBodyPart.values()) {
					if (!gibbing.hasBodyPart(part)) {
						switch (part) {
						case HEAD:
							model.bipedHead.isHidden=true;
							model.bipedHeadwear.isHidden=true;
							break;
						case LEFT_ARM:
							model.bipedLeftArm.isHidden=true;
							break;
						case LEFT_LEG:
							model.bipedLeftLeg.isHidden=true;
							break;
						case RIGHT_ARM:
							model.bipedRightArm.isHidden=true;
							break;
						case RIGHT_LEG:
							model.bipedRightLeg.isHidden=true;
							break;
						default:
							break;
						}
					}
				}
			}
		}
	}
}
