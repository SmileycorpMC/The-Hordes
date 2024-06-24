package net.smileycorp.hordes.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.neoforged.neoforge.common.extensions.IItemStackExtension;
import net.smileycorp.hordes.infection.InfectionEventHandler;
import net.smileycorp.hordes.infection.data.InfectionData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

@Mixin(ItemStack.class)
public abstract class MixinItemStack implements IItemStackExtension {
    
    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;forEachModifier(Lnet/minecraft/world/entity/EquipmentSlotGroup;Ljava/util/function/BiConsumer;)V", shift = At.Shift.AFTER), method = "addAttributeTooltips")
    public void hordes$addAttributeTooltips(Consumer<Component> adder, Player player, CallbackInfo callback, @Local EquipmentSlotGroup group) {
        float amount = InfectionData.INSTANCE.getProtectionMultiplier((ItemStack)(Object)this);
        if (amount <= 0) return;
        EquipmentSlot slot = InfectionEventHandler.getSlot((ItemStack)(Object)this);
        if (slot == null) return;
        if (group == EquipmentSlotGroup.bySlot(slot))
            adder.accept(Component.translatable("tooltip.hordes.wearableProtection", "+"
            + ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(amount * 100) + "%").withStyle(ChatFormatting.BLUE));
    }
    
}
