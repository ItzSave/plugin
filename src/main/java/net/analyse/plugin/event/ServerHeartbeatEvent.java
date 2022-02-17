package net.analyse.plugin.event;

import net.analyse.plugin.AnalysePlugin;
import net.analyse.plugin.request.PluginAPIRequest;
import net.analyse.plugin.request.ServerHeartbeatRequest;
import net.analyse.plugin.request.object.PlayerStatistic;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;

public class ServerHeartbeatEvent implements Runnable {

    private final AnalysePlugin plugin;

    public ServerHeartbeatEvent(final @NotNull AnalysePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        if (!plugin.isSetup()) {
            return;
        }

        ServerHeartbeatRequest serverHeartbeatRequest = new ServerHeartbeatRequest(Bukkit.getOnlinePlayers().size());

        PluginAPIRequest apiRequest = new PluginAPIRequest("server/heartbeat");

        apiRequest.getRequest()
                .header("X-SERVER-TOKEN", plugin.getConfig().getString("server.token"))
                .POST(HttpRequest.BodyPublishers.ofString(serverHeartbeatRequest.toJson()));

        HttpResponse<String> httpResponse = apiRequest.send();

        if (httpResponse.statusCode() == 404) {
            plugin.getLogger().severe("The server that was configured no longer exists!");
            plugin.setSetup(false);
        }
    }
}
