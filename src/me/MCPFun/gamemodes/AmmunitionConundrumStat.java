package me.MCPFun.gamemodes;

/**
 * This class is associated with individual players within an Ammunition Conundrum Game to track their kills/deaths/etc.
 * @author Kevin
 *
 */
public class AmmunitionConundrumStat {

	private int kills, deaths, roundsWon, selfKills, reflectKills;
	
	public AmmunitionConundrumStat(){
		this.setKills(0);
		this.setDeaths(0);
		this.setRoundsWon(0);
		this.setSelfKills(0);
		this.setReflectKills(0);
	}

	public int getKills() {
		return kills;
	}
	
	public void addKill(){
		this.kills ++;
	}

	public void setKills(int kills) {
		this.kills = kills;
	}

	public int getDeaths() {
		return deaths;
	}
	
	public void addDeath(){
		this.deaths ++;
	}

	public void setDeaths(int deaths) {
		this.deaths = deaths;
	}

	public int getRoundsWon() {
		return roundsWon;
	}
	
	public void addRoundWon(){
		this.roundsWon ++;
	}

	public void setRoundsWon(int roundsWon) {
		this.roundsWon = roundsWon;
	}

	public int getSelfKills() {
		return selfKills;
	}
	
	public void addSelfKill(){
		this.selfKills ++;
	}

	public void setSelfKills(int selfKills) {
		this.selfKills = selfKills;
	}

	public int getReflectKills() {
		return reflectKills;
	}
	
	public void addReflectKill(){
		this.reflectKills ++;
	}

	public void setReflectKills(int reflectKills) {
		this.reflectKills = reflectKills;
	}
	
	public String toString(){
		return "" + this.kills + "kills/" + this.deaths + "/deaths" + this.roundsWon + "wins/" + this.reflectKills + "reflect kills/" + this.selfKills + "self kills";
	}
}
