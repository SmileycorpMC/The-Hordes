package net.smileycorp.hordes.mixin;

import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.AbstractHorse;
import net.minecraft.entity.passive.EntityZombieHorse;
import net.minecraft.world.World;
import net.smileycorp.hordes.common.ConfigHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityZombieHorse.class)
public abstract class MixinEntityZombieHorse extends AbstractHorse implements IMob {

	public MixinEntityZombieHorse(World worldIn) {
		super(worldIn);
	}

	@Inject(at=@At("TAIL"), method = "applyEntityAttributes()V", cancellable = true)
	protected void applyEntityAttributes(CallbackInfo callback) {
		if (ConfigHandler.aggressiveZombieHorses) {
			getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(35.0D);
			getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(3.0D);
		}
	}

}
