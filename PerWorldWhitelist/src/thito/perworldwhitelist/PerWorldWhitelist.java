package thito.perworldwhitelist;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class PerWorldWhitelist extends JavaPlugin implements Listener {

	/*
	 * PerWorldWhitelist
	 * - Whitelist World -
	 * 
	 * Any methods here are config-dynamic.
	 * When anything changed, the config will always updated.
	 */
	public static final String DEFAULT_MESSAGE = "&cYou are not whitelisted in %world world.";
	public void onEnable() {
		reloadConfig();
		getServer().getPluginManager().registerEvents(this, this);
		getServer().getConsoleSender().sendMessage(ChatColor.GREEN+"Using "+getDescription().getName()+" v"+getDescription().getVersion()+" by "+getAuthor());
	}
	
	public boolean onCommand(CommandSender sender,Command cmd,String label,String[]args) {
		if (!sender.hasPermission("perworldwhitelist.admin")) {
			sender.sendMessage(ChatColor.RED+"I'm sorry, but you do not have permission to perform this command. Please contact the server administrators if you believe that this is in error.");
			return true;
		}
		if (args.length > 0) {
			World w = getServer().getWorld(args[0]);
			if (w == null) {
				if (args[0].equalsIgnoreCase("reload")) {
					try {
						reloadConfig();
					} catch (Exception e) {
						sender.sendMessage(ChatColor.RED+"Failed to reload config! Check console for more information.");
						e.printStackTrace();
						return true;
					}
					sender.sendMessage(ChatColor.GREEN+"Config reloaded!");
					return true;
				} 
				sender.sendMessage(ChatColor.RED+"World not found: "+args[0]);
				return true;
			} else {
				if (args[0].equalsIgnoreCase("reload")) {
					if (args.length > 1 && args[1].equalsIgnoreCase("config")) {
						try {
							reloadConfig();
						} catch (Exception e) {
							sender.sendMessage(ChatColor.RED+"Failed to reload config! Check console for more information.");
							e.printStackTrace();
							return true;
						}
						Command.broadcastCommandMessage(sender, ChatColor.GREEN+"PerWorldPlugin configuration reloaded!",false);
						sender.sendMessage(ChatColor.GREEN+"Config reloaded!");
						return true;
					}
				}
				if (args.length > 1) {
					if (args[1].equalsIgnoreCase("info")) {
						sender.sendMessage(ChatColor.GREEN+"World Whitelist Info ("+w.getName()+")");
						sender.sendMessage(ChatColor.YELLOW+"Enable: "+(isEnabled(w) ? ChatColor.GREEN.toString()+ChatColor.BOLD+"YES":ChatColor.RED.toString()+ChatColor.BOLD+"NO"));
						sender.sendMessage(ChatColor.YELLOW+"Message: "+ChatColor.AQUA+getMessage(w));
						List<String> players = getPlayers(w);
						sender.sendMessage(ChatColor.YELLOW+"Players ("+ChatColor.AQUA.toString()+players.size()+ChatColor.YELLOW.toString()+"):");
						for (String s : players) {
							sender.sendMessage(ChatColor.DARK_AQUA+"- "+s);
						}
						if (players.isEmpty()) {
							sender.sendMessage(ChatColor.RED+"- There's no player whitelisted here -");
						}
						return true;
					}
					if (args[1].equalsIgnoreCase("add")) {
						if (args.length > 2) {
							if (addPlayer(w,args[2])) {
								sender.sendMessage(ChatColor.GREEN+"Added player "+args[2]+" to the "+args[0]+"'s whitelist.");
								Command.broadcastCommandMessage(sender, ChatColor.GREEN+"Added player "+args[2]+" to the "+args[0]+"'s whitelist.",false);
							} else {
								sender.sendMessage(ChatColor.RED+"Player "+args[2]+" is already on "+args[0]+"'s whitelist.");
								Command.broadcastCommandMessage(sender, ChatColor.RED+"Player "+args[2]+" is already on "+args[0]+"'s whitelist.",false);
							}
							return true;
						}
						sender.sendMessage(ChatColor.YELLOW+"Add player to the world whitelist. Usage: /"+label+" "+w.getName()+" add <player>");
						return true;
					}
					if (args[1].equalsIgnoreCase("remove")) {
						if (args.length > 2) {
							if (removePlayer(w,args[2])) {
								sender.sendMessage(ChatColor.GREEN+"Removed player "+args[2]+" from the "+args[0]+"'s whitelist.");
								Command.broadcastCommandMessage(sender, ChatColor.GREEN+"Removed player "+args[2]+" from the "+args[0]+"'s whitelist.",false);
							} else {
								sender.sendMessage(ChatColor.RED+"Player "+args[2]+" is not whitelisted on "+args[0]+"'s whitelist.");
								Command.broadcastCommandMessage(sender, ChatColor.RED+"Player "+args[2]+" is not whitelisted on "+args[0]+"'s whitelist.",false);
							}
							return true;
						}
						sender.sendMessage(ChatColor.YELLOW+"Remove player from the world whitelist. Usage: /"+label+" "+w.getName()+" remove <player>");
						return true;
					}
					if (args[1].equalsIgnoreCase("enable")) {
						if (args.length > 2) {
							switch (args[2].toLowerCase()) {
							case "true":
							case "yes":
							case "ya":
							case "oui":
							case "ok":
							case "sure":
								setEnabled(w,true);
								sender.sendMessage(ChatColor.GREEN+"Whitelist for world "+w.getName()+" has been enabled!");
								Command.broadcastCommandMessage(sender, ChatColor.GREEN+"Whitelist for world "+w.getName()+" has been enabled!", false);
								return true;
							case "false":
							case "no":
							case "tidak":
							case "dont":
							case "don't":
							case "do not":
							case "cant":
							case "cannot":
							case "can't":
								setEnabled(w,false);
								sender.sendMessage(ChatColor.GREEN+"Whitelist for world "+w.getName()+" has been disabled!");
								Command.broadcastCommandMessage(sender, ChatColor.GREEN+"Whitelist for world "+w.getName()+" has been disabled!", false);
								return true;
							}
						}
						sender.sendMessage(ChatColor.YELLOW+"Enable or disable world whitelist. Usage: /"+label+" "+w.getName()+" enable <true|false>");
						return true;
					}
					if (args[1].equalsIgnoreCase("fallback")) {
						if (args.length > 2) {
							switch (args[2].toLowerCase()) {
							case "true":
							case "yes":
							case "ya":
							case "oui":
							case "ok":
							case "sure":
								setFallback(w,true);
								sender.sendMessage(ChatColor.GREEN+"World "+w.getName()+" has been setted as Fallback");
								Command.broadcastCommandMessage(sender, ChatColor.GREEN+"World "+w.getName()+" has been setted as Fallback", false);
								return true;
							case "false":
							case "no":
							case "tidak":
							case "dont":
							case "don't":
							case "do not":
							case "cant":
							case "cannot":
							case "can't":
								setFallback(w,false);
								sender.sendMessage(ChatColor.GREEN+"World "+w.getName()+" is no longer setted as Fallback");
								Command.broadcastCommandMessage(sender, ChatColor.GREEN+"World "+w.getName()+" is no longer setted as Fallback", false);
								return true;
							}
						}
						sender.sendMessage(ChatColor.YELLOW+"Set the world as fallback. Usage: /"+label+" "+w.getName()+" fallback <true|false>");
						return true;
					}
					if (args[1].equalsIgnoreCase("setmessage")) {
						if (args.length > 2) {
							String msg = args[2];
							for (int i = 3; i < args.length; i++) msg+=" "+args[i];
							setMessage(w,msg);
							msg = getMessage(w);
							sender.sendMessage(ChatColor.GREEN+"Message for "+w.getName()+" whitelist has been setted to \""+msg+ChatColor.GREEN+"\"");
							Command.broadcastCommandMessage(sender, ChatColor.GREEN+"Message for "+w.getName()+" whitelist has been setted to \""+msg+ChatColor.GREEN+"\"", false);
							return true;
						}
						sender.sendMessage(ChatColor.YELLOW+"Set the world whitelist's message. Usage: /"+label+" "+w.getName()+" setmessage <message|null>");
						sender.sendMessage(ChatColor.GRAY+"Placeholder: %world - The World Name | %player - The Player Name");
						return true;
					}
				}
			}
		}
		sender.sendMessage(ChatColor.GREEN+getDescription().getName()+" v"+getDescription().getVersion()+" by "+getAuthor());
		sender.sendMessage(ChatColor.YELLOW+"/"+label+" <world> info - Show world whitelist info");
		sender.sendMessage(ChatColor.YELLOW+"/"+label+" <world> enable <true|false> - Enable or disable world whitelist");
		sender.sendMessage(ChatColor.YELLOW+"/"+label+" <world> add <player> - Add player to the world whitelist");
		sender.sendMessage(ChatColor.YELLOW+"/"+label+" <world> remove <player> - Remove player from the world whitelist");
		sender.sendMessage(ChatColor.YELLOW+"/"+label+" <world> setmessage <message|null> - Set the world whitelist's message");
		sender.sendMessage(ChatColor.YELLOW+"/"+label+" <world> fallback <true|false> - Set the world as fallback");
		sender.sendMessage(ChatColor.YELLOW+"/"+label+" reload config - Reload the plugin configuration");
		return true;
	}
	
	private String getAuthor() {
		String string = getDescription().getAuthors().toString();
		return string.substring(1, string.length()-1);
	}
	
	public boolean addPlayer(World w,String player) {
		try {
			reloadConfig();
		} catch (Exception e) {
		}
		List<String> strings = getConfig().getStringList("worlds."+w.getName()+".players");
		if (strings == null) strings = new ArrayList<>();
		if (strings.contains(player)) return false;
		strings.add(player);
		getConfig().set("worlds."+w.getName()+".players", strings);
		saveConfig();
		return true;
	}
	
	public boolean removePlayer(World w,String player) {
		try {
			reloadConfig();
		} catch (Exception e) {
		}
		List<String> strings = getConfig().getStringList("worlds."+w.getName()+".players");
		if (strings == null) strings = new ArrayList<>();
		if (!strings.remove(player)) return false;
		getConfig().set("worlds."+w.getName()+".players", strings);
		saveConfig();
		return true;
	}
	
	public List<String> getPlayers(World w) {
		try {
			reloadConfig();
		} catch (Exception e) {
		}
		List<String> strings = getConfig().getStringList("worlds."+w.getName()+".players");
		if (strings == null) strings = new ArrayList<>();
		return strings;
	}
	
	public boolean isEnabled(World w) {
		try {
			reloadConfig();
		} catch (Exception e) {
		}
		return getConfig().getBoolean("worlds."+w.getName()+".enable");
	}
	
	public boolean isFallback(World w) {
		try {
			reloadConfig();
		} catch (Exception e) {
		}
		return getConfig().getBoolean("worlds."+w.getName()+".fallback");
	}
	
	public String getMessage(World w) {
		try {
			reloadConfig();
		} catch (Exception e) {
		}
		String msg = getConfig().getString("worlds."+w.getName()+".message");
		if (msg == null) msg = DEFAULT_MESSAGE;
		return ChatColor.translateAlternateColorCodes('&', msg);
	}
	
	public void setEnabled(World w, boolean e) {
		try {
			reloadConfig();
		} catch (Exception ex) {
		}
		getConfig().set("worlds."+w.getName()+".enable", e);
		saveConfig();
	}
	
	public void setFallback(World w, boolean e) {
		try {
			reloadConfig();
		} catch (Exception ex) {
		}
		getConfig().set("worlds."+w.getName()+".fallback", e);
		saveConfig();
	}
	
	public void setMessage(World w,String m) {
		if (m.equals("null")) m = null;
		try {
			reloadConfig();
		} catch (Exception e) {
		}
		getConfig().set("worlds."+w.getName()+".message", m);
		saveConfig();
	}
	
	public void reloadConfig() {
		if (!new File(getDataFolder(),"config.yml").exists()) {
			saveDefaultConfig();
		}
		super.reloadConfig();
	}
	
	public boolean moveToFallback(Player p) {
		for (final World w : Bukkit.getWorlds()) {
			if (!isFallback(w)) continue;
			if (!isEnabled(w) || getPlayers(w).contains(p.getName())) {
				p.teleport(w.getSpawnLocation());
				return true;
			}
		}
		return false;
	}
	
	@EventHandler(priority=EventPriority.LOWEST)
	public void dispatchTeleport(PlayerTeleportEvent e) {
		final Location dest = e.getTo();
		final World world = dest.getWorld();
		final Player player = e.getPlayer();
		if (isEnabled(world)) {
			if (!getPlayers(world).contains(player.getName())) {
				e.setCancelled(true);
				player.sendMessage(getMessage(world)
						.replace("%world", world.getName())
						.replace("%player", player.getName()));
			}
		}
	}
	
	@EventHandler(priority=EventPriority.LOWEST)
	public void dispatchLogin(PlayerLoginEvent e) {
		final World world = e.getPlayer().getWorld();
		final Player player = e.getPlayer();
		if (isEnabled(world)) {
			if (!getPlayers(world).contains(player.getName())) {
				if (moveToFallback(player)) {
					player.sendMessage(getMessage(world).replace("%world", world.getName()).replace("%player", player.getName()));
				} else {
					e.disallow(Result.KICK_WHITELIST, getMessage(world)
							.replace("%world", world.getName())
							.replace("%player", player.getName()));
				}
			}
		}
	}
	
	@EventHandler(priority=EventPriority.LOWEST)
	public void dispatchChangedWorld(PlayerChangedWorldEvent e) {
		final World world = e.getPlayer().getWorld();
		final Player player = e.getPlayer();
		if (isEnabled(world)) {
			if (!getPlayers(world).contains(player.getName())) {
				if (moveToFallback(player)) {
					player.sendMessage(getMessage(world).replace("%world", world.getName()).replace("%player", player.getName()));
				} else {
					player.kickPlayer(getMessage(world)
						.replace("%world", world.getName())
						.replace("%player", player.getName()));
				}
			}
		}
	}
}
