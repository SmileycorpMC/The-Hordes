package net.smileycorp.hordes.common.entities;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

import com.mojang.authlib.GameProfile;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.monster.ZombieEntity;
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
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import net.smileycorp.hordes.common.CommonConfigHandler;
import net.smileycorp.hordes.common.infection.HordesInfection;


public class ZombiePlayerEntity extends ZombieEntity implements IZombiePlayer {

	protected static final DataParameter<Optional<UUID>> PLAYER_UUID = EntityDataManager.defineId(ZombiePlayerEntity.class, DataSerializers.OPTIONAL_UUID);
	protected static final DataParameter<Boolean> SHOW_CAPE = EntityDataManager.defineId(ZombiePlayerEntity.class, DataSerializers.BOOLEAN);

	protected NonNullList<ItemStack> playerItems = NonNullList.<ItemStack>create();
	protected UUID uuid;

	public double xCloakO;
	public double yCloakO;
	public double zCloakO;
	public double xCloak;
	public double yCloak;
	public double zCloak;

	public ZombiePlayerEntity(EntityType<? extends ZombiePlayerEntity> type, World world) {
		super(type, world);
	}

	public ZombiePlayerEntity(World world) {
		this(HordesInfection.ZOMBIE_PLAYER.get() ,world);
	}

	public ZombiePlayerEntity(PlayerEntity player) {
		this(player.level);
		setPlayer(player);
	}

	@Override
	protected void defineSynchedData(){
		super.defineSynchedData();
		entityData.define(PLAYER_UUID, Optional.of(UUID.fromString("1512ce82-00e5-441a-9774-f46d9b7badfb")));
		entityData.define(SHOW_CAPE, true);
	}

	@Override
	public void setPlayer(PlayerEntity player) {
		if (player == null) return;
		for (EquipmentSlotType slot : EquipmentSlotType.values()) {
			ItemStack stack = slot.getType() == EquipmentSlotType.Group.ARMOR ? player.inventory.armor.get(slot.getIndex()) :
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
		if (profile == null) return;
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
	protected void doUnderWaterConversion() {
		if (CommonConfigHandler.drownedPlayers.get()) {
			ZombieEntity drowned = convertTo(HordesInfection.DROWNED_PLAYER.get(), true);
			if (drowned != null) {
				drowned.handleAttributes(drowned.level.getCurrentDifficultyAt(drowned.blockPosition()).getSpecialMultiplier());
				drowned.setCanBreakDoors(drowned.supportsBreakDoorGoal() && this.canBreakDoors());
				ForgeEventFactory.onLivingConvert(this, drowned);
				if (drowned instanceof IZombiePlayer) ((IZombiePlayer) drowned).copyFrom(this);
			}
			if (!this.isSilent()) {
				this.level.levelEvent((PlayerEntity)null, 1040, this.blockPosition(), 0);
			}
		}
	}

	@Override
	public boolean isSunSensitive() {
		return CommonConfigHandler.zombiePlayersBurn.get();
	}

	@Override
	public boolean fireImmune() {
		return CommonConfigHandler.zombiePlayersFireImmune.get();
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
				new StringTextComponent("entity.hordes.ZombiePlayer.chat")).getString(), ScorePlayerTeam.formatNameForTeam(getTeam(), getName()));
		textcomponentstring.getStyle().withHoverEvent(this.createHoverEvent());
		textcomponentstring.getStyle().withInsertion(this.getEncodeId());
		return textcomponentstring;
	}

	@Override
	public void copyFrom(IZombiePlayer entity) {
		setPlayer(entity.getPlayerUUID());
		setInventory(entity.getInventory());
		for (EquipmentSlotType slot : EquipmentSlotType.values()) {
			ItemStack stack = ((MobEntity) entity).getItemBySlot(slot);
			setItemSlot(slot, stack);
		}
		entityData.set(SHOW_CAPE, entity.displayCape());
	}

	@Override
	public void tick() {
		super.tick();
		moveCloak(this);
	}

	@Override
	public void setDisplayCape(boolean display) {
		entityData.set(SHOW_CAPE, display);
	}

	@Override
	public boolean displayCape() {
		return entityData.get(SHOW_CAPE);
	}

	@Override
	public double getXCloakO() {
		return xCloakO;
	}

	@Override
	public double getYCloakO() {
		return yCloakO;
	}

	@Override
	public double getZCloakO() {
		return zCloakO;
	}

	@Override
	public double getXCloak() {
		return xCloak;
	}

	@Override
	public double getYCloak() {
		return yCloak;
	}

	@Override
	public double getZCloak() {
		return zCloak;
	}

	@Override
	public void setXCloakO(double value) {
		xCloakO = value;
	}

	@Override
	public void setYCloakO(double value) {
		yCloakO = value;
	}

	@Override
	public void setZCloakO(double value) {
		zCloakO = value;
	}

	@Override
	public void setXCloak(double value) {
		xCloak = value;
	}

	@Override
	public void setYCloak(double value) {
		yCloak = value;
	}

	@Override
	public void setZCloak(double value) {
		zCloak = value;
	}

}
