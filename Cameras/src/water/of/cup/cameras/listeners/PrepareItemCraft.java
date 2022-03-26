package water.of.cup.cameras.listeners;

import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import water.of.cup.cameras.Camera;

public class PrepareItemCraft implements Listener {

    private Camera instance = Camera.getInstance();

    @EventHandler
    public void prepareItemCraft(PrepareItemCraftEvent event) {
        if (!(event.getRecipe() instanceof ShapedRecipe shapedRecipe)) {
            return;
        }

        if (!shapedRecipe.getKey().equals(instance.key)) {
            return;
        }

        if (instance.getConfig().getBoolean("settings.camera.permissions")) {
            for (HumanEntity he : event.getViewers()) {
                if (he instanceof Player) {
                    if (!he.hasPermission("cameras.craft")) {
                        event.getInventory().setResult(new ItemStack(Material.AIR));
                    }
                }
            }
        }
    }
}
