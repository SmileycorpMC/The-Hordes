package net.smileycorp.hordes.common.entities;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

import com.mojang.authlib.GameProfile;

import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.BaseComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Drowned;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraftforge.server.ServerLifecycleHooks;
import net.smileycorp.atlas.api.util.DataUtils;
import net.smileycorp.hordes.common.infection.HordesInfection;


public class DrownedPlayer extends Drowned implements IZombiePlayer {

	protected static final EntityDataAccessor<Optional<UUID>> PLAYER_UUID = SynchedEntityData.defineId(DrownedPlayer.class, EntityDataSerializers.OPTIONAL_UUID);

	protected NonNullList<ItemStack> playerItems = NonNullList.<ItemStack>create();
	protected UUID uuid;

	public DrownedPlayer(EntityType<? extends DrownedPlayer> type, Level world) {
		super(type, world);
	}

	public DrownedPlayer(Level level) {
		this(HordesInfection.DROWNED_PLAYER.get() ,level);
	}

	public DrownedPlayer(Player player) {
		this(player.level);
		setPlayer(player);
	}

	@Override
	protected void defineSynchedData(){
		super.defineSynchedData();
		entityData.define(PLAYER_UUID, Optional.of(UUID.fromString("1512ce82-00e5-441a-9774-f46d9b7badfb")));
	}

	@Override
	public void setPlayer(Player player) {
		for (EquipmentSlot slot : EquipmentSlot.values()) {
			ItemStack stack = slot.getType() == EquipmentSlot.Type.ARMOR ? player.getInventory().getArmor(slot.getIndex()) :
				slot == EquipmentSlot.MAINHAND ? player.getMainHandItem() : player.getOffhandItem();
			setItemSlot(slot, stack);
		}
		setPlayer(player.getGameProfile());
	}

	@Override
	public void setPlayer(String username) {
		setPlayer(ServerLifecycleHooks.getCurrentServer().getProfileCache().get(username).get());
	}

	@Override
	public void setPlayer(UUID uuid) {
		setPlayer(ServerLifecycleHooks.getCurrentServer().getProfileCache().get(uuid).get());
	}

	@Override
	public void setPlayer(GameProfile profile) {
		uuid=profile.getId();
		this.setCustomName(new TextComponent(profile.getName()));
		entityData.set(PLAYER_UUID, Optional.of(uuid));
	}

	@Override
	public UUID getPlayerUUID() {
		return entityData.get(PLAYER_UUID).get();
	}

	@Override
	public void setInventory(Collection<ItemEntity> list) {
		playerItems.clear();
		for (ItemEntity item : list) {
			ItemStack stack = item.getItem();
			item.remove(RemovalReason.DISCARDED);;
			if (stack != null) playerItems.add(stack.copy());
		}
	}

	@Override
	public void setInventory(NonNullList<ItemStack> list) {
		playerItems.clear();
		playerItems.addAll(list);
	}

	@Override
	public NonNullList<ItemStack> getInventory() {
		return playerItems;
	}

	@Override
	protected void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHitIn){
		for (ItemStack stack : playerItems) {
			if (!stack.isEmpty() && ! EnchantmentHelper.hasVanishingCurse(stack)) {
				spawnAtLocation(stack, 0f);
			}
		}
	}

	@Override
	public boolean isSunSensitive() {
		return false;
	}

	@Override
	public void addAdditionalSaveData(CompoundTag compound) {
		super.addAdditionalSaveData(compound);
		if (uuid != null) {
			compound.putString("player", uuid.toString());
		}
		DataUtils.saveItemsToNBT(compound, playerItems);
	}

	@Override
	public void readAdditionalSaveData(CompoundTag compound) {
		super.readAdditionalSaveData(compound);
		if (compound.contains("player")) {
			uuid = UUID.fromString(compound.getString("player"));
		}
		playerItems = DataUtils.readItemsFromNBT(compound);
	}

	@Override
	public BaseComponent getDisplayName() {
		TranslatableComponent textcomponentstring = new TranslatableComponent(PlayerTeam.formatNameForTeam(getTeam(),
				new TextComponent("entity.hordes.DrownedPlayer.chat")).getString(), PlayerTeam.formatNameForTeam(getTeam(), getName()));
		textcomponentstring.getStyle().withHoverEvent(this.createHoverEvent());
		textcomponentstring.getStyle().withInsertion(this.getEncodeId());
		return textcomponentstring;
	}

	@Override
	public void copyFrom(IZombiePlayer entity) {
		setPlayer(entity.getPlayerUUID());
		setInventory(entity.getInventory());
		for (EquipmentSlot slot : EquipmentSlot.values()) {
			ItemStack stack = ((Mob) entity).getItemBySlot(slot);
			setItemSlot(slot, stack);
		}
	}

}
