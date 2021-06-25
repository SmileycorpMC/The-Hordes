package net.smileycorp.hordes.client;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.smileycorp.hordes.common.ModDefinitions;

public class ClientHandler {

	public static void playHordeSound(Vec3d dir) {
		Minecraft mc = Minecraft.getMinecraft();
		World world = mc.world;
		EntityPlayer player = mc.player;
		BlockPos pos = new BlockPos(player.posX + (3*dir.x), player.posY, player.posZ + (3*dir.z));
		float pitch = 1+((world.rand.nextInt(6)-3)/10);
		world.playSound(player, pos, new SoundEvent(ModDefinitions.getResource("horde_spawn")), SoundCategory.HOSTILE, 0.3f, pitch);
	}

	public static EntityPlayer getPlayer() {
		return Minecraft.getMinecraft().player;
	}

}
