package io.github.undefinedmaniac.mcscpplugin;

import io.github.undefinedmaniac.mcscpserver.MCSCPServer;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.Bukkit;

public class MCSCPPlugin extends JavaPlugin {

    private MCSCPServer mServer = null;
    private static float tps;

    public static float getTPS() {
        return tps;
    }

    @Override
    public void onEnable() {
        mServer = new MCSCPServer(this);
        mServer.runTaskAsynchronously(this);

        //Start timer for TPS
        Bukkit.getServer().getScheduler().runTaskTimer(this, new Runnable() {

            long secstart;
            long secend;

            int ticks;

            @Override
            public void run() {
                secstart = (System.currentTimeMillis() / 1000);

                if (secstart == secend) {
                    ticks++;
                } else {
                    secend = secstart;
                    tps = (tps == 0) ? ticks : ((tps + ticks) / 2);
                    ticks = 1;
                }
            }

        }, 0, 1);
    }

    @Override
    public void onDisable() {
        if (mServer != null)
            mServer.stop();
    }
}
