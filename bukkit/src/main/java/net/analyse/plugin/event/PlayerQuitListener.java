package net.analyse.plugin.event;

import net.analyse.plugin.AnalysePlugin;
import net.analyse.plugin.hook.PlaceholderAPIStatisticsHook;
import net.analyse.sdk.exception.ServerNotFoundException;
import net.analyse.sdk.obj.AnalysePlayer;
import net.analyse.sdk.obj.PlayerStatistic;
import net.analyse.sdk.util.VersionUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.List;
import java.util.logging.Level;

public class PlayerQuitListener implements Listener {
    private final AnalysePlugin platform;

    public PlayerQuitListener(AnalysePlugin platform) {
        this.platform = platform;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player bukkitPlayer = event.getPlayer();
        AnalysePlayer player = platform.getPlayers().get(bukkitPlayer.getUniqueId());

        if(player == null) return;

        platform.updatePlaceholderAPIStatistics(bukkitPlayer, player.getStatistics());
        platform.debug("Preparing to track " + bukkitPlayer.getName() + "..");

        platform.getSDK().trackPlayerSession(player).thenAccept(successful -> {
            if(! successful) {
                platform.warning("Failed to track player session for " + player.getName() + ".");
                return;
            }

            platform.debug("Successfully tracked player session for " + player.getName() + ".");
        }).exceptionally(ex -> {
            Throwable cause = ex.getCause();
            platform.log(Level.WARNING, "Failed to track player session: " + cause.getMessage());

            if(cause instanceof ServerNotFoundException) {
                platform.halt();
            } else {
                cause.printStackTrace();
            }

            return null;
        });
    }
}
