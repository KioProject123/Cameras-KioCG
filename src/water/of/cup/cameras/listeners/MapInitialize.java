package water.of.cup.cameras.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.MapInitializeEvent;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import water.of.cup.cameras.Camera;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class MapInitialize implements Listener {
    @EventHandler
    public void onMapInitialize(final MapInitializeEvent e) {
        MapView mapView = e.getMap();
        int mapId = mapView.getId();
        final File file = new File(Camera.getInstance().getDataFolder(), "maps/map_" + mapId + ".txt");
        if (file.exists()) {
            try {
                BufferedReader br = new BufferedReader(new FileReader(file));
                String encodedData = br.readLine();

                mapView.setTrackingPosition(false);
                for (MapRenderer renderer : mapView.getRenderers()) {
                    mapView.removeRenderer(renderer);
                }

                mapView.addRenderer(new MapRenderer() {
                    @Override
                    public void render(MapView mapViewNew, MapCanvas mapCanvas, Player player) {
                        int x = 0;
                        int y = 0;
                        int skipsLeft = 0;
                        byte colorByte = 0;
                        for (int index = 0; index < encodedData.length(); index++) {
                            if (skipsLeft == 0) {
                                int end = index;

                                while (encodedData.charAt(end) != ',') {
                                    end++;
                                }

                                String str = encodedData.substring(index, end);
                                index = end;

                                colorByte = Byte.parseByte(str.substring(0, str.indexOf('_')));

                                skipsLeft = Integer.parseInt(str.substring(str.indexOf('_') + 1));

                            }

                            while (skipsLeft != 0) {
                                mapCanvas.setPixel(x, y, colorByte);

                                y++;
                                if (y == 128) {
                                    y = 0;
                                    x++;
                                }

                                skipsLeft -= 1;
                            }
                        }
                    }
                });

            } catch (Exception ex) {
                ex.printStackTrace();
            }

        }
    }
}
