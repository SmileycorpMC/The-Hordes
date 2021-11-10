package net.smileycorp.hordes.common.entities;

import java.awt.Color;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.monster.DrownedEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.util.DamageSource;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

import com.mojang.authlib.GameProfile;


public class DrownedPlayerEntity extends DrownedEntity implements IZombiePlayer {

	protected static final DataParameter<Optional<UUID>> PLAYER_UUID = EntityDataManager.defineId(DrownedPlayerEntity.class, DataSerializers.OPTIONAL_UUID);

	protected Color COLOUR = new Color(0x689E93);

	protected NonNullList<ItemStack> playerItems = NonNullList.<ItemStack>create();
	protected UUID uuid;

	public DrownedPlayerEntity(EntityType<? extends DrownedPlayerEntity> type, World world) {
		super(type, world);
	}

	@Override
	protected void defineSynchedData(){
		super.defineSynchedData();
		entityData.define(PLAYER_UUID, Optional.of(UUID.fromString("1512ce82-00e5-441a-9774-f46d9b7badfb")));
	}

	@Override
	public void setPlayer(PlayerEntity player) {
		for (EquipmentSlotType slot : EquipmentSlotType.values()) {
			ItemStack stack = slot.getType() == EquipmentSlotType.Group.ARMOR ? player.inventory.getArmor(slot.getIndex()) :
				slot == EquipmentSlotType.MAINHAND ? player.getMainHandItem() : player.getOffhandItem();
				setItemSlot(slot, stack);
		}
		setPlayer(player.getGameProfile());
	}

	@Override
	public void setPlayer(String username) {
		setPlayer(ServerLifecycleHooks.getCurrentServer().getProfileCache().get(username));
	}

	@Override
	public void setPlayer(UUID uuid) {
		setPlayer(ServerLifecycleHooks.getCurrentServer().getProfileCache().get(uuid));
	}

	@Override
	public void setPlayer(GameProfile profile) {
		this.uuid=profile.getId();
		this.setCustomName(new StringTextComponent(profile.getName()));
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
			item.remove();;
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
	public void addAdditionalSaveData(CompoundNBT compound) {
		super.addAdditionalSaveData(compound);
		if (uuid != null) {
			compound.putString("player", uuid.toString());
		}
		ItemStackHelper.saveAllItems(compound, playerItems);
	}

	@Override
	public void readAdditionalSaveData(CompoundNBT compound) {
		super.readAdditionalSaveData(compound);
		if (compound.contains("player")) {
			uuid = UUID.fromString(compound.getString("player"));
		}
		NonNullList<ItemStack> read = NonNullList.<ItemStack>withSize(compound.getList("Items", 10).size(), ItemStack.EMPTY);
		ItemStackHelper.loadAllItems(compound, read);
		playerItems = read;
	}

	@Override
	public ITextComponent getDisplayName() {
		TranslationTextComponent textcomponentstring = new TranslationTextComponent(ScorePlayerTeam.formatNameForTeam(getTeam(),
				new StringTextComponent("entity.hordes.DrownedPlayer.chat")).getString(), ScorePlayerTeam.formatNameForTeam(getTeam(), getName()));
		textcomponentstring.getStyle().withHoverEvent(this.createHoverEvent());
		textcomponentstring.getStyle().withInsertion(this.getEncodeId());
		return textcomponentstring;
	}

	@Override
	public Color getColour() {
		return COLOUR;
	}

}
