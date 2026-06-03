package net.smileycorp.hordes.mixin;

import com.google.common.collect.Multimap;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.smileycorp.atlas.api.data.Pair;
import net.smileycorp.hordes.common.Constants;
import net.smileycorp.hordes.common.HordesLogger;
import net.smileycorp.hordes.config.InfectionConfig;
import net.smileycorp.hordes.config.data.HordesJsonLoader;
import net.smileycorp.hordes.config.data.infection.InfectionData;
import net.smileycorp.hordes.infection.HordesInfection;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ItemStack.class)
public class MixinItemStack {

    @Shadow @Final private Item item;

    @WrapOperation(at = @At(value = "INVOKE", target = "Lnet/minecraft/item/Item;getAttributeModifiers(Lnet/minecraft/inventory/EntityEquipmentSlot;Lnet/minecraft/item/ItemStack;)Lcom/google/common/collect/Multimap;", remap = false), method = "getAttributeModifiers")
    public Multimap<String, AttributeModifier> getAttributeModifiers$getAttributeModifiers(Item instance, EntityEquipmentSlot slot, ItemStack stack, Operation<Multimap<String, AttributeModifier>> original) {
        Multimap<String, AttributeModifier> map = original.call(instance, slot, stack);
        if (InfectionData.INSTANCE == null || item == null) return map;
        if (InfectionConfig.enableMobInfection && slot == item.getEquipmentSlot(stack)) {
            Pair<Float, Byte> pair = InfectionData.INSTANCE.getProtection(stack);
            if (pair == null) return map;
            map.put(HordesInfection.INFECTION_RESISTANCE.getName(), new AttributeModifier(Constants.locStr(slot.getName()), pair.getFirst(), pair.getSecond()));
        }
        return map;
    }

}
