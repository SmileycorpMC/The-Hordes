package net.smileycorp.hordes.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.AnimationUtils;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.smileycorp.hordes.common.entities.PlayerZombie;

import java.awt.*;

public class ZombiePlayerModel<T extends Zombie & PlayerZombie> extends PlayerModel<T> {

	protected final Color colour;
	protected final boolean isDrowned;

	public ZombiePlayerModel(ModelPart part) {
		this(part, Color.WHITE, false, false);
	}

	public ZombiePlayerModel(ModelPart part, Color colour, boolean isDrowned, boolean slim) {
		super(part, slim);
		this.colour = colour;
		this.isDrowned = isDrowned;
	}

	@Override
	public void prepareMobModel(T entity, float f1, float f2, float f3) {
		if (isDrowned) {
			rightArmPose = HumanoidModel.ArmPose.EMPTY;
			leftArmPose = HumanoidModel.ArmPose.EMPTY;
			ItemStack itemstack = entity.getItemInHand(InteractionHand.MAIN_HAND);
			if (itemstack.is(Items.TRIDENT) && entity.isAggressive()) {
				if (entity.getMainArm() == HumanoidArm.RIGHT) rightArmPose = HumanoidModel.ArmPose.THROW_SPEAR;
				else leftArmPose = HumanoidModel.ArmPose.THROW_SPEAR;
			}
		}
		super.prepareMobModel(entity, f1, f2, f3);
	}

	@Override
	public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float age, float headYaw, float headPitch) {
		super.setupAnim(entity, limbSwing, limbSwingAmount, age, headYaw, headPitch);
		AnimationUtils.animateZombieArms(leftArm, rightArm, entity.isAggressive(), attackTime, age);
		if (isDrowned) {
			if (leftArmPose == HumanoidModel.ArmPose.THROW_SPEAR) {
				leftArm.xRot = leftArm.xRot * 0.5f - (float)Math.PI;
				leftArm.yRot = 0.0f;
			}
			if (rightArmPose == HumanoidModel.ArmPose.THROW_SPEAR) {
				rightArm.xRot = rightArm.xRot * 0.5f - (float)Math.PI;
				rightArm.yRot = 0.0f;
			}
			if (swimAmount > 0.0f) {
				rightArm.xRot = rotlerpRad(swimAmount, rightArm.xRot, -2.5132742f) + swimAmount * 0.35f * Mth.sin(0.1f * age);
				leftArm.xRot = rotlerpRad(swimAmount, leftArm.xRot, -2.5132742f) - swimAmount * 0.35f * Mth.sin(0.1f * age);
				rightArm.zRot = rotlerpRad(swimAmount, rightArm.zRot, -0.15f);
				leftArm.zRot = rotlerpRad(swimAmount, leftArm.zRot, 0.15f);
				leftLeg.xRot -= swimAmount * 0.55f * Mth.sin(0.1f * age);
				rightLeg.xRot += swimAmount * 0.55f * Mth.sin(0.1f * age);
				head.xRot = 0.0f;
			}
		}
		leftPants.copyFrom(leftLeg);
		rightPants.copyFrom(rightLeg);
		leftSleeve.copyFrom(leftArm);
		rightSleeve.copyFrom(rightArm);
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer consumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		super.renderToBuffer(poseStack, consumer, packedLight, packedOverlay, colour.getRed() / 255f, colour.getGreen() / 255f, colour.getBlue() / 255f, 1);
	}

}
