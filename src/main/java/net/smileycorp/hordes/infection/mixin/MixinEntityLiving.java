package net.smileycorp.hordes.infection.mixin;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.smileycorp.hordes.infection.InfectionRegister;

//@Mixin(EntityLiving.class)
public abstract class MixinEntityLiving extends EntityLivingBase {

	public MixinEntityLiving(World world) {
		super(world);
	}
	
	//@Inject(at=@At("HEAD"), method = "processInitialInteract(Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/util/EnumHand;)Z", cancellable = true)
	public void processInteract(EntityPlayer player, EnumHand hand/*, CallbackInfoReturnable<Boolean> callback*/) {
		ItemStack stack = player.getHeldItem(hand);
		if (InfectionRegister.isCure(stack) &! world.isRemote) {
			stack.interactWithEntity(player, this, hand);
			//callback.setReturnValue(true);
			//callback.cancel();
		}
	}
	
}
