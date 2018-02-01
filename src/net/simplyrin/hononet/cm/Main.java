package net.simplyrin.hononet.cm;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.java.JavaPlugin;

import ru.tehkode.permissions.bukkit.PermissionsEx;

/**
 * Created by SimplyRin on 2018/02/01.
 */
public class Main extends JavaPlugin implements Listener {

	/**
	 * このプラグインは 2016/11/13 に作成されたプラグインを元に再構成されたものです。
	 */
	private static Main plugin;

	@Override
	public void onEnable() {
		plugin = this;
		if(!plugin.getDescription().getAuthors().contains("SimplyRin")) {
			plugin.getServer().getPluginManager().disablePlugin(this);
			return;
		}

		if(plugin.getServer().getPluginManager().getPlugin("PermissionsEx") == null) {
			plugin.getLogger().info(this.getPrefix() + "PermissionsEx が見つかりませんでした。プラグインを導入してください。");
			plugin.getServer().getPluginManager().disablePlugin(this);
			return;
		}

		plugin.saveDefaultConfig();
		plugin.getCommand("cm").setExecutor(this);
		plugin.getServer().getPluginManager().registerEvents(this, this);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		if(!sender.hasPermission("commandmonitoring.use")) {
			sender.sendMessage(this.getPrefix() + "§cYou do not have access to this command");
			return true;
		}

		if(args.length > 0) {
			if(args[0].equalsIgnoreCase("true") || args[0].equalsIgnoreCase("false")) {
				boolean b = Boolean.valueOf(args[0]);

				plugin.getConfig().set("Toggle", b);
				plugin.saveConfig();
				plugin.reloadConfig();

				sender.sendMessage(this.getPrefix() + "§aコマンド送信表示を " + (b ? "§b有効化" : "§c無効化") + " §aしました。");
				return true;
			}

			if(args[0].equalsIgnoreCase("reload")) {
				plugin.reloadConfig();
				sender.sendMessage(this.getPrefix() + "§aConfig ファイルをリロードしました。");
				return true;
			}

			if(args[0].equalsIgnoreCase("layout")) {
				if(args.length > 1) {
					String layout = "";
					for (int i = 1; i < args.length; i++) {
						layout = layout + args[i] + " ";
					}
					int length = layout.length();
					length--;
					layout = layout.substring(0, length);
					plugin.getConfig().set("Layout", layout);
					plugin.saveConfig();
					plugin.reloadConfig();

					sender.sendMessage(this.getPrefix() + "§aレイアウトを以下に変更しました。");
					sender.sendMessage(this.getPrefix() + "§b'§a" + layout + "§b'");
					return true;
				}

				sender.sendMessage(this.getPrefix() + "§cUsage: /cm layout <layout>");
				sender.sendMessage(this.getPrefix() + "§a置き換え文字");
				sender.sendMessage(this.getPrefix() + "%player: コマンドを送信したプレイヤー名に置き換わります。");
				sender.sendMessage(this.getPrefix() + "%command: 送信されたコマンドに置き換えます。");
				sender.sendMessage(this.getPrefix() + "%prefix: コマンドを送信したプレイヤーのプレフィックスに置き換わります。");
				sender.sendMessage(this.getPrefix() + "%suffix: コマンドを送信したプレイヤーのサフィックスに置き換わります。");
				return true;
			}
		}

		sender.sendMessage(this.getPrefix() + "§ccommands:");
		sender.sendMessage(this.getPrefix() + "§a/cm true: コマンド送信表示を §b有効化 §aします。");
		sender.sendMessage(this.getPrefix() + "§a/cm true: コマンド送信表示を §c無効化 §aします。");
		sender.sendMessage(this.getPrefix() + "§a/cm reload: Configファイルをリロードします。");
		sender.sendMessage(this.getPrefix() + "§a/cm layout <layout>: コマンド送信時に表示されるレイアウトを変更します。");
		return true;
	}

	@EventHandler
	public void oncommand(PlayerCommandPreprocessEvent event) {
		Player player = event.getPlayer();
		String command = event.getMessage();

		String layout = plugin.getConfig().getString("Toggle");

		layout = layout.replaceAll("%player", player.getName());
		layout = layout.replaceAll("%command", command);

		try {
			layout = layout.replaceAll("%prefix", PermissionsEx.getUser(player).getGroups()[0].getPrefix());
		} catch (Exception e) {
		}

		try {
			layout = layout.replaceAll("%suffix", PermissionsEx.getUser(player).getGroups()[0].getSuffix());
		} catch (Exception e) {
		}

		layout = ChatColor.translateAlternateColorCodes('&', layout);

		for(Player target : plugin.getServer().getOnlinePlayers()) {
			String[] args = command.split(" ");
			List<String> disablecommand = plugin.getConfig().getStringList("DisableCommands");

			for (String disable : disablecommand) {
				if (args[0].equalsIgnoreCase(disable)) {
					return;
				}
			}

			if(!target.hasPermission("commandmonitoring.view")) {
				return;
			}

			if(!plugin.getConfig().getBoolean("Enable")) {
				return;
			}

			target.sendMessage(layout);
		}
	}

	public String getPrefix() {
		return "§e[§bCM§e] §f";
	}
}
