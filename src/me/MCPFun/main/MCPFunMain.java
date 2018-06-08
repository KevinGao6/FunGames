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

	/**Command to start the randomization process*/
	private static final String create = "create";

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

		System.out.println(sender.getClass().getName());

		//TODO: Cleanup
		String cmdName = cmd.getName();
		
		if (cmdName.equals(create)){
			gameAC = new AmmunitionConundrum(server, (Player)sender);
			sender.sendMessage("" + ChatColor.RED + ChatColor.BOLD + "Started the Gamemode");
		}
		
		else if (gameAC == null){
			sender.sendMessage(ChatColor.RED + "No AC game exists!");
		}
		
		else if (cmdName.equals("set")){
			gameAC.setModerator(server.getPlayer(args[0]));
		}
		
		else if (cmdName.equals("add")){
			gameAC.addPlayer(server.getPlayer(args[0]));
		}
		
		else if (cmdName.equals("remove")){
			gameAC.removePlayer(server.getPlayer(args[0]));
		}
		
		else if (cmdName.equals("removeAll")){
			gameAC.removeAllPlayers();
		}
		
		else if (cmdName.equals("next")){
			gameAC.nextRound();
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
