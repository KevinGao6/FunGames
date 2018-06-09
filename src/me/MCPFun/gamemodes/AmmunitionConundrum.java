package me.MCPFun.gamemodes;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
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
	 * Points for specific AC Game events
	 */
	private static final int PTS_PER_KILL = 1;
	private static final int PTS_PER_DEATH = -0;
	private static final int PTS_PER_1ST_PLACE = 2;
	private static final int PTS_LOST_PER_SELFKILL = 1;
//	private static final int PTS_PER_2ND_PLACE = 1;

	
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
	 * Scoreboard objectives
	 */
	private Objective playerScore;
	private Objective playerKills;
	private Objective playerDeaths;
	private Objective roundsWon;
	
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
		server.broadcastMessage("" + ChatColor.GOLD + "Disabling Mob spawning...");
		server.broadcastMessage("" + ChatColor.GOLD + "Locking Players' foodLevel at 20...");
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
	public void addPlayer(Player p){
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

	}

	/**
	 * This method checks itself to see if the player is a participant
	 * @param p the player to remove
	 * @return whether or not removal was successful
	 */
	public boolean removePlayer(Player p){

		if (p == null){
			this.tellModerator("" + ChatColor.LIGHT_PURPLE + ChatColor.BOLD + "Could not find Player.");
			return false;
		}

		if (players.contains(p)){
			this.tellModerator(p.getDisplayName() + " was removed from the game.");
			return players.remove(p);
		}

		this.tellModerator("Invalid player to remove.");
		return false;
	}

	/**
	 * Removes all the current players from the game
	 * Keeps the current moderator
	 */
	public void removeAllPlayers(){
		players = new ArrayList<Player>();

		this.tellModerator("All players removed.");
	}

	/**
	 * @return whether or not a round is active
	 */
	public boolean getRoundActive(){
		return roundActive;
	}

	/**
	 * Activates the next round in this Ammunition Conundrum game
	 */
	public void nextRound(){

		if(roundActive){
			this.tellModerator("Cannot start new round - A round is currently active");
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
			p.sendMessage("" + ChatColor.GOLD + "Firing your stick might discharge 1 functional shot, or a devastating misfire.");
		}

		for (Player p: specials){
			p.sendMessage("" + ChatColor.GOLD + ChatColor.BOLD + "Your role is unknown this round!");
			p.sendMessage("" + ChatColor.GOLD + "You could be the funman or a normal person.");
			p.sendMessage("" + ChatColor.GOLD + "Firing your stick might discharge 1 functional shot, or a devastating misfire.");
		}
		server.broadcastMessage("" + ChatColor.GOLD + ChatColor.BOLD + "New Round Started!");
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

			//TODO: Teleport people to corresponding places
			curPlayers.add(p);
			alives.add(p);
		}

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
	@SuppressWarnings("deprecation")
	private void makeScoreboard(){
		this.tellModerator("Making scoreboard...");
		ScoreboardManager manager = Bukkit.getScoreboardManager();
		Scoreboard board = manager.getNewScoreboard();

		playerScore = board.registerNewObjective("playerScore", "dummy");
		playerKills = board.registerNewObjective("playerKills", "dummy");
		playerDeaths = board.registerNewObjective("playerDeaths", "dummy");
		roundsWon = board.registerNewObjective("roundsWon", "dummy");

		Objective[] objectives = new Objective[4];
		objectives[0] = playerScore;
		objectives[1] = playerKills;
		objectives[2] = playerDeaths;
		objectives[3] = roundsWon;

		playerScore.setDisplaySlot(DisplaySlot.SIDEBAR);
		playerKills.setDisplaySlot(DisplaySlot.SIDEBAR);
		playerDeaths.setDisplaySlot(DisplaySlot.SIDEBAR);
		roundsWon.setDisplaySlot(DisplaySlot.SIDEBAR);

		playerScore.setDisplayName(ChatColor.GREEN + "Score");
		playerKills.setDisplayName(ChatColor.GREEN + "Kills");
		playerDeaths.setDisplayName(ChatColor.GREEN + "Deaths");
		roundsWon.setDisplayName(ChatColor.GREEN + "Rounds Won");
		
		for (Player p: players){
			//Set scoreboard for each participating player
			p.setScoreboard(board);
			
			//Reset all scores to 0
			for (Objective o: objectives){
				Score score = o.getScore(p);
				score.setScore(0);
			}
		}
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

		if (ent instanceof Player && victim instanceof Player){
			if (deads.contains((Player)(ent)) || deads.contains((Player)(ent))){
				((Player)(ent)).sendMessage("" + ChatColor.DARK_RED + ChatColor.BOLD + "Attacking this player is not allowed.");
				e.setCancelled(true);
			}
		}

		//If the damager is a snowball shot by a player
		if (ent instanceof Snowball && ((Snowball)ent).getShooter() instanceof Player && ((Player)victim) instanceof Player){
			Player shooter = (Player)(((Snowball)ent).getShooter());			
			Player shotted = ((Player)victim);

			if (protecteds.contains(shotted)){
				shooter.damage(1000.0);
				//Give kill to reflector
				addKill(shotted);
				
				selfKill(shooter);
				server.broadcastMessage("" + ChatColor.RED + "The funman shot the reflector and killed themself");
			}

			else{
				shotted.damage(1000.0);
				addKill(shooter);
				server.broadcastMessage("" + ChatColor.RED + "The funman's sole snowball finds a poor victim");
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
		
		//A Kill that is part of this AC Game
		if (alives.contains(p)){

			//If the player was not killed by a player
			if (!(EDE instanceof EntityDamageByEntityEvent)){
				e.setDeathMessage(null);
			}
			
			//Player was killed by another participating player: Give points to killer
			else {
				Entity damager = ((EntityDamageByEntityEvent)(EDE)).getDamager();
				if (damager instanceof Player){
					Player killer = ((Player)(damager));
					addKill(killer);
				}
			}
			
			//Update the settings of the player that just died
			addDeath(p);
			
			deads.add(p);
			alives.remove(p);
			e.getDrops().clear();
			if (alives.size() <= 1)
				roundOver();
		}
	}

	/**
	 * Called at the end of a round
	 */
	public void roundOver(){
		roundActive = false;
		server.broadcastMessage("" + ChatColor.DARK_RED + ChatColor.BOLD + "Round Over!");
		Player winner = null;
		if (alives.size() == 1){
			winner = alives.get(0);
			this.addRoundWon(winner);
			server.broadcastMessage("" + ChatColor.AQUA + ChatColor.BOLD + winner.getDisplayName() + " is the winner!");
		}
		hasBoolay = false;

		for (Player p : players){
			PlayerInventory inv = p.getInventory();
			inv.clear();
			inv.setHelmet(null);
			inv.setChestplate(null);
			inv.setLeggings(null);
			inv.setBoots(null);
		}
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
		p.damage(DEFAULT_MISFIRE_DAMAGE);
		p.setFireTicks(DEFAULT_MISFIRE_LENGTH_TICKS);
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
			this.moderator.sendMessage("" + ChatColor.LIGHT_PURPLE + ChatColor.BOLD + msg);
		}
	}
	
	/**
	 * Adds 1 to the objective playerKills for the target player on the scoreboard
	 * Adds the number of points to the target player's playerScore defined by PTS_PER_WIN
	 * @param p the target player
	 */
	@SuppressWarnings("deprecation")
	private void addKill(Player p){
		Score score = playerKills.getScore(p);
		score.setScore(score.getScore() + 1);
		
		score = playerScore.getScore(p);
		score.setScore(score.getScore() + PTS_PER_KILL);
	}
	
	/**
	 * Adds 1 to the objective playerDeaths for the target player on the scoreboard
	 * Adds the number of points to the target player's playerScore defined by PTS_PER_DEATH
	 * @param p the target player
	 */
	@SuppressWarnings("deprecation")
	private void addDeath(Player p){
		Score score = playerDeaths.getScore(p);
		score.setScore(score.getScore() + 1);
		
		score = playerScore.getScore(p);
		score.setScore(score.getScore() + PTS_PER_DEATH);
	}
	
	/**
	 * Adds 1 to the objective roundsWon for the target player on the scoreboard
	 * Adds the number of points to the target player's playerScore defined by PTS_PER_1ST_PLACE
	 * @param p the target player
	 */
	@SuppressWarnings("deprecation")
	private void addRoundWon(Player p){
		Score score = roundsWon.getScore(p);
		score.setScore(score.getScore() + 1);
		
		score = playerScore.getScore(p);
		score.setScore(score.getScore() + PTS_PER_1ST_PLACE);
	}
	
	/**
	 * Removes 1 from the objective playerKills for the target player on the scoreboard
	 * Removes the number of points to the target player's playerScore defined by PTS_LOST_PER_SELFKILL
	 * @param p the target player
	 */
	@SuppressWarnings("deprecation")
	private void selfKill(Player p){
		p.sendMessage(ChatColor.RED + "-1 Kill and -" + PTS_LOST_PER_SELFKILL + " Score for killing yourself.");
		Score score = playerKills.getScore(p);
		score.setScore(score.getScore() - 1);
		
		score = playerScore.getScore(p);
		score.setScore(score.getScore() - PTS_LOST_PER_SELFKILL);
	}
}
