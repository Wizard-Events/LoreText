package ron.thewizard.loretext;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class LoreText extends JavaPlugin implements CommandExecutor, TabCompleter {

    private PluginCommand loreTextCmd;
    private List<String> fileNameTabCompletions;
    private long nextPossibleTabCompleteFileScan;
    private Permission loreTextPermission;
    private ComponentLogger logger;

    @Override
    public void onEnable() {
        try {
            // Just in case someone tries to put this into a spigot server (ew)
            Class.forName("net.kyori.adventure.text.Component");
        } catch (ClassNotFoundException e) {
            getLogger().severe("This plugin requires Kyori's libraries.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        logger = getComponentLogger();

        try {
            if (!getDataFolder().exists()) {
                Files.createDirectories(getDataPath());
                saveResource("buff_ron.yaml", false);
            }
        } catch (Exception e) {
            logger.error("Unable to create plugin directory.", e);
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        try {
            loreTextCmd = Objects.requireNonNull(getCommand("loretext"), "Command is not defined in the plugin.yml");
            loreTextCmd.setExecutor(this);
            fileNameTabCompletions = new ArrayList<>();
            nextPossibleTabCompleteFileScan = System.currentTimeMillis();
            loreTextCmd.setTabCompleter(this);
        } catch (Throwable t) {
            logger.error("Unable to register command.", t);
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        try {
            loreTextPermission = new Permission("loretext.apply", PermissionDefault.OP);
            getServer().getPluginManager().addPermission(loreTextPermission);
        } catch (IllegalArgumentException ignored) {
            logger.warn("Permission loretext.apply has already been registered by another plugin. This might cause problems.");
        }

        logger.info("Done");
    }

    @Override
    public void onDisable() {
        if (loreTextCmd != null) {
            loreTextCmd.unregister(getServer().getCommandMap());
            loreTextCmd.setTabCompleter(null);
            loreTextCmd.setExecutor(null);
            loreTextCmd = null;
        }
        if (loreTextPermission != null) {
            getServer().getPluginManager().removePermission(loreTextPermission);
            loreTextPermission = null;
        }
        if (fileNameTabCompletions != null) {
            fileNameTabCompletions.clear();
            fileNameTabCompletions = null;
        }
        logger = null;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission(loreTextPermission)) {
            sender.sendMessage(Component.text("You don't have permissies :I", Utils.wizardRed));
            return true;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Only players can execute this command", Utils.wizardRed));
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(Component.text("Missing argument. Syntax: /loretext somefile", Utils.wizardRed));
            return true;
        }

        ItemStack itemStack = player.getInventory().getItemInMainHand();

        if (itemStack.getType().isAir()) {
            sender.sendMessage(Component.text("There is nothing in your main hand", Utils.wizardRed));
            return true;
        }

        File loreFile = new File(getDataFolder(), args[0]);

        try {
            if (!loreFile.exists()) {
                sender.sendMessage(Component.text("File doesn't exist", Utils.wizardRed));
                logger.warn("{} failed setting itemlore from file because it does not exist", sender.getName());
                return true;
            }

            if (loreFile.isDirectory()) {
                sender.sendMessage(Component.text("Specified file is a directory", Utils.wizardRed));
                logger.warn("{} failed setting itemlore from file because it is a directory", sender.getName());
                return true;
            }

            List<Component> lore = Utils.readUTF8File(loreFile);

            ItemMeta itemMeta = itemStack.getItemMeta();
            itemMeta.lore(lore);
            itemStack.setItemMeta(itemMeta);
            sender.sendMessage(Component.text("Itemlore successfully set!", Utils.wizardPurple));
        } catch (Exception e) {
            sender.sendMessage(Component.text("Couldn't set itemlore: " + e.getLocalizedMessage(), Utils.wizardRed));
            logger.error("Error setting itemlore from file '{}' ", loreFile.getPath(), e);
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length > 1 || !sender.hasPermission(loreTextPermission)) {
            return Collections.emptyList();
        }

        if (nextPossibleTabCompleteFileScan > System.currentTimeMillis()) {
            return fileNameTabCompletions;
        }

        fileNameTabCompletions.clear();

        try {
            for (File yamlList : Objects.requireNonNull(getDataFolder().listFiles(), "Result of directory listFiles was null")) {
                fileNameTabCompletions.add(yamlList.getName());
            }
        } catch (Throwable t) {
            logger.error("Something went wrong while preparing a list of tab-completions using the files in {}",
                    getDataFolder().getPath(), t);
        }

        nextPossibleTabCompleteFileScan = System.currentTimeMillis() + 2000L;
        return fileNameTabCompletions;
    }
}
