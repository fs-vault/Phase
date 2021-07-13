package xyz.nkomarn.Phase;

import com.firestartermc.kerosene.Kerosene;
import com.firestartermc.kerosene.data.db.LocalStorage;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.nkomarn.Phase.command.SetWarpCommand;
import xyz.nkomarn.Phase.command.WarpAdminCommand;
import xyz.nkomarn.Phase.command.WarpCommand;
import xyz.nkomarn.Phase.listener.InventoryClickListener;
import xyz.nkomarn.Phase.listener.PlayerJoinListener;
import xyz.nkomarn.Phase.listener.SignListener;
import xyz.nkomarn.Phase.task.ExpirationTask;
import xyz.nkomarn.Phase.util.Database;

public class Phase extends JavaPlugin {

    private static Phase PHASE;
    private static LocalStorage STORAGE;

    public void onEnable() {
        PHASE = this;
        saveDefaultConfig();

        STORAGE = Kerosene.getKerosene().getLocalStorage("phase");

        if (!Database.initialize()) {
            getLogger().severe("Failed to initialize the database.");
            getServer().getPluginManager().disablePlugin(this);
        }

        getCommand("warp").setExecutor(new WarpCommand());
        getCommand("warpadmin").setTabCompleter(new WarpAdminCommand());
        getCommand("setwarp").setExecutor(new SetWarpCommand());

        Bukkit.getPluginManager().registerEvents(new PlayerJoinListener(), this);
        Bukkit.getPluginManager().registerEvents(new InventoryClickListener(), this);
        Bukkit.getPluginManager().registerEvents(new SignListener(), this);
        getServer().getScheduler().runTaskTimerAsynchronously(this, new ExpirationTask(), 0, 10 * 20);
    }

    public void onDisable() {
    }

    public static Phase getPhase() {
        return PHASE;
    }

    public static LocalStorage getStorage() {
        return STORAGE;
    }
}
