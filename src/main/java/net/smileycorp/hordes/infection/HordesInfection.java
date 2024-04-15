package net.smileycorp.hordes.infection;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Effect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.common.ForgeTagHandler;
import net.minecraftforge.common.Tags;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.smileycorp.hordes.common.Constants;

import java.util.List;
import java.util.stream.Collectors;

public class HordesInfection {

	public static final DeferredRegister<Effect> EFFECTS = DeferredRegister.create(ForgeRegistries.POTIONS, Constants.MODID);

	public static final Tags.IOptionalNamedTag<EntityType<?>> INFECTION_ENTITIES_TAG = ForgeTagHandler.createOptionalTag(Registry.ENTITY_TYPE_REGISTRY.getRegistryName(), Constants.loc("infection_entities"));
	public static final Tags.IOptionalNamedTag<Item> INFECTION_CURES_TAG = ForgeTagHandler.createOptionalTag(Registry.ITEM_REGISTRY.getRegistryName(), Constants.loc("infection_cures"));
	
	public static final RegistryObject<Effect> INFECTED = EFFECTS.register("infected", InfectedEffect::new);
	public static final RegistryObject<Effect> IMMUNITY = EFFECTS.register("immunity", ImmuneEffect::new);

	public static DamageSource INFECTION_DAMAGE = new DamageSourceInfection();
	
	public static List<ItemStack> getCureList() {
		return INFECTION_CURES_TAG.getValues()
				.stream().map(ItemStack::new).collect(Collectors.toList());
	}
	
	public static boolean isCure(ItemStack stack) {
		return stack.getItem().is(INFECTION_CURES_TAG);
	}
	
	public static boolean canCauseInfection(LivingEntity entity) {
		if (entity == null) return false;
		return entity.getType().is(INFECTION_ENTITIES_TAG);
	}

}
