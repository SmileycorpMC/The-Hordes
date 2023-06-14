package net.smileycorp.hordes.common.hordeevent.data;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraftforge.fml.config.ConfigTracker;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.fml.loading.moddiscovery.ModFile;
import net.smileycorp.hordes.common.Hordes;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;

public class DefaultDataGenerator {

    private static Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final Path CONFIG_FOLDER = FMLPaths.CONFIGDIR.get().resolve("hordes");

    public static boolean tryGenerateDataFiles() {
        if (CONFIG_FOLDER.toFile().exists()) return false;
        generateHordeTableFile();
        generateInfectionCuresFile();
        generateMcmetaFile();
        copyFile("assets/hordes/sounds.json");
        copyFile("assets/hordes/sounds/horde_spawn.ogg");
        copyFile("assets/hordes/lang/en_us.json");
        copyFile("data/hordes/horde_scripts/default.json");
        copyFile("data/hordes/tables/skeletons.json");
        copyFile("data/hordes/tables/mixed_mobs.json");
        copyFile("data/hordes/tables/illagers.json");
        copyFile("data/hordes/tags/items/infection_cures.json");
        copyFile("data/minecraft/tags/items/piglin_loved.json");
        //TODO: convert infection list to tag
        return true;
    }

    private static void generateMcmetaFile() {
        try {
            File file = CONFIG_FOLDER.resolve("pack.mcmeta").toFile();
            file.createNewFile();
            JsonObject json = new JsonObject();
            JsonObject pack = new JsonObject();
            pack.addProperty("description", "The Hordes config datapack");
            pack.addProperty("pack_format", 8);
            json.add("pack", pack);
            FileWriter writer = new FileWriter(file);
            writer.write(GSON.toJson(json));
            writer.close();
        } catch (Exception e) {
            Hordes.logError("Failed to generate pack.mcmeta", e);
        }
    }

    private static void generateHordeTableFile() {
        try {
            List<String> hordeEntries = null;
            List<String> oldHordeEntries = Lists.newArrayList("minecraft:zombie-35-0-20", "minecraft:zombie_villager-1-0-20",
                    "minecraft:husk-40-30-0", "minecraft:drowned{HandItems:[{id:trident,Count:1}]}-1-40-0", "minecraft:zombie_horse-3-30-0");
            File file = CONFIG_FOLDER.resolve("data/hordes/tables/default.json").toFile();
            file.getParentFile().mkdirs();
            file.createNewFile();
            try {
                ModConfig config = ConfigTracker.INSTANCE.fileMap().get("hordes-common.toml");
                CommentedFileConfig configData = config.getHandler().reader(FMLPaths.CONFIGDIR.get()).apply(config);
                hordeEntries = configData.get(Lists.newArrayList("Horde Event", "spawnList"));
            } catch (Exception e) {
                copyFile("data/hordes/tables/default.json");
                Hordes.logError("Error reading hordes-common.toml, generating fallback spawnlist", e);
                return;
            }
            if (hordeEntries == null || hordeEntries.isEmpty() || hordeEntries.equals(oldHordeEntries)) {
                copyFile("data/hordes/tables/default.json");
                return;
            }
            JsonArray json = new JsonArray();
            for (String entry : hordeEntries) json.add(entry);
            FileWriter writer = new FileWriter(file);
            writer.write(GSON.toJson(json));
            writer.close();
        } catch (Exception e) {
            Hordes.logError("Failed to generate horde table file", e);
        }
    }

    private static void generateInfectionCuresFile() { //TODO: finish and test cures tag
        try {
            List<String> cureEntries = null;
            File file = CONFIG_FOLDER.resolve("data/hordes/tags/items/infection_cures.json").toFile();
            file.getParentFile().mkdirs();
            file.createNewFile();
            try {
                ModConfig config = ConfigTracker.INSTANCE.fileMap().get("hordes-common.toml");
                CommentedFileConfig configData = config.getHandler().reader(FMLPaths.CONFIGDIR.get()).apply(config);
                cureEntries = configData.get(Lists.newArrayList("Infection", "cureItemList"));
            } catch (Exception e) {
                copyFile("data/hordes/tags/items/infection_cures.json");
                Hordes.logError("Error reading hordes-common.toml, generating fallback cure item list", e);
                return;
            }
            if (cureEntries == null || cureEntries.isEmpty()) {
                copyFile("data/hordes/tags/items/infection_cures.json");
                return;
            }
            JsonArray json = new JsonArray();
            for (String entry : cureEntries) json.add(entry);
            FileWriter writer = new FileWriter(file);
            writer.write(GSON.toJson(json));
            writer.close();
        } catch (Exception e) {
            Hordes.logError("Failed to generate cure item file", e);
        }
    }

    private static void copyFile(String path) {
        try {
            ModFile mod = FMLLoader.getLoadingModList().getModFileById("hordes").getFile();
            File directory = CONFIG_FOLDER.toFile();
            File output = new File(directory, path);
            File dir = output.getParentFile();
            if (dir != null) dir.mkdirs();
            FileUtils.copyInputStreamToFile(Files.newInputStream(mod.findResource("config_defaults/" + path), StandardOpenOption.READ), new File(directory, path));
        } catch (Exception e) {
            Hordes.logError("Failed to copy file " + path, e);
        }
    }

}
