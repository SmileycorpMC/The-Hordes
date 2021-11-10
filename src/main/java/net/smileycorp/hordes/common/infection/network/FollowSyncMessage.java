package net.smileycorp.hordes.common.infection.network;

import java.io.IOException;

import net.minecraft.entity.MobEntity;
import net.minecraft.network.INetHandler;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;

public class FollowSyncMessage implements IPacket<INetHandler> {

		public FollowSyncMessage() {}

		private int entity;
		private boolean isUnfollow;

		public FollowSyncMessage(MobEntity entity, boolean isUnfollow) {
			this.entity = entity.getId();
			this.isUnfollow = isUnfollow;
		}

		@Override
		public void read(PacketBuffer buf) throws IOException {
			entity = buf.readInt();
			isUnfollow = buf.readBoolean();
		}

		@Override
		public void write(PacketBuffer buf) throws IOException {
			buf.writeInt(entity);
			buf.writeBoolean(isUnfollow);
		}

		public MobEntity getEntity(World world) {
			return (MobEntity) world.getEntity(entity);
		}

		public boolean isUnfollow() {
			return isUnfollow;
		}

		@Override
		public void handle(INetHandler handler) {}

}
