package net.smileycorp.hordes.gibbing;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class BodyPartMessage implements IMessage {
	
		private NBTTagCompound nbt = new NBTTagCompound();
		private int id = 0;
		
		public BodyPartMessage() {}
		
		public BodyPartMessage(Entity entity) {
			IZombieGibbing gibbing = entity.getCapability(ZombieGibbingProvider.GIBBING, EnumFacing.UP);
			nbt = (NBTTagCompound) ZombieGibbingProvider.GIBBING.getStorage().writeNBT(ZombieGibbingProvider.GIBBING, gibbing, EnumFacing.UP);
			id = entity.getEntityId();
		}

		@Override
		public void toBytes(ByteBuf buf) {
			buf.writeInt(id);
			ByteBufUtils.writeTag(buf, nbt);
		}

		@Override
		public void fromBytes(ByteBuf buf) {
			id = buf.readInt();
			nbt = ByteBufUtils.readTag(buf);
		}
		
		public NBTTagCompound getNBT() {
			return nbt;
		}

		public int getEntityID() {
			return 0;
		}

}
