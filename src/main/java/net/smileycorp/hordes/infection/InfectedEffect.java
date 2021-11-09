package net.smileycorp.hordes.infection;

import java.util.List;
import java.util.UUID;

import net.minecraft.client.gui.DisplayEffectsScreen;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.AttributeModifierManager;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.EffectType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.smileycorp.hordes.common.CommonConfigHandler;
import net.smileycorp.hordes.common.ModDefinitions;

import com.mojang.blaze3d.matrix.MatrixStack;

public class InfectedEffect extends Effect {

	public static final ResourceLocation TEXTURE = ModDefinitions.getResource("textures/gui/potions.png");

	private final UUID SPEED_MOD_UUID = UUID.fromString("05d68949-cb8b-4031-92a6-bd75e42b5cdd");
	private final String SPEED_MOD_NAME = ModDefinitions.getName("Infected");
	private final AttributeModifier SPEED_MOD = new AttributeModifier(SPEED_MOD_NAME, -0.1, AttributeModifier.Operation.MULTIPLY_TOTAL);

	public InfectedEffect() {
		super(EffectType.HARMFUL, 0x00440002);
		String name = "Infected";
		setRegistryName(ModDefinitions.getResource(name));
	}

	@Override
    public boolean shouldRender(EffectInstance effect) {
        return true;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void renderInventoryEffect(EffectInstance effect, DisplayEffectsScreen<?> gui, MatrixStack mStack, int x, int y, float z) {

    }

    @Override
	public List<ItemStack> getCurativeItems() {
    	return CommonConfigHandler.enableMobInfection.get() ? InfectionRegister.getCureList() : super.getCurativeItems();
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
    	if (entity instanceof PlayerEntity) {
            ((PlayerEntity)entity).causeFoodExhaustion(0.007F * (amplifier+1));
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
    	return CommonConfigHandler.infectHunger.get();
    }

    @Override
	public void addAttributeModifiers(LivingEntity entity,  AttributeModifierManager map, int amplifier) {
        if (amplifier > 0 && CommonConfigHandler.infectSlowness.get()) {
        	ModifiableAttributeInstance attribute = map.getInstance(Attributes.MOVEMENT_SPEED);
        	if (attribute != null) {
        		attribute.removeModifier(SPEED_MOD_UUID);
        		attribute.addPermanentModifier(new AttributeModifier(SPEED_MOD_UUID, SPEED_MOD_NAME + " " + amplifier, this.getAttributeModifierValue(amplifier-1, SPEED_MOD), AttributeModifier.Operation.MULTIPLY_TOTAL));
            }
        }
    }

    @Override
	public void removeAttributeModifiers(LivingEntity entity, AttributeModifierManager map, int amplifier) {
    	ModifiableAttributeInstance attribute = map.getInstance(Attributes.MOVEMENT_SPEED);
    	if (attribute != null) attribute.removeModifier(SPEED_MOD_UUID);
    }

}
