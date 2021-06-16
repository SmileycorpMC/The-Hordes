package net.smileycorp.hordes.client;

import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.smileycorp.hordes.infection.entities.EntityZombiePlayer;

public class ModelZombiePlayer extends ModelPlayer {
	
	protected boolean fixedArms = false;
	
	public ModelZombiePlayer() {
		super(0.0f, false);
	}
	
	@Override
	public void render(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
		if (!fixedArms) {
			if (DefaultPlayerSkin.getSkinType(((EntityZombiePlayer) entity).getPlayerUUID()).equals("slim")) {
				this.bipedLeftArm = new ModelRenderer(this, 32, 48);
		        this.bipedLeftArm.addBox(-1.0F, -2.0F, -2.0F, 3, 12, 4, 0.0f);
		        this.bipedLeftArm.setRotationPoint(5.0F, 2.5F, 0.0F);
		        this.bipedRightArm = new ModelRenderer(this, 40, 16);
		        this.bipedRightArm.addBox(-2.0F, -2.0F, -2.0F, 3, 12, 4, 0.0f);
		        this.bipedRightArm.setRotationPoint(-5.0F, 2.5F, 0.0F);
			}
			fixedArms = true;
		}
		super.render(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
	}
	
	@Override
	public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn) {
        super.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, entityIn);
        boolean flag = ((EntityZombiePlayer)entityIn).isArmsRaised();
        float f = MathHelper.sin(this.swingProgress * (float)Math.PI);
        float f1 = MathHelper.sin((1.0F - (1.0F - this.swingProgress) * (1.0F - this.swingProgress)) * (float)Math.PI);
        this.bipedRightArm.rotateAngleZ = 0.0F;
        this.bipedLeftArm.rotateAngleZ = 0.0F;
        this.bipedRightArm.rotateAngleY = -(0.1F - f * 0.6F);
        this.bipedLeftArm.rotateAngleY = 0.1F - f * 0.6F;
        float f2 = -(float)Math.PI / (flag ? 1.5F : 2.25F);
        this.bipedRightArm.rotateAngleX = f2;
        this.bipedLeftArm.rotateAngleX = f2;
        this.bipedRightArm.rotateAngleX += f * 1.2F - f1 * 0.4F;
        this.bipedLeftArm.rotateAngleX += f * 1.2F - f1 * 0.4F;
        this.bipedRightArm.rotateAngleZ += MathHelper.cos(ageInTicks * 0.09F) * 0.05F + 0.05F;
        this.bipedLeftArm.rotateAngleZ -= MathHelper.cos(ageInTicks * 0.09F) * 0.05F + 0.05F;
        this.bipedRightArm.rotateAngleX += MathHelper.sin(ageInTicks * 0.067F) * 0.05F;
        this.bipedLeftArm.rotateAngleX -= MathHelper.sin(ageInTicks * 0.067F) * 0.05F;
    }

}
