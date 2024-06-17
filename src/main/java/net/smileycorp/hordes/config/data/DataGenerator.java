package net.smileycorp.hordes.config.data;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.smileycorp.hordes.common.HordesLogger;
import org.apache.commons.io.FileUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;

public class DataGenerator {

    private static Path CONFIG_FOLDER;
    
    public static void init(FMLPreInitializationEvent event) {
        CONFIG_FOLDER = event.getModConfigurationDirectory().toPath().resolve("hordes");
        if (shouldGenerateFiles(event.getModConfigurationDirectory().toPath())) generateData();
    }

    public static boolean shouldGenerateFiles(Path folder) {
        if (isUpToDate()) return false;
        try {
            if (CONFIG_FOLDER.toFile().exists()) {
                File backup = folder.resolve("hordes-backup").toFile();
                backup.mkdir();
                FileUtils.deleteDirectory(backup);
                FileUtils.copyDirectory(CONFIG_FOLDER.toFile(), backup);
                HordesLogger.logInfo("Backed up old data to " + backup);
            }
            FileUtils.deleteDirectory(CONFIG_FOLDER.toFile());
        } catch (Exception e) {}
        return true;
    }

    public static void generateData() {
        try (FileSystem mod = FileSystems.newFileSystem(DataGenerator.class.getProtectionDomain().getCodeSource().getLocation().toURI(),
                Collections.emptyMap())) {
            Files.find(mod.getPath("config_defaults"), Integer.MAX_VALUE, (matcher, options) -> options.isRegularFile())
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
            JsonObject mod_json = parser.parse(new BufferedReader(
                    new InputStreamReader(DataGenerator.class.getResourceAsStream("config_defaults/hordes-info.json")))).getAsJsonObject();
            boolean flag = config_json.get("data_version").getAsInt() >= mod_json.get("data_version").getAsInt();
            if (!flag) HordesLogger.logInfo("Hordes data is not up to date, or set to pack author mode, generating new files.");;
            return flag;
        } catch (Exception e) {
            HordesLogger.logError("Failed data version check", e);
            return false;
        }
    }
    
    private static void copyFileFromMod(String path) {
        try {
            FileUtils.copyInputStreamToFile(DataGenerator.class.getResourceAsStream(path),
                    new File(CONFIG_FOLDER.toFile(), path.replace( "config_defaults/", "")));
            HordesLogger.logInfo("Copied file " + path);
        } catch (Exception e) {
            HordesLogger.logError("Failed to copy file " + path, e);
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
