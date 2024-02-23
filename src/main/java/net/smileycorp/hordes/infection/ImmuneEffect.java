package net.smileycorp.hordes.infection;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.neoforged.neoforge.common.EffectCure;

import java.util.Set;

public class ImmuneEffect extends MobEffect {

    public ImmuneEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x00923A89);
    }
    
    @Override
    public void fillEffectCures(Set<EffectCure> cures, MobEffectInstance effectInstance) {}

}
