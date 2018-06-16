package me.MCPFun.main;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import me.MCPFun.gamemodes.AmmunitionConundrum;
import me.MCPFun.reference.FunType;
import me.MCPFun.reference.GameSpawns;
import me.MCPFun.reference.UtilityCommands;

/**
 * This class represents the main class for this JavaPlugin
 * @author Kevin
 */
public class MCPFunMain extends JavaPlugin implements Listener{

	/**Command to start the randomization process*/
	private static final String name = "MCPFun";

	/**Speed of snowballs*/
	private static final double speed = 999;

	/**Plugin Manager for this Plugin*/
	private static PluginManager pluginManager;

	/**
	 * Current AC Game
	 */
	private AmmunitionConundrum gameAC;

	/**
	 * This CraftBukkit Server
	 */
	private Server server;

	/**
	 * Automatically run when this plugin is enabled
	 */
	@Override
	public void onEnable(){
		System.out.println("Enabling event listener for " + name + "...");
		server = Bukkit.getServer();
		pluginManager = Bukkit.getServer().getPluginManager();
		pluginManager.registerEvents(this, this);
		System.out.println("Success! " + name + " loaded!");
	}

	/**
	 * Automatically run when this plugin is disabled
	 */
	@Override
	public void onDisable(){
		System.out.println("Disabling Plugin " + this.getClass().getName() + "...");
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){

		String cmdName = cmd.getName();

		if (cmdName.equals("loc") && sender instanceof Player){
			Location loc = ((Player)sender).getLocation();
			String msg = "";
			msg += loc.getX();
			msg += ",";
			msg += loc.getY();
			msg += ",";
			msg += loc.getZ();
			msg += ",";
			msg += loc.getYaw();
			msg += ",";
			msg += loc.getPitch();
			sender.sendMessage(msg);
			return true;
		}

		//OP COMMANDS FROM HERE ON OUT
		if (!sender.isOp()){
			sender.sendMessage("" + ChatColor.RED + "You do not have permission to use this command.");
			return true;
		}

		if (cmdName.equals("hex")){
			if (args.length != 1){
				sender.sendMessage(ChatColor.RED + "Usage: /hex <Player>");
			}
			else{
				Player p = server.getPlayer(args[0]);
				if (p != null){
					server.broadcastMessage("" + ChatColor.DARK_RED + ChatColor.BOLD + "[MCP Anti-Cheat] " + ChatColor.RESET + ChatColor.GOLD + ChatColor.BOLD + p.getDisplayName() + ChatColor.RED + " is using a brightness exploit.");
				}
			}
			return true;
		}

		if (cmdName.equals("AC")){

			if (args.length == 0){
				sender.sendMessage(ChatColor.LIGHT_PURPLE + "Commands: create|removeall|next|set|remove|add|delete|over|info|stats|setscore|changescore|resetscore|loadspawns");
				return true;
			}

			String arg0 = args[0];

			// /AC create
			if (arg0.equals("create")){
				if (gameAC == null){
					if (sender instanceof Player){
						gameAC = new AmmunitionConundrum(server, (Player)sender);
					}
					else {
						gameAC = new AmmunitionConundrum(server, null);
					}
					UtilityCommands.deleteMobs(server);
				} 
				else {
					sender.sendMessage("" + ChatColor.LIGHT_PURPLE + "An AC Game already exists. Please delete the previous instance before creating a new one.");
				}
			}

			//Null check for game AC
			else if (gameAC == null){
				sender.sendMessage("" + ChatColor.LIGHT_PURPLE + "No AC Game Exists!");
				return true;
			}

			//Moderator Check for game AC
			else if (!(sender instanceof Player) || !sender.equals(gameAC.getModerator())){
				sender.sendMessage("" + ChatColor.LIGHT_PURPLE + "You are not the moderator for this AC Game.");
				return true;
			}

			// /AC delete
			else if (arg0.equals("delete")){
				gameAC = null;
				sender.sendMessage("" + ChatColor.LIGHT_PURPLE + "AC Game deleted.");
				server.broadcastMessage("" + ChatColor.GOLD + "Re-enabling Mob spawning...");
				server.broadcastMessage("" + ChatColor.GOLD + "Un-locking Players' foodLevels...");
				return true;
			}

			// /AC set <Player>
			else if (arg0.equals("set")){

				//Invalid Number of args
				if (args.length != 2){
					sender.sendMessage("" + ChatColor.LIGHT_PURPLE + "Usage: /AC set <Player>");
				} 

				//Valid Number of args
				else {
					Player p = server.getPlayer(args[1]);
					gameAC.setModerator(p);

				}

				return true;
			}

			// /AC add <Player>
			else if (arg0.equals("add")){
				//Invalid Number of args
				if (args.length != 2){
					sender.sendMessage("" + ChatColor.LIGHT_PURPLE + "Usage: /AC add <Player>");
				} 

				//Valid Number of args
				else {
					Player p = server.getPlayer(args[1]);
					gameAC.addPlayer(p);

				}

				return true;
			}

			// /AC remove <Player>
			else if (arg0.equals("remove")){
				//Invalid number of args
				if (args.length != 2){
					sender.sendMessage("" + ChatColor.LIGHT_PURPLE + "Usage: /AC remove <Player>");
				} 

				//Valid number of args
				else {
					Player p = server.getPlayer(args[1]);
					gameAC.removePlayer(p);
				}

				return true;
			}

			// /AC setscore <Player> number
			else if (arg0.equals("setscore")){
				//Invalid number of args
				if (args.length != 3){
					sender.sendMessage("" + ChatColor.LIGHT_PURPLE + "Usage: /AC setscore <Player> amount");
				} 

				//Valid number of args
				else{
					int score = 0;

					//Attempt to parse int
					try{
						score = Integer.parseInt(args[2]);
						Player p = server.getPlayer(args[1]);
						gameAC.setScore(p, score);
					} catch (Exception e){
						sender.sendMessage("" + ChatColor.LIGHT_PURPLE + "Invalid amount.");
					}
				}
			}

			// /AC changescore <Player> number
			else if (arg0.equals("changescore")){
				//Invalid number of args
				if (args.length != 3){
					sender.sendMessage("" + ChatColor.LIGHT_PURPLE + "Usage: /AC changescore <Player> amount");
				} 

				//Valid number of args
				else{
					int score = 0;

					//Attempt to parse int
					try{
						score = Integer.parseInt(args[2]);
						Player p = server.getPlayer(args[1]);
						gameAC.changeScore(p, score);
					} catch (Exception e){
						sender.sendMessage("" + ChatColor.LIGHT_PURPLE + "Invalid amount.");
					}
				}
				return true;
			}

			// /AC resetscore <Player>
			else if (arg0.equals("resetscore")){
				//Invalid number of args
				if (args.length != 2){
					sender.sendMessage("" + ChatColor.LIGHT_PURPLE + "Usage: /AC resetscore <Player>");
				} 

				//Valid number of args
				else{
					Player p = server.getPlayer(args[1]);
					gameAC.resetScore(p);
				}
				return true;
			}

			// /AC loadspawns <name>
			else if (arg0.equals("loadspawns")){

				if(args.length != 2){
					sender.sendMessage(ChatColor.RED + "Usage: /ac loadspawns <name>");
				} 
				else{
					String temp = args[1];
					//If valid number of args, attempt to retrieve the list of spawns
					ArrayList<Location> spawns = GameSpawns.getSpawnList(temp);

					if (spawns == null){
						sender.sendMessage(ChatColor.RED + "No entry found under " + temp);
					}
					else{
						gameAC.receiveSpawnList(temp, spawns);
					}

				}

				return true;
			}

			// /AC removeall
			else if (arg0.equals("removeall")){
				gameAC.removeAllPlayers();
				return true;
			}

			// /AC next
			else if (arg0.equals("next")){
				gameAC.nextRound();
				return true;
			}

			// /AC over
			else if (arg0.equals("over")){
				if (gameAC.getRoundActive())
					gameAC.forceRoundOver();
				return true;
			}

			// /AC info
			else if (arg0.equals("info")){
				gameAC.showInfo();
				return true;
			}

			// /AC stats
			else if (arg0.equals("stats")){
				gameAC.showPlayerStats();
				return true;
			}

			//AC + unknown argument0
			else {
				sender.sendMessage(ChatColor.LIGHT_PURPLE + "Commands: create|removeall|next|set|remove|add|delete|over|info|stats|setscore|changescore|resetscore|loadspawns");
				return true;
			}
		}

		// /delemobs
		else if (cmdName.equals("delmobs")){
			UtilityCommands.deleteMobs(server);
		}

		// /delitems
		else if (cmdName.equals("delitems")){
			UtilityCommands.deleteItems(server);
		}

		// /loadfile <name> <fileName> 
		else if (cmdName.equals("loadfile")){

			if (args.length != 2){
				sender.sendMessage(ChatColor.RED + "Usage: /loadfile <name> <filename>");
			}

			else{
				//If valid number of args, attempt to load the file
				String name = args[0];
				String fileName = args[1];
				GameSpawns.loadFile(name, fileName, sender);
			}

			return true;

		}

		// /createfile <name>
		else if (cmdName.equals("createfile")){
			if (args.length != 1){
				sender.sendMessage(ChatColor.RED + "Usage: /createfile [name].spawn " + ChatColor.BOLD + "*no need to add the .spawn*");
			}
			else{
				boolean success = GameSpawns.createFile(args[0]);
				if (success)
					sender.sendMessage(ChatColor.GREEN + "Successfully created " + GameSpawns.getFileName());
			}
			return true;
		}

		// /writefile [line|loc]
		else if (cmdName.equals("writefile")){
			int length = args.length;
			if (!GameSpawns.hasWriter()){
				sender.sendMessage(ChatColor.RED + "No PrintWriter exists!");
				return true;
			}
			if (length != 1){
				sender.sendMessage(ChatColor.RED + "Usage: /writefile [line|loc]");
			}
			else{
				String arg = args[0];
				if (arg.equals("loc")){
					Location loc = ((Player)sender).getLocation();
					arg = "";
					arg += loc.getX();
					arg += ",";
					arg += loc.getY();
					arg += ",";
					arg += loc.getZ();
					arg += ",";
					arg += loc.getYaw();
					arg += ",";
					arg += loc.getPitch();
				}
				int result = GameSpawns.writeLine(arg);
				if (result == 0)
					sender.sendMessage(ChatColor.GREEN + "Successfully wrote " + ChatColor.WHITE + arg + ChatColor.GREEN + " to " + ChatColor.AQUA + GameSpawns.getFileName());
				else
					sender.sendMessage(ChatColor.RED + "Error writing: " + arg + " to " + ChatColor.AQUA + GameSpawns.getFileName());
			}
			return true;
		}
		
		// /closefile
		else if (cmdName.equals("closefile")){
			boolean success = GameSpawns.closeWriter();
			if (success)
				sender.sendMessage(ChatColor.GREEN + "Successfully saved " + ChatColor.AQUA + GameSpawns.getFileName());
			else
				sender.sendMessage(ChatColor.RED + "Error closing " + ChatColor.AQUA + GameSpawns.getFileName());

		}

//		Bukkit.getWorld("world").setGa
		
		return true;
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent e){
		if(gameAC != null)
			gameAC.shoot(e);
		defaultOnInteract(e);

	}	

	@EventHandler
	public void onEntityDamage(EntityDamageByEntityEvent e){
		if (gameAC != null)
			gameAC.hit(e);
		defaultOnDamage(e);
	}

	/**
	 * Method #1 of 2 methods which enable the FunTypes defined by enum FunTypes if called in OnPlayerInteract
	 * @param e
	 */
	private void defaultOnInteract(PlayerInteractEvent e){
		//Null checks
		if (e == null || e.getItem() == null)
			return;

		Action a = e.getAction();
		Material m = e.getItem().getType();
		Player p = e.getPlayer();

		//Playe right clicks anything
		if (a.equals(Action.RIGHT_CLICK_AIR) || a.equals(Action.RIGHT_CLICK_BLOCK)){
			//Check for valid weapon and shoot projectile
			for (FunType t: FunType.values()){
				if (m.equals(t.getMaterial())){
					Snowball s = p.launchProjectile(Snowball.class);
					s.setVelocity(p.getLocation().getDirection().multiply(speed));
					break;
				}
			}
		}
	}

	/**
	 * Method #2 of 2 methods which enable the FunTypes defined by enum FunTypes if called in OnPlayerInteract
	 * @param e
	 */
	@SuppressWarnings("deprecation")
	private void defaultOnDamage(EntityDamageByEntityEvent e){
		Entity ent = e.getDamager();
		Entity victim = e.getEntity();

		//If the damager is a snowball shot by a player
		if (ent instanceof Snowball && ((Snowball)ent).getShooter() instanceof Player){
			Player shooter = (Player)(((Snowball)ent).getShooter());
			Material m = shooter.getItemInHand().getType();		

			//Find the corresponding FunType
			for (FunType t: FunType.values()){
				//Based on currently held material
				if (m.equals(t.getMaterial())){

					//Set damage of boolay
					e.setDamage(t.getDamage());
					shooter.sendMessage("" + t.getColor() + "Nice shot, " + shooter.getDisplayName() + "!");
					if(victim.isDead()){
						this.getServer().broadcastMessage(shooter.getDisplayName() + " killed " + victim.toString() + " with a " + t.getDamage() + " shot from the " + t);
					}
					break;
				}
			}
		}
	}

	/**
	 * Method run whenever a player dies
	 * @param e
	 */
	@EventHandler
	public void onDeath(PlayerDeathEvent e){
		if (gameAC != null)
			gameAC.death(e);
	}

	@EventHandler
	public void onFoodChange(FoodLevelChangeEvent e){
		if(gameAC != null){
			Player victim = ((Player)(e.getEntity()));
			victim.setFoodLevel(20);
			e.setCancelled(true);

		}
	}

	@EventHandler
	public void onCreatureSpawn(CreatureSpawnEvent e) {
		if(gameAC != null){
			e.setCancelled(true);
		}
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent e){
		Player quitter = e.getPlayer();
		if (gameAC != null && gameAC.contains(quitter)){
			gameAC.removePlayer(quitter);
		}
	}

	@EventHandler
	public void onPlayerRespawn(PlayerRespawnEvent e) {
		Player p = e.getPlayer();
		if (gameAC != null && gameAC.contains(p)){
			e.setRespawnLocation(gameAC.getSpawnDirection());
		}
	}
}