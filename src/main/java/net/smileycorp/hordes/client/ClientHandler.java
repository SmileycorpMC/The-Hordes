package net.smileycorp.hordes.client;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;
import net.smileycorp.hordes.common.ModDefinitions;

public class ClientHandler {

	public static void playHordeSound() {
		Minecraft mc = Minecraft.getMinecraft();
		World world = mc.world;
		EntityPlayer player = mc.player;
		float pitch = 1+((world.rand.nextInt(6)-3)/10);
		world.playSound(player, player.getPosition(), new SoundEvent(ModDefinitions.getResource("horde_spawn")), SoundCategory.HOSTILE, 0.3f, pitch);
	}

}
