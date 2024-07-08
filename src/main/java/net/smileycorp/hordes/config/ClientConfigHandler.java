package net.smileycorp.hordes.config;

import net.minecraftforge.common.config.Configuration;
import net.smileycorp.hordes.common.HordesLogger;

public class ClientConfigHandler {
    
    public static int[] zombiePlayerColour;
    
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
        HordesLogger.logInfo("Trying to load client config");
        try{
            config.load();
            zombiePlayerColour = config.get("Horde Event", "zombiePlayerColour", new int[]{121, 156, 101}, "Colour tint for zombie players.", 0, 255).getIntList();
            //horde event
            eventNotifyMode = config.get("Horde Event", "eventNotifyMode", 1, "How do players get notified of a horde event. 0: Off, 1: Chat, 2:Action Bar, 3:Title").getInt();
            eventNotifyDuration = config.get("Horde Event", "eventNotifyDuration", 60, "How long in ticks does the horde notification appear? (Only applies to modes 2 and 3)").getInt();
            hordeSpawnSound = config.get("Horde Event", "hordeSpawnSound", true, "Play a sound when a horde wave spawns?").getBoolean();
            hordeEventTintsSky = config.get("Horde Event", "hordeEventTintsSky", true, "Whether the sky and moon should be tinted on a horde night").getBoolean();
            hordeEventSkyColour = config.get("Horde Event", "hordeEventSkyColour", new int[]{102, 0, 0}, "Colour of the sky during horde events", 0, 255).getIntList();
            hordeEventMoonColour = config.get("Horde Event", "hordeEventMoonColour", new int[]{193, 57, 15}, "Colour of the moon during horde events", 0, 255).getIntList();
            //infection
            playerInfectionVisuals = config.get("Infection", "playerInfectionVisuals", true, "Tint the player's screen and display other visual effects if they are infected.").getBoolean();
            playerInfectSound = config.get("Infection", "playerInfectSound", true, "Play a sound when the player becomes infected.").getBoolean();
            infectionProtectSound = config.get("Infection", "infectionProtectSound", false, "Play a sound when infection gets prevented?").getBoolean();
            cureTooltip = config.get("Infection", "cureTooltip", true, "Show a tooltip on items that can cure infection?").getBoolean();
            immunityTooltip = config.get("Infection", "immunityTooltip", true, "Show a tooltip on items that give infection immunity?").getBoolean();
            wearableProtectionTooltip = config.get("Infection", "wearableProtectionTooltip", true, "Show a tooltip on wearable items that grant some protection against infection?").getBoolean();
        } catch(Exception e) {
        } finally {
            if (config.hasChanged()) config.save();
        }
    }
    
    public static int[] getHordeSkyColour() {
        if (hordeEventSkyColour .length < 3) hordeEventSkyColour  = new int[]{102, 0, 0};
        return hordeEventSkyColour;
    }
    
    
    public static int[] getHordeMoonColour() {
        if (hordeEventMoonColour.length < 3) hordeEventMoonColour = new int[]{193, 57, 15};
        return hordeEventMoonColour;
    }
    
    public static int[] getZombiePlayerColour() {
        if (zombiePlayerColour.length < 3) zombiePlayerColour = new int[]{121, 156, 101};
        return zombiePlayerColour;
    }
    
}
