package water.of.cup.cameras.listeners;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import water.of.cup.cameras.Camera;
import water.of.cup.cameras.Picture;

import java.util.Objects;

public class CameraClick implements Listener {
    private final Camera instance = Camera.getInstance();

    @EventHandler
    public void cameraClicked(final PlayerInteractEvent e) {
        final Action action = e.getAction();
        if (action != Action.RIGHT_CLICK_AIR) {
            if (action != Action.RIGHT_CLICK_BLOCK || Objects.requireNonNull(e.getClickedBlock()).getType().isInteractable()) {
                return;
            }
        }

        final ItemStack itemStack = e.getItem();
        if (itemStack == null || itemStack.getType() != Material.PLAYER_HEAD) {
            return;
        }

        final ItemMeta itemMeta = itemStack.getItemMeta();
        if (!itemMeta.hasCustomModelData() || itemMeta.getCustomModelData() != 14) {
            return;
        }

        e.setCancelled(true);

        final Player player = e.getPlayer();

        if (instance.getConfig().getBoolean("settings.camera.permissions")
            && !player.hasPermission("cameras.useitem")) {
            return;
        }

        if (player.getInventory().firstEmpty() == -1) {
            if (instance.getConfig().getBoolean("settings.messages.enabled")) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(instance.getConfig().getString("settings.messages.invfull"))));
            }
            return;
        }

        if (Picture.takePicture(player)) {
            itemStack.subtract();
            player.playSound(player.getLocation(), Sound.BLOCK_PISTON_EXTEND, 0.5F, 2.0F);
        }
    }
}
