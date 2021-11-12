package net.smileycorp.hordes.client;

import java.awt.Color;

import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.client.renderer.model.ModelHelper;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.entity.MobEntity;
import net.smileycorp.hordes.common.entities.IZombiePlayer;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;

public class ZombiePlayerModel<T extends MobEntity & IZombiePlayer> extends PlayerModel<T> {

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
				this.leftArm = new ModelRenderer(this, 32, 48);
		        this.leftArm.addBox(-1.0F, -2.0F, -2.0F, 3, 12, 4, 0.0f);
		        this.leftArm.setPos(5.0F, 2.5F, 0.0F);
		        this.rightArm = new ModelRenderer(this, 40, 16);
		        this.rightArm.addBox(-2.0F, -2.0F, -2.0F, 3, 12, 4, 0.0f);
		        this.rightArm.setPos(-5.0F, 2.5F, 0.0F);
			}
			fixedArms = true;
		}
		super.prepareMobModel(entity, f1, f2, f3);
	}

	@Override
	public void setupAnim(T entity, float p_225597_2_, float p_225597_3_, float p_225597_4_, float p_225597_5_, float p_225597_6_) {
        super.setupAnim(entity, p_225597_2_, p_225597_3_, p_225597_4_, p_225597_5_, p_225597_6_);
        ModelHelper.animateZombieArms(this.leftArm, this.rightArm, entity.isAggressive(), this.attackTime, p_225597_4_);
        leftPants.copyFrom(this.leftLeg);
        rightPants.copyFrom(this.rightLeg);
        leftSleeve.copyFrom(this.leftArm);
        rightSleeve.copyFrom(this.rightArm);
    }

	@Override
	public void renderToBuffer(MatrixStack matrixStack, IVertexBuilder buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		super.renderToBuffer(matrixStack, buffer, packedLight, packedOverlay, colour.getRed()/255f, colour.getGreen()/255f, colour.getBlue()/255f, 1);
	}

}
