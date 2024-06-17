package net.smileycorp.hordes.mixin;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.smileycorp.hordes.config.InfectionConfig;
import net.smileycorp.hordes.infection.HordesInfection;
import net.smileycorp.hordes.infection.PotionInfected;
import net.smileycorp.hordes.infection.network.InfectMessage;
import net.smileycorp.hordes.infection.network.InfectionPacketHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PotionEffect.class)
public class MixinPotionEffect {

	@Shadow
	private Potion potion;

	@Shadow
	private int duration;

	@Shadow
	private int amplifier;

	@Inject(at=@At("HEAD"), method = "onUpdate(Lnet/minecraft/entity/EntityLivingBase;)Z", cancellable = true)
	public void onUpdate(EntityLivingBase entity, CallbackInfoReturnable<Boolean> callback) {
		if (duration <= 1 && potion == HordesInfection.INFECTED && InfectionConfig.enableMobInfection) {
			if (amplifier < 3) {
				amplifier = amplifier + 1;
				duration = PotionInfected.getInfectionTime(entity);
				if (entity instanceof EntityPlayerMP) InfectionPacketHandler.sendTo(new InfectMessage(false), (EntityPlayerMP) entity);
				callback.setReturnValue(true);
			}
			else {
				entity.attackEntityFrom(HordesInfection.INFECTION_DAMAGE, Float.MAX_VALUE);
				callback.setReturnValue(false);
			}
		}
	}


	@Inject(at=@At("TAIL"), method = "readCustomPotionEffectFromNBT (Lnet/minecraft/nbt/NBTTagCompound;)Lnet/minecraft/potion/PotionEffect;", cancellable = true)
	private static void load(NBTTagCompound nbt, CallbackInfoReturnable<PotionEffect> callback) {
		PotionEffect effect = callback.getReturnValue();
		if (effect.getPotion() == HordesInfection.INFECTED) {
			if (effect.duration > InfectionConfig.ticksForEffectStage) {
				int d = effect.duration + InfectionConfig.ticksForEffectStage - 10000;
				if (d > 0) effect.duration = d;
				else effect.duration = InfectionConfig.ticksForEffectStage;
			}
		}
	}


}
