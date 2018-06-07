package me.MCPFun.gamemodes;

import java.util.ArrayList;
import java.util.TreeSet;

import org.bukkit.entity.Player;

/**
 * The first, original gamemode of the server
 * @author Kevin
 *
 */
public class AmmunitionConundrum {

	/**
	 * Treeset of all participating players
	 */
	TreeSet<Player> players;

	/**
	 * The moderator of the game - can be a participating player
	 */
	Player moderator;
	
	/**
	 * The players who are the normal class for this round
	 */
	ArrayList<Player> normals;
	
	/**
	 * The players who are the special class for this round
	 */
	ArrayList<Player> specials;
	
	/**
	 * The players who are the protected class for this round
	 */
	ArrayList<Player> protecteds;

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

	public void nextRound(){
		//TODO: Teleport people to corresponding places
		normals = new ArrayList<Player>();
		specials = new ArrayList<Player>();
		protecteds = new ArrayList<Player>();
		
		Player[] curPlayers = new Player[players.size()];
		int i = 0;
		for (Player p: players){
			curPlayers[i] = p;
			i ++;
		}
			
	}
}
