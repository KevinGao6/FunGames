package me.MCPFun.main;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import me.MCPFun.gamemodes.AmmunitionConundrum;
import me.MCPFun.reference.FunType;

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

		if (cmdName.equals("AC")){

			if (!sender.isOp()){
				sender.sendMessage("" + ChatColor.LIGHT_PURPLE + ChatColor.BOLD + "You do not have permission to use this command.");
			}

			if (args.length == 0){
				sender.sendMessage(ChatColor.LIGHT_PURPLE + "Commands: create/add/set/remove/removeAll/next/delete");
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
					sender.sendMessage("" + ChatColor.LIGHT_PURPLE + ChatColor.BOLD + "Started the Gamemode");
				} 
				else {
					sender.sendMessage("" + ChatColor.LIGHT_PURPLE + ChatColor.BOLD + "An AC Game already exists. Please delete the previous instance before creating a new one.");
				}
			}

			//Null check for game AC
			else if (gameAC == null){
				sender.sendMessage("" + ChatColor.LIGHT_PURPLE + ChatColor.BOLD + "No AC Game Exists!");
				return true;
			}

			//Moderator Check for game AC
			else if (!(sender instanceof Player) || !sender.equals(gameAC.getModerator())){
				sender.sendMessage("" + ChatColor.LIGHT_PURPLE + ChatColor.BOLD + "You are not the moderator for this AC Game.");
				return true;
			}

			// /AC delete
			else if (arg0.equals("delete")){
				gameAC = null;
				sender.sendMessage("" + ChatColor.LIGHT_PURPLE + ChatColor.BOLD + "AC Game deleted.");

			}

			// /AC set <Player>
			else if (arg0.equals("set")){

				//Invalid Number of args
				if (args.length != 2){
					sender.sendMessage("" + ChatColor.LIGHT_PURPLE + ChatColor.BOLD + "Usage: /AC set <Player>");
				} 

				//Valid Number of args
				else {
					Player p = server.getPlayer(args[1]);
					gameAC.setModerator(p);

				}
			}

			// /AC add <Player>
			else if (arg0.equals("add")){
				//Invalid Number of args
				if (args.length != 2){
					sender.sendMessage("" + ChatColor.LIGHT_PURPLE + ChatColor.BOLD + "Usage: /AC add <Player>");
				} 

				//Valid Number of args
				else {
					Player p = server.getPlayer(args[1]);
					gameAC.addPlayer(p);

				}
			}

			// /AC remove <Player>
			else if (arg0.equals("remove")){
				//Invalid Number of args
				if (args.length != 2){
					sender.sendMessage("" + ChatColor.LIGHT_PURPLE + ChatColor.BOLD + "Usage: /AC remove <Player>");
				} 

				//Valid Number of args
				else {
					Player p = server.getPlayer(args[1]);
					gameAC.removePlayer(p);
				}
			}

			// /AC removeall
			else if (arg0.equals("removeAll")){
				gameAC.removeAllPlayers();
			}

			// /AC next
			else if (arg0.equals("next")){
				gameAC.nextRound();
			}
		}
		return true;
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent e){
		if(gameAC != null)
			gameAC.shoot(e);
		defaultOnInteract(e);

	}	


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

	@EventHandler
	public void onEntityDamage(EntityDamageByEntityEvent e){
		if (gameAC != null)
			gameAC.hit(e);
		defaultOnDamage(e);
	}

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

	@EventHandler
	public void onDeath(PlayerDeathEvent e){
		if (gameAC != null)
			gameAC.death(e);
	}
}