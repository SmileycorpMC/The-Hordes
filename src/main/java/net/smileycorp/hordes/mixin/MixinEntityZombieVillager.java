package net.smileycorp.hordes.mixin;

import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.monster.EntityZombieVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.smileycorp.hordes.config.CommonConfigHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityZombieVillager.class)
public abstract class MixinEntityZombieVillager extends EntityZombie {

	public MixinEntityZombieVillager(World worldIn) {
		super(worldIn);
	}

	@Inject(at=@At("HEAD"), method = "processInteract(Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/util/EnumHand;)Z", cancellable = true)
	public void processInteract(EntityPlayer player, EnumHand hand, CallbackInfoReturnable<Boolean> callback) {
		if (!CommonConfigHandler.zombieVillagersCanBeCured) {
			callback.setReturnValue(super.processInteract(player, hand));
			callback.cancel();
		}
	}

}
