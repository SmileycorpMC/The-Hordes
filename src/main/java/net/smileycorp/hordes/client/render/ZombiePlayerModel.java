package net.smileycorp.hordes.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.AnimationUtils;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.smileycorp.hordes.common.entities.PlayerZombie;

import java.awt.*;
import java.util.Optional;
import java.util.UUID;

public class ZombiePlayerModel<T extends Zombie & PlayerZombie> extends PlayerModel<T> {
	protected boolean fixedArms = false;
	protected final Color colour;
	protected final boolean isDrowned;

	public ZombiePlayerModel(ModelPart part) {
		this(part, Color.WHITE, false);
	}

	public ZombiePlayerModel(ModelPart part, Color colour, boolean isDrowned) {
		super(part, false);
		this.colour = colour;
		this.isDrowned = isDrowned;
	}

	@Override
	public void prepareMobModel(T entity, float f1, float f2, float f3) {
		if (!fixedArms) {
			Optional<UUID> optional = entity.getPlayerUUID();
			if (optional.isPresent()) if (DefaultPlayerSkin.get(optional.get()).equals("slim")) slim = true;
			fixedArms = true;
		}
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
				leftArm.xRot = leftArm.xRot * 0.5F - (float)Math.PI;
				leftArm.yRot = 0.0F;
			}
			if (rightArmPose == HumanoidModel.ArmPose.THROW_SPEAR) {
				rightArm.xRot = rightArm.xRot * 0.5F - (float)Math.PI;
				rightArm.yRot = 0.0F;
			}
			if (swimAmount > 0.0F) {
				rightArm.xRot = rotlerpRad(swimAmount, rightArm.xRot, -2.5132742F) + swimAmount * 0.35F * Mth.sin(0.1F * age);
				leftArm.xRot = rotlerpRad(swimAmount, leftArm.xRot, -2.5132742F) - swimAmount * 0.35F * Mth.sin(0.1F * age);
				rightArm.zRot = rotlerpRad(swimAmount, rightArm.zRot, -0.15F);
				leftArm.zRot = rotlerpRad(swimAmount, leftArm.zRot, 0.15F);
				leftLeg.xRot -= swimAmount * 0.55F * Mth.sin(0.1F * age);
				rightLeg.xRot += swimAmount * 0.55F * Mth.sin(0.1F * age);
				head.xRot = 0.0F;
			}
		}
		leftPants.copyFrom(leftLeg);
		rightPants.copyFrom(rightLeg);
		leftSleeve.copyFrom(leftArm);
		rightSleeve.copyFrom(rightArm);
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer consumer, int packedLight, int packedOverlay, int c) {
		super.renderToBuffer(poseStack, consumer, packedLight, packedOverlay, FastColor.ARGB32.colorFromFloat(1, colour.getRed()/255f, colour.getGreen()/255f, colour.getBlue()/255f));
	}

}
