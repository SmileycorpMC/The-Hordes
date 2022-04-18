package net.smileycorp.hordes.client;

import java.awt.Color;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.world.entity.Mob;
import net.smileycorp.hordes.common.entities.IZombiePlayer;

public class ZombiePlayerModel<T extends Mob & IZombiePlayer> extends PlayerModel<T> {

	protected boolean fixedArms = false;
	protected final Color colour;

	public ZombiePlayerModel() {
		this(Color.WHITE);
	}

	public ZombiePlayerModel(Color colour) {
		super(0.0f, false);
		this.colour = colour;
	}

	@Override
	public void prepareMobModel(T entity, float f1, float f2, float f3) {
		if (!fixedArms) {
			if (DefaultPlayerSkin.getSkinModelName(((IZombiePlayer) entity).getPlayerUUID()).equals("slim")) {
				leftArm = new ModelRenderer(this, 32, 48);
				leftArm.addBox(-1.0F, -2.0F, -2.0F, 3, 12, 4, 0.0f);
				leftArm.setPos(5.0F, 2.5F, 0.0F);
				rightArm = new ModelRenderer(this, 40, 16);
				rightArm.addBox(-2.0F, -2.0F, -2.0F, 3, 12, 4, 0.0f);
				rightArm.setPos(-5.0F, 2.5F, 0.0F);
			}
			fixedArms = true;
		}
		super.prepareMobModel(entity, f1, f2, f3);
	}

	@Override
	public void setupAnim(T entity, float p_225597_2_, float p_225597_3_, float p_225597_4_, float p_225597_5_, float p_225597_6_) {
		super.setupAnim(entity, p_225597_2_, p_225597_3_, p_225597_4_, p_225597_5_, p_225597_6_);
		ModelHelper.animateZombieArms(leftArm, rightArm, entity.isAggressive(), attackTime, p_225597_4_);
		leftPants.copyFrom(leftLeg);
		rightPants.copyFrom(rightLeg);
		leftSleeve.copyFrom(leftArm);
		rightSleeve.copyFrom(rightArm);
	}

	@Override
	public void renderToBuffer(PoseStack matrixStack, IVertexBuilder buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		super.renderToBuffer(matrixStack, buffer, packedLight, packedOverlay, colour.getRed()/255f, colour.getGreen()/255f, colour.getBlue()/255f, 1);
	}

}
