package water.of.cup.cameras;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import io.papermc.paper.event.server.ServerResourcesReloadedEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;
import water.of.cup.cameras.commands.CameraCommands;
import water.of.cup.cameras.listeners.CameraClick;
import water.of.cup.cameras.listeners.MapInitialize;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class Camera extends JavaPlugin implements Listener {

	private static Camera instance;
	ResourcePackManager resourcePackManager = new ResourcePackManager();
	private File configFile;
	private FileConfiguration config;

	public NamespacedKey key = new NamespacedKey(this, "camera");

	@Override
	public void onEnable() {
		instance = this;

		loadConfig();

		this.resourcePackManager.initialize();

		Utils.loadColors();
		getCommand("takePicture").setExecutor(new CameraCommands());
		registerListeners(new CameraClick(), new MapInitialize(), this /* 兼容数据包重载 */);

		if(config.getBoolean("settings.camera.recipe.enabled"))
			addCameraRecipe();
	}

	@Override
	public void onDisable() {
		/* Disable all current async tasks */
		Bukkit.getScheduler().cancelTasks(this);

		Bukkit.removeRecipe(new NamespacedKey(this, "camera"));
	}

	private void registerListeners(Listener... listeners) {
		Arrays.stream(listeners).forEach(listener -> getServer().getPluginManager().registerEvents(listener, this));
	}

	public static Camera getInstance() {
		return instance;
	}

	// 兼容数据包重载
	@EventHandler
	public void onServerResourcesReloaded(final ServerResourcesReloadedEvent e) {
		Bukkit.getScheduler().runTask(this, this::addCameraRecipe);
	}

	public void addCameraRecipe() {
		ItemStack camera = new ItemStack(Material.PLAYER_HEAD);
		SkullMeta cameraMeta = (SkullMeta) camera.getItemMeta();
		PlayerProfile profile = Bukkit.createProfile(null, "MHF_Camera");
		profile.setProperty(new ProfileProperty("textures",
												"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNmZiNWVlZTQwYzNkZDY2ODNjZWM4ZGQxYzZjM2ZjMWIxZjAxMzcxNzg2NjNkNzYxMDljZmUxMmVkN2JmMjc4ZSJ9fX0="));
		cameraMeta.setPlayerProfile(profile);

		cameraMeta.setDisplayName(ChatColor.BLUE + "一次性拍立得");

		cameraMeta.setCustomModelData(6); // KioCG
		camera.setItemMeta(cameraMeta);

		ShapedRecipe recipe = new ShapedRecipe(key, camera);

		ArrayList<String> shapeArr = (ArrayList<String>) config.get("settings.camera.recipe.shape");
		recipe.shape(shapeArr.toArray(new String[shapeArr.size()]));

		for(String ingredientKey : config.getConfigurationSection("settings.camera.recipe.ingredients").getKeys(false)){
			recipe.setIngredient(ingredientKey.charAt(0), Material.valueOf((String) config.get("settings.camera.recipe.ingredients." + ingredientKey)));
		}

		Bukkit.addRecipe(recipe);
	}

	private void loadConfig() {
		if (!getDataFolder().exists()) {
			getDataFolder().mkdir();
		}

		configFile = new File(getDataFolder(), "config.yml");
		if (!configFile.exists()) {
			try {
				configFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		config = YamlConfiguration.loadConfiguration(configFile);

		HashMap<String, Object> defaultConfig = new HashMap<>();

		defaultConfig.put("settings.messages.notready", "&cCameras is still loading, please wait.");
		defaultConfig.put("settings.messages.invfull", "&cYou can not take a picture with a full inventory.");
		defaultConfig.put("settings.messages.enabled", true);
		defaultConfig.put("settings.camera.transparentWater", true);
		defaultConfig.put("settings.camera.shadows", true);
		defaultConfig.put("settings.camera.permissions", true);

		HashMap<String, String> defaultRecipe = new HashMap<>();
		defaultRecipe.put("I", Material.IRON_INGOT.toString());
		defaultRecipe.put("G", Material.GLASS_PANE.toString());
		defaultRecipe.put("T", Material.GLOWSTONE_DUST.toString());
		defaultRecipe.put("R", Material.REDSTONE.toString());

		defaultConfig.put("settings.camera.recipe.enabled", true);
		defaultConfig.put("settings.camera.recipe.shape", new ArrayList<String>() {
			{
				add("IGI");
				add("ITI");
				add("IRI");
			}
		});


		if(!config.contains("settings.camera.recipe.ingredients")) {
			for (String key : defaultRecipe.keySet()) {
				defaultConfig.put("settings.camera.recipe.ingredients." + key, defaultRecipe.get(key));
			}
		}

		for (String key : defaultConfig.keySet()) {
			if(!config.contains(key)) {
				config.set(key, defaultConfig.get(key));
			}
		}

		File mapDir = new File(getDataFolder(), "maps");
		if (!mapDir.exists()) {
			mapDir.mkdir();
		}

		this.saveConfig();
	}

	public ResourcePackManager getResourcePackManager() {
		return this.resourcePackManager;
	}

	@Override
	public void saveConfig() {
		try {
			config.save(configFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public FileConfiguration getConfig() {
		return config;
	}
}
