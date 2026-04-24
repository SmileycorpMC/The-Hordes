package net.smileycorp.hordes.common;

import com.google.common.collect.Lists;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.LiteralContents;
import net.minecraft.resources.ResourceLocation;
import net.smileycorp.hordes.common.data.HordesParsingException;
import net.smileycorp.hordes.hordeevent.capability.HordeSavedData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

public class HordesLogger {

    private static final Logger logger = LogManager.getLogger(Constants.MODID);
    private static final Path log_file = Paths.get("logs/hordes.log");

    private static final List<String> persistent_data = Lists.newArrayList();
    private static final List<ResourceLocation> errored_scripts = Lists.newArrayList();

    private static boolean is_volatile = false;
    private static boolean has_errors = false;

    public static void clearLog(boolean clear_persistent) {
        try {
            Files.write(log_file, clear_persistent ? Lists.newArrayList() : persistent_data, StandardCharsets.UTF_8);
        } catch (Exception e) {
            logger.error("Failed to write to log file", e);
            e.printStackTrace();
        }
    }

    public static void logSilently(Object message) {
        writeToFile(message);
    }

    public static void logInfo(Object message) {
        writeToFile(message);
        logger.info(message);
    }

    public static void logError(Object message, Exception e) {
        boolean stackTrace = true;
        if (e instanceof HordesParsingException) {
            ResourceLocation script = ((HordesParsingException) e).getScript();
            stackTrace = false;
            if (script != null) {
                errored_scripts.add(script);
                writeToFile("Errors in horde script " + script);
            }
        }
        logger.error(message, e);
        writeToFile(message +  " " + (stackTrace ? e : e.getMessage()));
        if (stackTrace) {
            for (StackTraceElement traceElement : e.getStackTrace()) writeToFile(traceElement);
            e.printStackTrace();
        }
        has_errors = true;
    }

    public static boolean logSaveData(HordeSavedData data) {
        writeToFile("Horde world data: ");
        return writeToFile(data.getDebugText());
    }

    private static boolean writeToFile(Object message) {
        return writeToFile(Lists.newArrayList(String.valueOf(message)));
    }

    private static boolean writeToFile(List<String> out) {
        try {
            if (!is_volatile) persistent_data.addAll(out);
            Files.write(log_file, out, StandardCharsets.UTF_8, StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            return true;
        } catch (Exception e) {
            logger.error("Failed to write to log file", e);
            e.printStackTrace();
            return false;
        }
    }

    public static void markVolatile() {
        is_volatile = true;
    }

    public static boolean hasErrors() {
        return has_errors;
    }

    public static List<ResourceLocation> getErroredScripts() {
        return errored_scripts;
    }

    public static MutableComponent getFiletext() {
        String file = log_file.toAbsolutePath().toString();
        MutableComponent text = MutableComponent.create(new LiteralContents(file));
        text.setStyle(Style.EMPTY.withUnderlined(true).withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, file))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, MutableComponent.create(new LiteralContents(file)))));
        return text;
    }

    public static void blankLine() {
        writeToFile("");
    }

    public static void heading(String message) {
        writeToFile("############################## " + message + " ##############################");
    }

    public static void clearErrors() {
        has_errors = false;
        errored_scripts.clear();
    }

}
