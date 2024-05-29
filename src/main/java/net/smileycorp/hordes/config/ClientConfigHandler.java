package net.smileycorp.hordes.config;

import net.minecraftforge.common.config.Configuration;
import net.smileycorp.hordes.common.Hordes;

import java.awt.*;

public class ClientConfigHandler {
    
    private static Color zombiePlayerColour = null;
    
    //horde event
    public static int eventNotifyMode;
    public static int eventNotifyDuration;
    public static boolean hordeSpawnSound;
    public static boolean hordeEventTintsSky;
    public static int[] hordeEventSkyColour;
    public static int[] hordeEventMoonColour;
    
    //infection
    public static boolean playerInfectionVisuals;
    public static boolean playerInfectSound;
    public static boolean infectionProtectSound;
    public static boolean cureTooltip;
    public static boolean immunityTooltip;
    public static boolean wearableProtectionTooltip;
    
    
    public static void syncConfig(Configuration config) {
        Hordes.logInfo("Trying to load client config");
        try{
            config.load();
            //horde event
            eventNotifyMode = config.get("Horde Event", "eventNotifyMode", 1, "How do players get notified of a horde event. 0: Off, 1: Chat, 2:Action Bar, 3:Title").getInt();
            eventNotifyDuration = config.get("Horde Event", "eventNotifyDuration", 60, "How long in ticks does the horde notification appear? (Only applies to modes 2 and 3)").getInt();
            hordeSpawnSound = config.get("Horde Event", "hordeSpawnSound", true, "Play a sound when a horde wave spawns?").getBoolean();
            hordeEventTintsSky = config.get("Horde Event", "hordeEventTintsSky", true, "Whether the sky and moon should be tinted on a horde night").getBoolean();
            hordeEventSkyColour = config.get("Horde Event", "configHordeEventSkyColour", new int[]{102, 0, 0}, "Colour of the sky during horde events", 0, 255).getIntList();
            hordeEventMoonColour = config.get("Horde Event", "configHordeEventMoonColour", new int[]{193, 57, 15}, "Colour of the moon during horde events", 0, 255).getIntList();
        } catch(Exception e) {
        } finally {
            if (config.hasChanged()) config.save();
        }
    }
    
    public static int[] getHordeSkyColour() {
        if (hordeEventSkyColour .length < 3) hordeEventSkyColour  = new int[]{193, 57, 15};
        return hordeEventSkyColour;
    }
    
    
    public static int[] getHordeMoonColour() {
        if (hordeEventMoonColour.length < 3) hordeEventMoonColour = new int[]{193, 57, 15};
        return hordeEventMoonColour;
    }
    
}
