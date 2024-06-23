package net.smileycorp.hordes.hordeevent.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.smileycorp.atlas.api.util.TextUtils;
import net.smileycorp.hordes.config.ClientConfigHandler;

import java.awt.*;

public class HordeClientHandler {
    
    public static final HordeClientHandler INSTANCE = new HordeClientHandler();
    
    private int day_length;
    private boolean horde_day;
    
    @SubscribeEvent
    public void fogColour(ViewportEvent.ComputeFogColor event) {
        if (!ClientConfigHandler.hordeEventTintsSky.get()) return;
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        if (isHordeNight(level)) {
            float d = level.getSkyDarken((float)event.getPartialTick()) * 1.4f;
            Color rgb = ClientConfigHandler.getHordeSkyColour();
            event.setRed((1f - d) * (float)rgb.getRed()/255f + d * event.getRed());
            event.setGreen((1f - d) * (float)rgb.getGreen()/255f + d * event.getGreen());
            event.setBlue((1f - d) * (float)rgb.getBlue()/255f + d * event.getBlue());
        }
    }
    
    public void playHordeSound(Vec3 vec3, ResourceLocation sound) {
        if (ClientConfigHandler.hordeSpawnSound.get()) {
            Minecraft mc = Minecraft.getInstance();
            Level level = mc.level;
            LocalPlayer player = mc.player;
            BlockPos pos = BlockPos.containing(player.getX() + (10 * vec3.x), player.getY(), player.getZ() + (10 * vec3.z));
            float pitch = 1 + ((level.random.nextInt(6) - 3) / 10);
            level.playSound(player, pos, SoundEvent.createVariableRangeEvent(sound), SoundSource.HOSTILE, 0.5f, pitch);
        }
    }
    
    public void displayMessage(String text) {
        Minecraft mc = Minecraft.getInstance();
        Gui gui = mc.gui;
        MutableComponent message = TextUtils.translatableComponent(text, null);
        message.setStyle(Style.EMPTY.withColor(ClientConfigHandler.getHordeMessageColour()));
        if (ClientConfigHandler.eventNotifyMode.get() == 1) gui.getChat().addMessage(message);
        else if (ClientConfigHandler.eventNotifyMode.get() == 2) {
            gui.overlayMessageString = message;
            gui.overlayMessageTime = ClientConfigHandler.eventNotifyDuration.get();
            gui.animateOverlayMessageColor = false;
        } else if (ClientConfigHandler.eventNotifyMode.get() == 3) {
            gui.setTimes(5, ClientConfigHandler.eventNotifyDuration.get(), 5);
            gui.setSubtitle(message);
        }

    }
    
    public void setHordeDay(boolean horde_day, int day_length) {
        if (day_length > 0) this.day_length = day_length;
        this.horde_day = horde_day;
    }
    
    public boolean isHordeNight(Level level) {
        if (day_length == 0 |! horde_day) return false;
        return (level.getDayTime() % day_length >= 0.5 * day_length);
    }
    
    
}