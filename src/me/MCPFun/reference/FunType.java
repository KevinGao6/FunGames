package me.MCPFun.reference;

import org.bukkit.ChatColor;
import org.bukkit.Material;

/**
 * This class provides the constants for each Fun Type
 * @author Kevin
 *
 */
public enum FunType {
	
//	GOLD (Material.STICK, 100, ChatColor.DARK_PURPLE),
	CLASSIC_FUN (Material.IRON_AXE, 5, ChatColor.AQUA),
	GOLDEN_FUN (Material.DIAMOND_AXE, 1000, ChatColor.DARK_PURPLE);
	
	private final Material material;
	private final double damage;
	private final ChatColor color;
	
	FunType(Material material, double damage, ChatColor color){
		this.material = material;
		this.damage = damage;
		this.color = color;
	}
	
	public Material getMaterial(){
		return this.material;
	}
	
	public Double getDamage(){
		return this.damage;
	}
	
	public ChatColor getColor(){
		return color;
	}
}