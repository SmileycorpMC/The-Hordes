package net.smileycorp.hordes.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.passive.EntityZombieHorse;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import net.smileycorp.hordes.config.CommonConfigHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityLivingBase.class)
public abstract class MixinEntityLivingBase extends Entity {

	public MixinEntityLivingBase(World worldIn) {
		super(worldIn);
	}

	@Shadow
	public abstract IAttributeInstance getEntityAttribute(IAttribute attribute);

	@Inject(at=@At("RETURN"), method = "attackEntityAsMob(Lnet/minecraft/entity/Entity;)Z", cancellable = true)
	public void hordes$attackEntityAsMob(Entity entity, CallbackInfoReturnable<Boolean> callback) {
		if (!(((Entity)this) instanceof EntityZombieHorse) |! CommonConfigHandler.aggressiveZombieHorses) return;
		if (!entity.attackEntityFrom(DamageSource.causeMobDamage((EntityLivingBase)(Entity)this),
				(float) getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue())) return;
		callback.setReturnValue(true);
	}

}
