package net.smileycorp.hordes.common.entities;

import com.google.common.base.Optional;
import com.google.common.base.Supplier;
import com.google.common.collect.Maps;
import com.mojang.authlib.GameProfile;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.smileycorp.atlas.api.util.Func;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public interface PlayerZombie<T extends EntityMob & PlayerZombie<T>> {

	void setPlayer(EntityPlayer player);

	void setPlayer(String username);

	void setPlayer(UUID uuid);

	void setPlayer(GameProfile profile);

	Optional<UUID> getPlayerUUID();

	void storeDrops(Collection<EntityItem> drops);

	void setInventory(NonNullList<ItemStack> drops);

	NonNullList<ItemStack> getInventory();

	void setDisplayCape(boolean display);

	boolean displayCape();

	double getXCloakO();

	double getYCloakO();

	double getZCloakO();

	double getXCloak();

	double getYCloak();

	double getZCloak();

	void setXCloakO(double value);

	void setYCloakO(double value);

	void setZCloakO(double value);

	void setXCloak(double value);

	void setYCloak(double value);

	void setZCloak(double value);

	default void moveCloak() {
		T entity = asEntity();
		setXCloakO(getXCloak());
		setYCloakO(getYCloak());
		setZCloakO(getZCloak());
		double d0 = entity.posX - getXCloak();
		double d1 = entity.posY - getYCloak();
		double d2 = entity.posZ - getZCloak();
		if (d0 > 10.0D) {
			setXCloak(entity.posX);
			setXCloakO(getXCloak());
		}

		if (d2 > 10.0D) {
			setZCloak(entity.posZ);
			setZCloakO(getZCloak());
		}

		if (d1 > 10.0D) {
			setYCloak(entity.posY);
			setYCloakO(getYCloak());
		}

		if (d0 < -10.0D) {
			setXCloak(entity.posX);
			setXCloakO(getXCloak());
		}

		if (d2 < -10.0D) {
			setZCloak(entity.posZ);
			setZCloakO(getZCloak());
		}

		if (d1 < -10.0D) {
			setYCloak(entity.posY);
			setYCloakO(getYCloak());
		}

		setXCloak(getXCloak() + (d0 * 0.25D));
		setYCloak(getYCloak() + (d1 * 0.25D));
		setZCloak(getZCloak() + (d2 * 0.25D));
	}

	default <U extends EntityMob & PlayerZombie<U>> void copyFrom(U entity) {
		Optional<UUID> optional = entity.getPlayerUUID();
		if(optional.isPresent()) setPlayer(optional.get());
		setInventory(entity.getInventory());
		for (EntityEquipmentSlot slot : EntityEquipmentSlot.values()) {
			ItemStack stack = entity.getItemStackFromSlot(slot);
			asEntity().setItemStackToSlot(slot, stack);
		}
		setDisplayCape(entity.displayCape());
	}

	default T asEntity() {
		return (T) this;
	}

	Type<T> getType();

	class Type<T extends EntityMob & PlayerZombie<T>> {

		private static final Map<String, Type<?>> VALUES = Maps.newLinkedHashMap();

		private final Class<T> type;
		private final Supplier<Boolean> predicate;

		public Type(String name, Class<T> type) {
			this(name, type, Func::True);
		}

		public Type(String name, Class<T> type, Supplier<Boolean> predicate) {
			this.type = type;
			this.predicate = predicate;
			VALUES.put(name, this);
		}

		public T create(World level) {
			try {
				return type.getConstructor(World.class).newInstance(level);
			} catch (Exception e) {
				return null;
			}
		}

		public static List<Type<?>> values() {
			return VALUES.values().stream().filter(type -> type.predicate.get())
					.collect(Collectors.toList());
		}

		public static List<String> names() {
			return VALUES.entrySet().stream().filter(entry -> entry.getValue().predicate.get())
					.map(Map.Entry::getKey).collect(Collectors.toList());
		}

		public static Type<?> get(String name) {
			return VALUES.get(name);
		}

	}

}
