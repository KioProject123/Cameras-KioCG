package water.of.cup.cameras.listeners;

//import java.util.Map;

import org.bukkit.ChatColor;
//import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
//import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
//import org.bukkit.inventory.ItemStack;

import org.bukkit.inventory.ItemStack;
//import org.bukkit.inventory.meta.ItemMeta;
import water.of.cup.cameras.Camera;
import water.of.cup.cameras.Picture;

public class CameraClick implements Listener {

	private Camera instance = Camera.getInstance();

	@EventHandler
	public void cameraClicked(PlayerInteractEvent e) {
		if (!e.getAction().isRightClick()) {
			return;
		}

		final ItemStack itemStack = e.getItem();
		if(itemStack == null)
			return;

		if (!itemStack.hasCustomModelData() || itemStack.getCustomModelData() != 14) {
			return;
		}

		Player p = e.getPlayer();

			boolean usePerms = instance.getConfig().getBoolean("settings.camera.permissions");
			if(usePerms && !p.hasPermission("cameras.useitem")) return;

			boolean messages = instance.getConfig().getBoolean("settings.messages.enabled");
			if (p.getInventory().firstEmpty() == -1) { //check to make sure there is room in the inventory for the map
				if(messages) {
					p.sendMessage(ChatColor.translateAlternateColorCodes('&', instance.getConfig().getString("settings.messages.invfull")));
				}
				return;
			}
//			if (p.getInventory().contains(Material.PAPER)) { //check to make sure the player has paper
				boolean tookPicture = Picture.takePicture(p);

				if(tookPicture) {
					//remove 1 paper from the player's inventory
//					Map<Integer, ? extends ItemStack> paperHash = p.getInventory().all(Material.PAPER);
//					for (ItemStack item : paperHash.values()) {
//						item.setAmount(item.getAmount() - 1);
//						break;
//					}

					//remove 1 camera from the player's inventory - KioCG
					itemStack.subtract();
				}
//			} else {
//				if(messages) {
//					p.sendMessage(ChatColor.translateAlternateColorCodes('&', instance.getConfig().getString("settings.messages.nopaper")));
//				}
//			}
			
	}
}
