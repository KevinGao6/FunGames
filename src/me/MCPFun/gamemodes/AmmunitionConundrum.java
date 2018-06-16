package me.MCPFun.gamemodes;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

/**
 * The first, original gamemode of the server
 * @author Kevin
 *
 */
public class AmmunitionConundrum {

	/**
	 * Default armor of each player
	 */
	private static final ItemStack[] DEFAULT_ARMOR = {new ItemStack(Material.IRON_BOOTS, 1), new ItemStack(Material.IRON_LEGGINGS, 1), new ItemStack(Material.IRON_CHESTPLATE, 1), new ItemStack(Material.IRON_HELMET, 1)};

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
	 * Speed of snowballs
	 */
	private static final double speed = 999;

	/**
	 * Instant damage to a normal player that misfires
	 */
	private static final double DEFAULT_MISFIRE_DAMAGE = 4.0;

	/**
	 * Ticks which a normal player is set aflame if he/she misfires
	 */
	private static final int DEFAULT_MISFIRE_LENGTH_TICKS = 10;

	/**
	 * Maximum number of players that can participate in a singular AC Game
	 */
	private static final int MAX_PLAYERS = 4;

	/**
	 * Amount of extra damage a reflector takes from melee attacks
	 */
	private static final double EXTRA_DAMAGE_CONSTANT = 1.25;

	/**
	 * Points for specific AC Game events
	 */
	private static final int PTS_PER_KILL = 1;
	private static final int PTS_PER_REFLECT_KILL = 1;
	private static final int PTS_PER_1ST_PLACE = 2;

	//Should be negative
	//	private static final int PTS_PER_DEATH = 0;
	private static final int PTS_LOST_PER_SELF_KILL = -1;
	private static final int PTS_PER_2ND_PLACE = 1;


	/**
	 * This current server
	 */
	private Server server;

	/**
	 * Treeset of all participating players
	 */
	private ArrayList<Player> players;

	/**
	 * The moderator of the game - can be a participating player
	 */
	private Player moderator;

	/**
	 * The players who are currently alive
	 */
	private ArrayList<Player> alives;

	/**
	 * The players who are dead
	 */
	private ArrayList<Player> deads;

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
	 * Whether or not a round is currently active
	 */
	private boolean roundActive;

	/**
	 * Whether or not the special can fire
	 */
	private boolean hasBoolay;

	/**
	 * Scoreboard manager
	 */
	private ScoreboardManager manager;

	/**
	 * Scoreboard
	 */
	private Scoreboard board;

	/**
	 * Scoreboard objective
	 */
	private Objective objective;

	/**
	 * Hashmap which associates players with their corresponding stats
	 */
	private HashMap<Player, AmmunitionConundrumStat> statMap;

	/**
	 * List of locations that the server can teleport players to on nextRound()
	 */
	private ArrayList<Location> spawnList;

	/**
	 * The designated spawn point for spectators in the AC game
	 */
	private Location spawn;
	
	/**
	 * Default constructor
	 * @param moderator the default moderator
	 */
	public AmmunitionConundrum(Server server, Player moderator){
		this.server = server;
		players = new ArrayList<Player>();
		this.moderator = moderator;
		normals = new ArrayList<Player>();
		specials = new ArrayList<Player>();
		protecteds = new ArrayList<Player>();
		alives = new ArrayList<Player>();
		deads = new ArrayList<Player>();
		roundActive = false;
		hasBoolay = false;
		statMap = new HashMap<Player, AmmunitionConundrumStat>();
		server.broadcastMessage("" + ChatColor.GOLD + "Disabling Mob spawning...");
		server.broadcastMessage("" + ChatColor.GOLD + "Locking Players' foodLevels at 20...");
		this.tellModerator("New AC Game Created.");
		this.makeScoreboard();
	}

	/**
	 * Fast constructor when the moderator already knows all the participating players
	 * @param moderator the default moderator
	 * @param p1 player 1
	 * @param p2 player 2
	 * @param p3 player 3
	 */
	public AmmunitionConundrum(Server server, Player moderator, Player p1, Player p2, Player p3){
		this(server, moderator);
		players.add(p1);
		players.add(p2);
		players.add(p3);
	}

	/**
	 * @param p the new moderator
	 */
	public void setModerator(Player p){
		//Invalid Player to set as moderator
		if (p == null){
			this.tellModerator("Could not find Player.");
			return;
		}

		if (!p.isOp()){
			this.tellModerator("Player not eligble for moderator");
			return;
		}

		this.tellModerator("You are no longer the moderator for this AmmunitionConundrum game.");
		this.moderator = p;
		this.tellModerator("You are the new moderator for this AmmunitionConundrum game.");

	}

	/**
	 * @return the current moderator
	 */
	public Player getModerator(){
		return this.moderator;
	}

	/**
	 * PRECONDITION: No more than MAX_PLAYERS players already in game
	 * @param p the new player
	 */
	@SuppressWarnings("deprecation")
	public void addPlayer(Player p){

		if(roundActive){
			this.tellModerator("Cannot add a new player while a round is active.");
			return;
		}

		if (players.size() >= MAX_PLAYERS){
			this.tellModerator("Too many existing players. Please remove a player before adding another.");
			return;
		}

		if (p == null){
			this.tellModerator("" + ChatColor.LIGHT_PURPLE + ChatColor.BOLD + "Could not find Player.");
			return;
		}

		if (players.contains(p)){
			this.tellModerator("Player already in game.");
			return;
		}

		players.add(p);
		this.tellModerator(p.getDisplayName() + " was added to the group");

		p.setScoreboard(board);
		this.tellModerator("Scoreboard entry for " + p.getDisplayName() + " created.");

		this.tellModerator("Attempting to set all scores to 0 for " + p.getDisplayName());
		Score score = objective.getScore(p);
		score.setScore(999);
		score.setScore(0);

		statMap.put(p, new AmmunitionConundrumStat());
		this.tellModerator("Stat object for " + p.getDisplayName() + " created.");

	}

	/**
	 * This method checks itself to see if the player is a participant
	 * @param p the player to remove
	 * @return whether or not removal was successful
	 */
	@SuppressWarnings("deprecation")
	public boolean removePlayer(Player p){

		if (p == null){
			this.tellModerator("" + ChatColor.LIGHT_PURPLE + ChatColor.BOLD + "Null player.");
			return false;
		}

		if (!players.contains(p)){
			this.tellModerator("Invalid player to remove.");
			return false;
		}

		if (roundActive){
			roundOver();
		}

		statMap.remove(p);
		this.tellModerator("Stat object for " + p.getDisplayName() + " deleted.");

		this.resetScore(p);
		this.tellModerator("Score for " + p.getDisplayName() + " set to 0.");

		p.setScoreboard(manager.getNewScoreboard());
		board.resetScores(p);
		this.tellModerator("Removed " + p.getDisplayName() + " from the scoreboard.");

		this.tellModerator(p.getDisplayName() + " was removed from the game.");
		return players.remove(p);		
	}

	/**
	 * Removes all the current players from the game
	 * Keeps the current moderator
	 */
	public void removeAllPlayers(){

		for (Player p: players){
			removePlayer(p);
		}

		this.tellModerator("All players removed.");
	}

	/**
	 * @return whether or not a round is active
	 */
	public boolean getRoundActive(){
		return roundActive;
	}

	/**
	 * @param p the Player to check
	 * @return true if the player is participating in this AC game, false otherwise
	 */
	public boolean contains(Player p){
		return players.contains(p);
	}

	/**
	 * Activates the next round in this Ammunition Conundrum game
	 */
	public void nextRound(){

		if(roundActive){
			this.tellModerator("Cannot start new round - A round is currently active");
			return;
		}

		for (Player p: players)
			if (p.isDead()){
				this.tellModerator("Not all players have respawned.");
				return;
			}

		generateRoles();
		hasBoolay = true;

		ItemStack DEFAULT_WEAPON = new ItemStack(DEFAULT_MELEE, 1);
		DEFAULT_WEAPON.addEnchantment(Enchantment.DAMAGE_ALL, DEFAULT_SHARPNESS_LEVEL);

		//Default kits for each player
		for (Player p: players){

			//Set players to adventure mode
			p.setGameMode(GameMode.ADVENTURE);

			//Set players to full health
			p.setHealth(20.0);

			//Set player to full food
			p.setFoodLevel(20);

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

		for (Player p: protecteds){
			p.sendMessage("" + ChatColor.GOLD + ChatColor.BOLD + "You are the reflector this round!");
			p.sendMessage("" + ChatColor.GOLD + "Your stick is for disguising purposes only.");
			p.sendMessage("" + ChatColor.GOLD + "You can reflect the funman's bullet but you will take extra damage from melee attacks.");
		}

		for (Player p: normals){
			p.sendMessage("" + ChatColor.GOLD + ChatColor.BOLD + "Your role is unknown this round!");
			p.sendMessage("" + ChatColor.GOLD + "You could be the funman or a normal person.");
			p.sendMessage("" + ChatColor.GOLD + "Firing your stick might lead to 1 successful shot, or a devastating misfire.");
		}

		for (Player p: specials){
			p.sendMessage("" + ChatColor.GOLD + ChatColor.BOLD + "Your role is unknown this round!");
			p.sendMessage("" + ChatColor.GOLD + "You could be the funman or a normal person.");
			p.sendMessage("" + ChatColor.GOLD + "Firing your stick might lead to 1 successful shot, or a devastating misfire.");
		}
		server.broadcastMessage("" + ChatColor.DARK_PURPLE + ChatColor.BOLD + "New Round Started!");
		roundActive = true;
	}

	/**
	 * Randomly generate roles for each participating player
	 */
	private void generateRoles(){
		normals = new ArrayList<Player>();
		specials = new ArrayList<Player>();
		protecteds = new ArrayList<Player>();
		deads = new ArrayList<Player>();
		alives = new ArrayList<Player>();

		ArrayList<Player> curPlayers = new ArrayList<Player>(players.size());
		for (Player p: players){
			curPlayers.add(p);
			alives.add(p);
		}

		teleportPlayers();

		int ran = 0;
		//One Special
		if (players.size() > 0){
			ran = (int)(Math.random()*curPlayers.size());
			specials.add(curPlayers.remove(ran));
		}

		//Deflector only if at least 3 players
		if (players.size() > 2){
			ran = (int)(Math.random()*curPlayers.size());
			protecteds.add(curPlayers.remove(ran));
		}
		else{
			this.tellModerator("Play with 3 or more to get optimal experience.");
		}

		//The rest of the players are all normals
		for (Player p: curPlayers){
			normals.add(p);
		}

		System.out.println(ChatColor.RED + "Specials are: " + specials);
		System.out.println(ChatColor.YELLOW + "Protecteds are: " + protecteds);
		System.out.println(ChatColor.GREEN + "Normals are: " + normals);
	}

	/**
	 * Creates a scoreboard for this gamemode
	 */
	private void makeScoreboard(){
		this.tellModerator("Making scoreboard...");
		manager = Bukkit.getScoreboardManager();
		board = manager.getNewScoreboard();

		objective = board.registerNewObjective("objective", "dummy");

		objective.setDisplaySlot(DisplaySlot.SIDEBAR);

		objective.setDisplayName(ChatColor.GREEN + "Leaderboard");
	}

	/**
	 * Called when a player in an AC Game interacts with anything
	 */
	public void shoot(PlayerInteractEvent e){

		if(!roundActive)
			return;

		//Null checks
		if (e == null || e.getItem() == null)
			return;

		Action a = e.getAction();
		Material m = e.getItem().getType();
		Player p = e.getPlayer();

		//Player right clicks anything
		if (a.equals(Action.RIGHT_CLICK_AIR) || a.equals(Action.RIGHT_CLICK_BLOCK)){
			//Check for valid weapon
			if (m.equals(DEFAULT_FUN.getType())){

				//Protected: Do nothing
				if (protecteds.contains(p)){
					return;
				}

				//Normals: Set misfire effect
				else if (normals.contains(p)){
					misfire(p);
					return;
				}

				//Specials: Attempt to throw snowball
				else if (specials.contains(p)){
					if (hasBoolay){
						Snowball s = p.launchProjectile(Snowball.class);
						s.setVelocity(p.getLocation().getDirection().multiply(speed));
						hasBoolay = false;
					}

					else{
						p.sendMessage(ChatColor.RED + "No bullets left!");
					}
				}
			}
		}
	}

	@SuppressWarnings("deprecation")
	public void hit(EntityDamageByEntityEvent e){
		if(!roundActive)
			return;

		if (e == null)
			return;

		Entity ent = e.getDamager();
		Entity victim = e.getEntity();

		if (ent == null || victim == null){
			System.out.println("Something is amiss");
			return;
		}

		//Player attacks player
		if (ent instanceof Player && victim instanceof Player){

			//Either player is not alive in the round
			if ((!players.contains(ent) && players.contains(victim)) || deads.contains((Player)(ent)) || deads.contains((Player)(victim))){
				((Player)(ent)).sendMessage("" + ChatColor.DARK_RED + ChatColor.BOLD + "Attacking this player is not allowed.");
				e.setCancelled(true);
			}

			//The attacked player is a reflector
			else if (protecteds.contains((Player)(victim))){
				e.setDamage(e.getDamage() * EXTRA_DAMAGE_CONSTANT);
			}
		}

		//If the damager is a snowball shot by a player
		else if (ent instanceof Snowball && ((Snowball)ent).getShooter() instanceof Player && victim instanceof Player){
			Player shooter = (Player)(((Snowball)ent).getShooter());			
			Player shotted = ((Player)victim);

			if ((!players.contains(shooter) && players.contains(shotted)) || deads.contains(shooter) || deads.contains(shotted)){
				((Player)(shooter)).sendMessage("" + ChatColor.DARK_RED + ChatColor.BOLD + "Attacking this player is not allowed.");
				e.setCancelled(true);
			}

			else if (protecteds.contains(shotted)){
				shooter.setLastDamageCause(null);
				shooter.setHealth(0.0);
				//Give kill to reflector
				statMap.get(shotted).addKill();
				statMap.get(shooter).addSelfKill();

				changeScore(shotted, PTS_PER_REFLECT_KILL);
				this.alert(shotted, "You gained " + PTS_PER_REFLECT_KILL + " points for reflecting the funman's shot back at them.");
				changeScore(shooter, PTS_LOST_PER_SELF_KILL);
				this.alert(shooter, "You lost " + PTS_LOST_PER_SELF_KILL + " points for shooting the reflector.");
			}

			else{
				shotted.setLastDamageCause(null);
				shotted.setHealth(0.0);;
				changeScore(shooter, PTS_PER_KILL);
				this.alert(shooter, "You gained " + PTS_PER_KILL + " points for landing a nice shot.");

				statMap.get(shooter).addKill();
			}
		}
	}

	/**
	 * Called whenever a player dies
	 * @param e the PlayerDeath Event
	 */
	public void death(PlayerDeathEvent e){

		if(!roundActive)
			return;

		Player p = e.getEntity();
		EntityDamageEvent EDE = p.getLastDamageCause();

		//Killed by "nothing" - a snowball
		if (EDE == null){
			e.getDrops().clear();
			return;
		}
		//A Kill that is part of this AC Game
		if (alives.contains(p)){

			e.setDeathMessage(null);

			//If the player was not killed by a player
			if (!(EDE instanceof EntityDamageByEntityEvent)){
			}

			//Player was killed by another participating player: Give points to killer
			else {
				Entity damager = ((EntityDamageByEntityEvent)(EDE)).getDamager();
				if (damager instanceof Player){
					Player killer = ((Player)(damager));
					statMap.get(killer).addKill();
					changeScore(killer, PTS_PER_KILL);
					this.alert(killer, "You gained " + PTS_PER_KILL + " points for neutralizing " + p.getDisplayName() + ".");
				}
			}

			server.broadcastMessage(ChatColor.WHITE + p.getDisplayName() + " died!");

			//Update the settings of the player that just died
			//			changeScore(p, PTS_PER_DEATH);
			//			this.alert(p, "You lost " + PTS_PER_DEATH + " points for dying.");
			statMap.get(p).addDeath();

			deads.add(p);
			alives.remove(p);
			e.getDrops().clear();
			if (alives.size() == 1){
				changeScore(p, PTS_PER_2ND_PLACE);
				this.alert(p, "You gained " + PTS_PER_2ND_PLACE + " points for coming in second.");
			}

			if (alives.size() <= 1)
				roundOver();
		}
	}

	/**
	 * Called at the end of a round
	 */
	public void roundOver(){
		//If there was a winner
		if (alives.size() == 1){
			Player winner = null;
			winner = alives.get(0);
			statMap.get(winner).addRoundWon();
			changeScore(winner, PTS_PER_1ST_PLACE);
			this.alert(winner, "You gained " + PTS_PER_1ST_PLACE + " points for winning the round.");
			server.broadcastMessage("" + ChatColor.AQUA + ChatColor.BOLD + winner.getDisplayName() + " is the winner!");
		}

		roundActive = false;
		server.broadcastMessage("" + ChatColor.DARK_RED + ChatColor.BOLD + "Round Over!");
		hasBoolay = false;

		this.clearAllCurPlayerInventories();
	}

	/**
	 * Called to force the end of a round with no score calculation
	 */
	public void forceRoundOver(){
		roundActive = false;
		server.broadcastMessage("" + ChatColor.DARK_RED + ChatColor.BOLD + "Round Over!");
		hasBoolay = false;

		this.clearAllCurPlayerInventories();
	}

	/**
	 * Prints each participating player's stats, line-by-line
	 */
	public void showPlayerStats(){
		for(Player p: players){
			server.broadcastMessage(p.getDisplayName() + " has " + statMap.get(p));
		}
	}

	/**
	 * This method removes the necessary components to delete this AC game
	 */
	public void deleteGame(){
		this.removeAllPlayers();
		this.forceRoundOver();
	}

	/**
	 * Shows this AC Game's info to the moderator
	 */
	public void showInfo(){
		this.tellModerator("Info for this AC Game:");
		if (this.moderator == null)
			this.tellModerator("Moderator: Console");
		else
			this.tellModerator("Moderator: " + this.moderator.getDisplayName());

		this.tellModerator("Current Players: " + players);
	}

	/**
	 * Damages and sets aflame a given player
	 * @param p player that misfired
	 */
	private void misfire(Player p){
		EntityDamageEvent e = p.getLastDamageCause();
		p.damage(DEFAULT_MISFIRE_DAMAGE);
		p.setFireTicks(DEFAULT_MISFIRE_LENGTH_TICKS);
		p.setLastDamageCause(e);
	}

	/**
	 * Sends a bold, light_purple message directly to the moderator of this AC game
	 * @param msg
	 */
	private void tellModerator(String msg){
		if (moderator == null){
			System.out.println("" + ChatColor.LIGHT_PURPLE + ChatColor.BOLD + msg);
		}
		else {
			this.moderator.sendMessage("" + ChatColor.LIGHT_PURPLE + ChatColor.ITALIC + msg);
		}
	}

	/**
	 * Sets the score of the target player to newScore
	 * @param p the target player
	 * @param newScore the new score
	 */
	@SuppressWarnings("deprecation")
	public void setScore(Player p, int newScore){
		if (p == null)
			return;
		Score score = objective.getScore(p);
		score.setScore(newScore);
	}

	/**
	 * Changes the score of the target player by changeAmount - can be negative
	 * @param p the target player
	 * @param changeAmount the amount by which the score to change
	 */
	@SuppressWarnings("deprecation")
	public void changeScore(Player p, int changeAmount){
		if (p == null)
			return;
		Score score = objective.getScore(p);
		score.setScore(score.getScore() + changeAmount);
	}

	/**
	 * Sets the score of the target player to 0
	 * @param p the target player
	 */
	@SuppressWarnings("deprecation")
	public void resetScore(Player p){
		if (p == null)
			return;
		Score score = objective.getScore(p);
		score.setScore(0);
	}

	/**
	 * Method to standardize the personal alert messages for AC Game Players
	 * @param p the player to alert
	 * @param msg the message to tell the player
	 */
	private void alert(Player p, String msg){
		p.sendMessage(ChatColor.RED + msg);
	}

	/**
	 * Recieves a list of locations which act as the temporary spawn locations during the AC Game
	 * @param locations
	 */
	public void receiveSpawnList(String name, ArrayList<Location> locations){
		if (locations != null){
			if (locations.size() < 1){
				this.tellModerator("This ArrayList of (Location)s is empty.");
				return;
			}
			
			ArrayList<Location> temp = new ArrayList<Location>(locations);
			this.spawn = temp.remove(0);
			this.spawnList = temp;
			
			Bukkit.getWorlds().get(0).setSpawnLocation(spawn.getBlockX(), spawn.getBlockY(), spawn.getBlockZ());

			this.tellModerator("Spectator spawn set to" + spawn.getX() + "," + spawn.getY() + "," + spawn.getZ());
			this.tellModerator(temp.size() + " spawns loaded for " + name);
		}
		else{
			this.tellModerator("Invalid spawnList to load.");
		}
	}
	
	/**
	 * @return the designated spawn point for players participating in the AC Game
	 */
	public Location getSpawnDirection() {
		return this.spawn;
	}

	/**
	 * Teleports the players to the locations designated by spawnList
	 */
	private void teleportPlayers(){
		this.tellModerator("teleport players called.");

		if (spawnList == null){
			this.tellModerator("spawnList not loaded. Aborting random teleporting...");
			return;
		}

		if (spawnList.size() < players.size()){
			this.tellModerator("Not enough unique spawns locations in spawnList for all the participating players. Aborting random teleporting...");
			return;
		}

		ArrayList<Location> temp = new ArrayList<Location>(spawnList);

		for (Player p: players){
			Location newLoc = temp.remove((int)(Math.random()*temp.size()));
			p.teleport(newLoc);
		}

		this.tellModerator("Successfully teleported all players.");
	}

	/**
	 * Clears the inventory and armor of the given player
	 */
	private void clearInventory(Player p){
		PlayerInventory inv = p.getInventory();
		inv.clear();
		inv.setHelmet(null);
		inv.setChestplate(null);
		inv.setLeggings(null);
		inv.setBoots(null);
	}

	/**
	 * Clears the inventory and armor of all participating players
	 */
	private void clearAllCurPlayerInventories(){
		for(Player p: players){
			this.clearInventory(p);
		}
	}
}
