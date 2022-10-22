package water.of.cup.cameras;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import io.papermc.paper.event.server.ServerResourcesReloadedEvent;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.bukkit.plugin.java.JavaPlugin;

import water.of.cup.cameras.commands.CameraCommands;
import water.of.cup.cameras.listeners.CameraClick;

public class Camera extends JavaPlugin implements Listener {

	private static Camera instance;
	List<Integer> mapIDsNotToRender = new ArrayList<>();
	ResourcePackManager resourcePackManager = new ResourcePackManager();
	private File configFile;
	private FileConfiguration config;

	public NamespacedKey key = new NamespacedKey(this, "camera");

	@Override
	public void onEnable() {
		instance = this;

		loadConfig();

		this.resourcePackManager.initialize();

		File folder = new File(getDataFolder() + "/maps");
		File[] listOfFiles = folder.listFiles();

		for (File file : listOfFiles) {
			if (file.isFile()) {
				int mapId = Integer.parseInt(file.getName().split("_")[1].split(Pattern.quote("."))[0]);
				try {
					BufferedReader br = new BufferedReader(new FileReader(file));
					String encodedData = br.readLine();

					MapView mapView = Bukkit.getMap(Integer.valueOf(mapId));

					mapView.setTrackingPosition(false);
					for(MapRenderer renderer : mapView.getRenderers())
						mapView.removeRenderer(renderer);

					mapView.addRenderer(new MapRenderer() {
						@Override
						public void render(MapView mapViewNew, MapCanvas mapCanvas, Player player) {
							if(!mapIDsNotToRender.contains(mapId)) {
								mapIDsNotToRender.add(mapId);

								int x = 0;
								int y = 0;
								int skipsLeft = 0;
								byte colorByte = 0;
								for(int index = 0; index < encodedData.length(); index++) {
									if(skipsLeft == 0) {
										int end = index;

										while(encodedData.charAt(end) != ',')
											end++;

										String str = encodedData.substring(index, end);
										index = end;

										colorByte = Byte.parseByte(str.substring(0, str.indexOf('_')));

										skipsLeft = Integer.parseInt(str.substring(str.indexOf('_') + 1));

									}

									while(skipsLeft != 0) {
										mapCanvas.setPixel(x, y, colorByte);

										y++;
										if(y == 128) {
											y = 0;
											x++;
										}

										skipsLeft -= 1;
									}
								}
							}
						}
					});

				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		}

		Utils.loadColors();
		getCommand("takePicture").setExecutor(new CameraCommands());
		registerListeners(new CameraClick(), this /* 兼容数据包重载 */ );

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

		cameraMeta.setCustomModelData(14); // KioCG
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
