package water.of.cup.cameras;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

public class Picture {
	public static boolean takePicture(Player p) {
        Camera instance = Camera.getInstance();
        boolean messages = instance.getConfig().getBoolean("settings.messages.enabled");
        if(instance.getResourcePackManager().isLoaded()) {
        } else {
            if(messages) {
                p.sendMessage(ChatColor.translateAlternateColorCodes('&', instance.getConfig().getString("settings.messages.notready")));
            }
            return false;
        }

		ItemStack itemStack = new ItemStack(Material.FILLED_MAP); // requires api-version: 1.13 in plugin.yml
        MapMeta mapMeta = (MapMeta) itemStack.getItemMeta();
        MapView mapView = Bukkit.createMap(p.getWorld()); 

        mapView.setTrackingPosition(false);

        for(MapRenderer renderer : mapView.getRenderers())
            mapView.removeRenderer(renderer);

        Renderer customRenderer = new Renderer();
        mapView.addRenderer(customRenderer);
		mapMeta.setMapView(mapView);
		
        itemStack.setItemMeta(mapMeta);
        p.getInventory().addItem(itemStack);

		return true;
	}
}
