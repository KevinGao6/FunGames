package me.MCPFun.main;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;

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
	private static final String start = "start";
	
	/**
	 * Automatically run when this plugin is enabled
	 */
	@Override
	public void onEnable(){
		System.out.println("Enabling event listener for " + name + "...");
		Bukkit.getServer().getPluginManager().registerEvents(this, this);
		
		System.out.println("Success! " + name + " loaded!");
	}
	
	/**
	 * Automatically run when this plugin is disabled
	 */
	@Override
	public void onDisable(){
		System.out.println("Disabling Plugin " + this.getClass().getName() + "...");
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){

		System.out.println(sender.getClass().getName());
		
		if (cmd.getName().equals(start)){
			sender.sendMessage("" + ChatColor.RED + ChatColor.BOLD + "Started the Gamemode");
		}
		
		
		return true;
	}
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent e){
		
		if (e == null || e.getItem() == null)
			return;
		
		Action a = e.getAction();
		
		//Playe right clicks anything
		if (a.equals(Action.RIGHT_CLICK_AIR) || a.equals(Action.RIGHT_CLICK_BLOCK)){
			
			Material m = e.getItem().getType();
			Player p = e.getPlayer();
			
			for (FunType t: FunType.values()){
				if (m.equals(t.getMaterial())){
					Snowball s = p.launchProjectile(Snowball.class);
					s.setVelocity(p.getLocation().getDirection().multiply(speed));
					break;
				}
			}
		}	
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler
	public void onEntityDamage(EntityDamageByEntityEvent e){
		Entity ent = e.getDamager();
		Entity victim = e.getEntity();
		//TODO: Seperate class for snowball
		if (ent instanceof Snowball && ((Snowball)ent).getShooter() instanceof Player){
			Player shooter = (Player)(((Snowball)ent).getShooter());
			Material m = shooter.getItemInHand().getType();		
			for (FunType t: FunType.values()){
				if (m.equals(t.getMaterial())){
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
}
