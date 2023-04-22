package net.smileycorp.hordes.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.monster.EntityZombieVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.smileycorp.hordes.common.ConfigHandler;

@Mixin(EntityZombieVillager.class)
public abstract class MixinEntityZombieVillager extends EntityZombie {

	public MixinEntityZombieVillager(World worldIn) {
		super(worldIn);
	}

	@Inject(at=@At("HEAD"), method = "processInteract(Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/util/EnumHand;)Z", cancellable = true)
	public void processInteract(EntityPlayer player, EnumHand hand, CallbackInfoReturnable<Boolean> callback) {
		if (!ConfigHandler.zombieVillagersCanBeCured) {
			callback.setReturnValue(super.processInteract(player, hand));
			callback.cancel();
		}
	}

}
