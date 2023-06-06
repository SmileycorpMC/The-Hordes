package net.smileycorp.hordes.common.hordeevent.data;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import net.minecraftforge.fml.config.ConfigTracker;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.loading.FMLPaths;
import net.smileycorp.hordes.common.Hordes;
import net.smileycorp.hordes.common.hordeevent.data.conditions.Condition;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Path;
import java.util.List;

public class DataSerializer {

	public static Condition readCondition(JsonObject obj) {
		return null;

	}

}
