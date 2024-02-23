package net.smileycorp.hordes.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.AnimationUtils;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.PlayerSkin;
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
			if (optional.isPresent()) if (DefaultPlayerSkin.get(optional.get()).model() == PlayerSkin.Model.SLIM) {
				slim = true;
			}
			fixedArms = true;
		}
		if (isDrowned) {
			rightArmPose = HumanoidModel.ArmPose.EMPTY;
			leftArmPose = HumanoidModel.ArmPose.EMPTY;
			ItemStack itemstack = entity.getItemInHand(InteractionHand.MAIN_HAND);
			if (itemstack.is(Items.TRIDENT) && entity.isAggressive()) {
				if (entity.getMainArm() == HumanoidArm.RIGHT) {
					this.rightArmPose = HumanoidModel.ArmPose.THROW_SPEAR;
				} else {
					this.leftArmPose = HumanoidModel.ArmPose.THROW_SPEAR;
				}
			}
		}
		super.prepareMobModel(entity, f1, f2, f3);
	}

	@Override
	public void setupAnim(T entity, float p_225597_2_, float p_225597_3_, float p_225597_4_, float p_225597_5_, float p_225597_6_) {
		super.setupAnim(entity, p_225597_2_, p_225597_3_, p_225597_4_, p_225597_5_, p_225597_6_);
		AnimationUtils.animateZombieArms(leftArm, rightArm, entity.isAggressive(), attackTime, p_225597_4_);
		if (isDrowned) {
			if (this.leftArmPose == HumanoidModel.ArmPose.THROW_SPEAR) {
				this.leftArm.xRot = this.leftArm.xRot * 0.5F - (float)Math.PI;
				this.leftArm.yRot = 0.0F;
			}

			if (this.rightArmPose == HumanoidModel.ArmPose.THROW_SPEAR) {
				this.rightArm.xRot = this.rightArm.xRot * 0.5F - (float)Math.PI;
				this.rightArm.yRot = 0.0F;
			}

			if (this.swimAmount > 0.0F) {
				this.rightArm.xRot = this.rotlerpRad(this.swimAmount, this.rightArm.xRot, -2.5132742F) + this.swimAmount * 0.35F * Mth.sin(0.1F * p_225597_4_);
				this.leftArm.xRot = this.rotlerpRad(this.swimAmount, this.leftArm.xRot, -2.5132742F) - this.swimAmount * 0.35F * Mth.sin(0.1F * p_225597_4_);
				this.rightArm.zRot = this.rotlerpRad(this.swimAmount, this.rightArm.zRot, -0.15F);
				this.leftArm.zRot = this.rotlerpRad(this.swimAmount, this.leftArm.zRot, 0.15F);
				this.leftLeg.xRot -= this.swimAmount * 0.55F * Mth.sin(0.1F * p_225597_4_);
				this.rightLeg.xRot += this.swimAmount * 0.55F * Mth.sin(0.1F * p_225597_4_);
				this.head.xRot = 0.0F;
			}
		}
		leftPants.copyFrom(leftLeg);
		rightPants.copyFrom(rightLeg);
		leftSleeve.copyFrom(leftArm);
		rightSleeve.copyFrom(rightArm);
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer consumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		super.renderToBuffer(poseStack, consumer, packedLight, packedOverlay, colour.getRed()/255f, colour.getGreen()/255f, colour.getBlue()/255f, 1);
	}

}
