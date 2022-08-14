package net.smileycorp.hordes.client.render;

import java.awt.Color;
import java.util.Optional;
import java.util.UUID;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.model.AnimationUtils;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.world.entity.monster.Zombie;
import net.smileycorp.hordes.common.entities.IZombiePlayer;

public class ZombiePlayerModel<T extends Zombie & IZombiePlayer> extends PlayerModel<T> {

	protected boolean fixedArms = false;
	protected final Color colour;

	public ZombiePlayerModel(ModelPart part) {
		this(part, Color.WHITE);
	}

	public ZombiePlayerModel(ModelPart part, Color colour) {
		super(part, false);
		this.colour = colour;
	}

	@Override
	public void prepareMobModel(T entity, float f1, float f2, float f3) {
		if (!fixedArms) {
			Optional<UUID> optional = entity.getPlayerUUID();
			if (optional.isPresent()) if (DefaultPlayerSkin.getSkinModelName(optional.get()).equals("slim")) {
				slim = true;
			}
			fixedArms = true;
		}
		super.prepareMobModel(entity, f1, f2, f3);
	}

	@Override
	public void setupAnim(T entity, float p_225597_2_, float p_225597_3_, float p_225597_4_, float p_225597_5_, float p_225597_6_) {
		super.setupAnim(entity, p_225597_2_, p_225597_3_, p_225597_4_, p_225597_5_, p_225597_6_);
		AnimationUtils.animateZombieArms(leftArm, rightArm, entity.isAggressive(), attackTime, p_225597_4_);
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
