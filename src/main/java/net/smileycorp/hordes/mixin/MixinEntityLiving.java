package net.smileycorp.hordes.mixin;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.smileycorp.hordes.config.data.infection.InfectionDataLoader;
import net.smileycorp.hordes.infection.HordesInfection;
import net.smileycorp.hordes.infection.network.CureEntityMessage;
import net.smileycorp.hordes.infection.network.InfectionPacketHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityLiving.class)
public abstract class MixinEntityLiving extends EntityLivingBase {

	public MixinEntityLiving(World world) {
		super(world);
	}

	@Inject(at=@At("HEAD"), method = "processInitialInteract(Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/util/EnumHand;)Z", cancellable = true)
	public void processInteract(EntityPlayer player, EnumHand hand, CallbackInfoReturnable<Boolean> callback) {
		ItemStack stack = player.getHeldItem(hand);
		if (!isPotionActive(HordesInfection.INFECTED)) return;
		if (!InfectionDataLoader.INSTANCE.isCure(stack)) return;
		removePotionEffect(HordesInfection.INFECTED);
		if (!player.world.isRemote) InfectionPacketHandler.send(this, new CureEntityMessage(this));
		if (!player.isCreative()) {
			ItemStack container = stack.getItem().getContainerItem(stack);
			if (stack.isItemStackDamageable())stack.damageItem(1, player);
			else stack.shrink(1);
			if (stack.isEmpty() && !container.isEmpty()) player.setHeldItem(hand, container);
		}
		callback.setReturnValue(world.isRemote);
	}

}
