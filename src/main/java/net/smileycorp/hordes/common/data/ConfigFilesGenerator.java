package net.smileycorp.hordes.common.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.fml.loading.moddiscovery.ModFile;
import net.smileycorp.hordes.common.Hordes;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class ConfigFilesGenerator {

    private static Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final Path CONFIG_FOLDER = FMLPaths.CONFIGDIR.get().resolve("hordes");

    public static boolean shouldGenerateFiles() {
        if (isUpToDate()) return false;
        copyFile("hordes-info.json");
        copyFile("pack.mcmeta");
        return true;
    }

    public static void generateAssets() {
        copyFile("assets/hordes/sounds.json");
        copyFile("assets/hordes/sounds/horde_spawn.ogg");
        copyFile("assets/hordes/lang/en_us.json");
        copyFile("assets/hordes/textures/entity/drowned_player_outer_layer.png");
        copyFile("assets/hordes/textures/entity/zombie_player_outer_layer.png");
        Hordes.logInfo("Generated asset files.");
    }

    public static void generateData() {
        copyFile("data/hordes/infection_conversions.json");
        copyFile("data/hordes/horde_scripts/default.json");
        copyFile("data/hordes/tables/default.json");
        copyFile("data/hordes/tables/skeletons.json");
        copyFile("data/hordes/tables/mixed_mobs.json");
        copyFile("data/hordes/tables/illagers.json");
        copyFile("data/hordes/tags/entity_types/infection_entities.json");
        copyFile("data/hordes/tags/items/infection_cures.json");
        Hordes.logInfo("Generated data files.");
    }

    private static boolean isUpToDate() {
        File config_file = CONFIG_FOLDER.resolve("hordes/hordes-info.json").toFile();
        if (!config_file.exists()) return false;
        try {
            JsonObject config_json = JsonParser.parseReader(new FileReader(config_file)).getAsJsonObject();
            ModFile mod = FMLLoader.getLoadingModList().getModFileById("hordes").getFile();
            JsonObject mod_json = JsonParser.parseReader(new FileReader(mod.findResource("config_defaults/hordes-info.json").toFile())).getAsJsonObject();
            return config_json.get("data_version").getAsInt() == mod_json.get("data_version").getAsInt();
        } catch (Exception e) {
            return false;
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
            Hordes.logInfo("Copied file " + path);
        } catch (Exception e) {
            Hordes.logError("Failed to copy file " + path, e);
        }
    }

}
