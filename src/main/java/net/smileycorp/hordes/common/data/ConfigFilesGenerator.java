package net.smileycorp.hordes.common.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.loading.FMLPaths;
import net.smileycorp.atlas.api.util.DataUtils;
import net.smileycorp.hordes.common.HordesLogger;
import org.apache.commons.io.FileUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConfigFilesGenerator {

    private static Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final Path CONFIG_FOLDER = FMLPaths.CONFIGDIR.get().resolve("hordes");

    public static boolean shouldGenerateFiles() {
        if (isUpToDate()) return false;
        try {
            if (CONFIG_FOLDER.toFile().exists()) {
                File backup = FMLPaths.CONFIGDIR.get().resolve("hordes-backup").toFile();
                backup.mkdir();
                FileUtils.deleteDirectory(backup);
                FileUtils.copyDirectory(CONFIG_FOLDER.toFile(), backup);
                HordesLogger.logInfo("Backed up old data to " + backup);
            }
            FileUtils.deleteDirectory(CONFIG_FOLDER.toFile());
        } catch (Exception e) {}
        DataUtils.copyFileFromMod("hordes-info.json", "hordes");
        DataUtils.copyFileFromMod("pack.mcmeta", "hordes");
        return true;
    }

    public static void generateAssets() {
        DataUtils.copyFileFromMod("assets/hordes/lang/en_us.json", "hordes");
        DataUtils.copyFileFromMod("assets/hordes/sounds.json", "hordes");
        DataUtils.copyFileFromMod("assets/hordes/sounds/horde_spawn.ogg", "hordes");
        DataUtils.copyFileFromMod("assets/hordes/textures/entity/layer/drowned_player_outer_layer.png", "hordes");
        DataUtils.copyFileFromMod("assets/hordes/textures/entity/layer/zombie_player_outer_layer.png", "hordes");
        DataUtils.copyFileFromMod("assets/hordes/textures/entity/zombie_evoker.png", "hordes");
        DataUtils.copyFileFromMod("assets/hordes/textures/entity/zombie_illusioner.png", "hordes");
        DataUtils.copyFileFromMod("assets/hordes/textures/entity/zombie_pillager.png", "hordes");
        DataUtils.copyFileFromMod("assets/hordes/textures/entity/zombie_vindicator.png", "hordes");
        DataUtils.copyFileFromMod("assets/hordes/textures/entity/zombie_wandering_trader.png", "hordes");
        DataUtils.copyFileFromMod("assets/hordes/textures/entity/zombie_witch.png", "hordes");
        DataUtils.copyFileFromMod("assets/hordes/textures/entity/zombified_piglin_brute.png", "hordes");
        HordesLogger.logInfo("Generated asset files.");
    }

    public static void generateData() {
        DataUtils.copyFileFromMod("data/hordes/horde_data/infection_conversions.json", "hordes");
        DataUtils.copyFileFromMod("data/hordes/horde_data/scripts/default.json", "hordes");
        DataUtils.copyFileFromMod("data/hordes/horde_data/tables/default.json", "hordes");
        DataUtils.copyFileFromMod("data/hordes/horde_data/tables/skeletons.json", "hordes");
        DataUtils.copyFileFromMod("data/hordes/horde_data/tables/mixed_mobs.json", "hordes");
        DataUtils.copyFileFromMod("data/hordes/horde_data/tables/illagers.json", "hordes");
        DataUtils.copyFileFromMod("data/hordes/tags/entity_types/infection_entities.json", "hordes");
        DataUtils.copyFileFromMod("data/hordes/tags/items/infection_cures.json", "hordes");
        HordesLogger.logInfo("Generated data files.");
    }

    private static boolean isUpToDate() {
        File config_file = CONFIG_FOLDER.resolve("hordes-info.json").toFile();
        if (!config_file.isFile()) {
            HordesLogger.logInfo("Hordes data does not exist, generating new files.");
            return false;
        }
        try {
            JsonObject config_json = JsonParser.parseReader(new FileReader(config_file)).getAsJsonObject();
            if (config_json.get("data_version").getAsInt() < 0) return true;
            JsonObject mod_json = JsonParser.parseReader(new BufferedReader(new InputStreamReader(
                    Files.newInputStream(FMLLoader.getLoadingModList().getModFileById("hordes").getFile()
                            .findResource("config_defaults/hordes-info.json"))))).getAsJsonObject();
            boolean flag = config_json.get("data_version").getAsInt() >= mod_json.get("data_version").getAsInt();
            if (!flag) HordesLogger.logInfo("Hordes data is not up to date, or set to pack author mode, generating new files.");;
            return flag;
        } catch (Exception e) {
            HordesLogger.logError("Failed data version check", e);
            return false;
        }
    }

}
