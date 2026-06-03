package net.smileycorp.hordes.hordeevent.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.*;
import net.minecraft.world.World;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.smileycorp.hordes.config.ClientConfigHandler;

public class HordeClientHandler {

    public static final HordeClientHandler INSTANCE = new HordeClientHandler();

    private int day_length;
    private boolean horde_day;

    @SubscribeEvent
    public void fogColour(EntityViewRenderEvent.FogColors event) {
        if (!ClientConfigHandler.hordeEventTintsSky) return;
        Minecraft mc = Minecraft.getMinecraft();
        WorldClient world = mc.world;
        if (!isHordeNight(world)) return;
        float d = world.getSunBrightnessBody((float)event.getRenderPartialTicks()) * 1.4f;
        int[] rgb = ClientConfigHandler.getHordeSkyColour();
        event.setRed((1f - d) * (float)rgb[0]/255f + (d * event.getRed()));
        event.setGreen((1f - d) * (float)rgb[1]/255f + d * event.getGreen());
        event.setBlue((1f - d) * (float)rgb[2]/255f + d * event.getBlue());
    }

    public void playHordeSound(float dirX, float dirZ, ResourceLocation sound) {
        if (!ClientConfigHandler.hordeSpawnSound) return;
        Minecraft mc = Minecraft.getMinecraft();
        World world = mc.world;
        EntityPlayer player = mc.player;
        BlockPos pos = new BlockPos(player.posX + (10 * dirX), player.posY, player.posZ + (10 * dirZ));
        world.playSound(player, pos, new SoundEvent(sound), SoundCategory.HOSTILE, 0.5f, 1 + ((world.rand.nextInt(6) - 3) / 10f));
    }

    public void displayMessage(String text) {
        GuiIngame gui = Minecraft.getMinecraft().ingameGUI;
        ITextComponent message = new TextComponentTranslation(text);
        message.setStyle(new Style().setBold(true).setColor(TextFormatting.DARK_RED));
        if (ClientConfigHandler.eventNotifyMode == 1) gui.addChatMessage(ChatType.CHAT, message);
        else if (ClientConfigHandler.eventNotifyMode == 2) {
            gui.overlayMessage = message.getFormattedText();
            gui.overlayMessageTime = ClientConfigHandler.eventNotifyDuration;
            gui.animateOverlayMessageColor = false;
        } else if (ClientConfigHandler.eventNotifyMode == 3) {
            gui.displayTitle(null, null, 5, ClientConfigHandler.eventNotifyDuration, 5);
            gui.displayTitle(" ", null, 0, 0, 0);
            gui.displayTitle(null, message.getFormattedText(), 0, 0, 0);
        }
    }

    public void setHordeDay(boolean horde_day, int day_length) {
        if (day_length > 0) this.day_length = day_length;
        this.horde_day = horde_day;
    }

    public boolean isHordeNight(World world) {
        if (day_length == 0 |! horde_day) return false;
        return (world.getWorldTime() % day_length >= 0.5 * day_length);
    }

}
