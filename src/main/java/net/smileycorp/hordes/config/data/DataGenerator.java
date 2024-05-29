package net.smileycorp.hordes.config.data;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.fml.loading.moddiscovery.ModFile;
import net.smileycorp.hordes.common.HordesLogger;
import org.apache.commons.io.FileUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;

public class DataGenerator {

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
        ModFile mod = FMLLoader.getLoadingModList().getModFileById("hordes").getFile();
        copyFileFromMod(mod.findResource("config_defaults/hordes-info.json"));
        copyFileFromMod(mod.findResource("config_defaults/pack.mcmeta"));
        return true;
    }
    
    public static void generateAssets() {
        ModFile mod = FMLLoader.getLoadingModList().getModFileById("hordes").getFile();
        try {
            Files.find(mod.findResource("config_defaults/assets"), Integer.MAX_VALUE, (matcher, options) -> options.isRegularFile())
                    .forEach(DataGenerator::copyFileFromMod);
            HordesLogger.logInfo("Generated asset files.");
        } catch (Exception e) {
            HordesLogger.logInfo("Failed to generate asset files.");
        }
    }

    public static void generateData() {
        ModFile mod = FMLLoader.getLoadingModList().getModFileById("hordes").getFile();
        try {
            Files.find(mod.findResource("config_defaults/data"), Integer.MAX_VALUE, (matcher, options) -> options.isRegularFile())
                    .forEach(DataGenerator::copyFileFromMod);
            HordesLogger.logInfo("Generated data files.");
        } catch (Exception e) {
            HordesLogger.logInfo("Failed to generate data files.");
        }
    }

    private static boolean isUpToDate() {
        File config_file = CONFIG_FOLDER.resolve("hordes-info.json").toFile();
        if (!config_file.isFile()) {
            HordesLogger.logInfo("Hordes data does not exist, generating new files.");
            return false;
        }
        try {
            JsonParser parser = new JsonParser();
            JsonObject config_json = parser.parse(new FileReader(config_file)).getAsJsonObject();
            if (config_json.get("data_version").getAsInt() < 0) return true;
            JsonObject mod_json = parser.parse(new BufferedReader(new InputStreamReader(
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
    
    private static void copyFileFromMod(Path path) {
        try {
            FileUtils.copyInputStreamToFile(Files.newInputStream(path),
                    new File(CONFIG_FOLDER.toFile(), path.toString().replace( "config_defaults/", "")));
            HordesLogger.logInfo("Copied file " + path);
        } catch (Exception e) {
            HordesLogger.logError("Failed to copy file " + path, e);
        }
    }

}
