package net.smileycorp.hordes.mixin;

import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityZombieHorse;
import net.minecraft.world.World;
import net.smileycorp.hordes.common.ConfigHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityAnimal.class)
public abstract class MixinEntityAnimal extends EntityAgeable {

	public MixinEntityAnimal(World worldIn) {
		super(worldIn);
	}

	@Inject(at=@At("TAIL"), method = "canDespawn()Z", cancellable = true)
	protected void canDespawn(CallbackInfoReturnable<Boolean> callback) {
		if (((EntityAgeable)this) instanceof EntityZombieHorse && ConfigHandler.aggressiveZombieHorses) {
			callback.setReturnValue(true);
			callback.cancel();
		}
	}

}
