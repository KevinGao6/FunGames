package me.MCPFun.gamemodes;

import java.util.ArrayList;
import java.util.TreeSet;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

/**
 * The first, original gamemode of the server
 * @author Kevin
 *
 */
public class AmmunitionConundrum {

	/**
	 * Treeset of all participating players
	 */
	private TreeSet<Player> players;

	/**
	 * The moderator of the game - can be a participating player
	 */
	private Player moderator;

	/**
	 * The players who are the normal class for this round
	 */
	private ArrayList<Player> normals;

	/**
	 * The players who are the special class for this round
	 */
	private ArrayList<Player> specials;

	/**
	 * The players who are the protected class for this round
	 */
	private ArrayList<Player> protecteds;

	/**
	 * Default armor of each player
	 */
	private static final ItemStack[] DEFAULT_ARMOR = {new ItemStack(Material.IRON_HELMET, 1), new ItemStack(Material.IRON_CHESTPLATE, 1), new ItemStack(Material.IRON_LEGGINGS, 1), new ItemStack(Material.IRON_BOOTS, 1)};
	
	/**
	 * Default weapon of each player
	 */
	private static final Material DEFAULT_MELEE = Material.DIAMOND_SWORD;
	
	/**
	 * Default sharpness level of each DEFAULT_MELEE weapon
	 */
	private static final int DEFAULT_SHARPNESS_LEVEL = 2;
	
	/**
	 * Default Fun
	 */
	private static final ItemStack DEFAULT_FUN = new ItemStack(Material.STICK, 1);
	
	/**
	 * Defualt constructor
	 * @param moderator the default moderator
	 */
	public AmmunitionConundrum(Player moderator){
		players = new TreeSet<Player>();
		this.moderator = moderator;
		normals = new ArrayList<Player>();
		specials = new ArrayList<Player>();
		protecteds = new ArrayList<Player>();
	}

	/**
	 * Fast constructor when the moderator already knows all the participating players
	 * @param moderator the default moderator
	 * @param p1 player 1
	 * @param p2 player 2
	 * @param p3 player 3
	 */
	public AmmunitionConundrum(Player moderator, Player p1, Player p2, Player p3){
		this(moderator);
		players.add(p1);
		players.add(p2);
		players.add(p3);
	}

	/**
	 * @param p the new moderator
	 */
	public void setModerator(Player p){
		this.moderator.sendMessage("You are no longer the moderator for this AmmunitionConundrum game.");
		this.moderator = p;
		this.moderator.sendMessage("You are the new moderator for this AmmunitionConundrum game.");

	}

	/**
	 * @return the current moderator
	 */
	public Player getModerator(){
		return this.moderator;
	}

	/**
	 * PRECONDITION: No more than 3 players already in game
	 * @param p the new player
	 */
	public void addPlayer(Player p){
		if (players.size() < 4){
			players.add(p);
			this.moderator.sendMessage(p.getDisplayName() + " was added to the group");
			return;
		}

		this.moderator.sendMessage("Too many existing players. Please remove a player before adding another.");
	}

	/**
	 * PRECONDITION: Player p is a participant
	 * @param p the player to remove
	 * @return whether or not removal was successful
	 */
	public boolean removePlayer(Player p){
		if (p != null && players.contains(p)){
			this.moderator.sendMessage(p.getDisplayName() + " was removed from the game.");
			return players.remove(p);
		}

		this.moderator.sendMessage("Invalid player to remove.");
		return false;
	}

	/**
	 * Removes all the current players from the game
	 * Keeps the current moderator
	 */
	public void removeAllPlayers(){
		for (Player p: players)
			players.remove(p);

		this.moderator.sendMessage("All players removed.");
	}

	/**
	 * Activates the next round in this Ammunition Conundrum game
	 */
	public void nextRound(){
		generateRoles();

		ItemStack DEFAULT_WEAPON = new ItemStack(DEFAULT_MELEE, 1);
		DEFAULT_WEAPON.addEnchantment(Enchantment.DAMAGE_ALL, DEFAULT_SHARPNESS_LEVEL);
		
		//Default kits for each player
		for (Player p: players){
			
			//Set players to adventure mode
			p.setGameMode(GameMode.ADVENTURE);
			
			PlayerInventory inventory = p.getInventory();
			
			//Clear Inventory
			inventory.clear();
			
			//Give Armor
			inventory.setArmorContents(DEFAULT_ARMOR);
			
			//Give weapon
			inventory.addItem(DEFAULT_WEAPON);
			
			//Give weapon
			inventory.addItem(DEFAULT_FUN);
		}
	}

	/**
	 * Randomly generate roles for each participating player
	 */
	private void generateRoles(){
		normals = new ArrayList<Player>();
		specials = new ArrayList<Player>();
		protecteds = new ArrayList<Player>();

		ArrayList<Player> curPlayers = new ArrayList<Player>(players.size());
		for (Player p: players){
			
			//TODO: Teleport people to corresponding places
			curPlayers.add(p);
		}

		//TODO: Better method of sorting people
		int ran = (int)(Math.random()*curPlayers.size());
		specials.add(curPlayers.remove(ran));

		ran = (int)(Math.random()*curPlayers.size());
		protecteds.add(curPlayers.remove(ran));

		for (Player p: curPlayers){
			normals.add(p);
		}

		System.out.println(ChatColor.RED + "Specials are: " + specials);
		System.out.println(ChatColor.YELLOW + "Protecteds are: " + protecteds);
		System.out.println(ChatColor.GREEN + "Normals are: " + normals);
	}
}
