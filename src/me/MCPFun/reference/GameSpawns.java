package me.MCPFun.reference;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;

/**
 * This class is used to load/save set spawn points for given arenas and maps from text files
 * @author Kevin
 *
 */
public class GameSpawns {

	private static HashMap<String, ArrayList<Location>> spawnMap = new HashMap<String, ArrayList<Location>>();
	
	/**
	 * Attempts to load a file containing spawn locations
	 * FORMAT:
	 * x,y,z
	 * x,y,z
	 * IE.
	 * 414,3,423
	 * 455,12,344
	 * @param name the identifier string to be associated with the ArrayList<Location> in spawnMap
	 * @param fileName the name of the file to attempt to load
	 * @param sender the player sending the command
	 */
	public static void loadFile(String name, String fileName, CommandSender sender){
		fileName = "plugins/" + fileName;
		try(BufferedReader br = new BufferedReader(new FileReader(fileName))) {
			
			//Counters and temp variables
			ArrayList<Location> temp = new ArrayList<Location>();
			int totalLines = 0;
			int loadedLines = 0;
			
			//Each individual line
			String line = br.readLine();

			//Seach all lines
			while (line != null) {
				totalLines ++;

				//Attempt to parse location
				try{
					//Get indices of numbers
					int index1 = line.indexOf(',');
					int index2 = line.indexOf(',', index1 + 1);
					
					//Attempt to parse
					double x = Double.parseDouble(line.substring(0, index1));
					double y = Double.parseDouble(line.substring(index1 + 1, index2));
					double z = Double.parseDouble(line.substring(index2 + 1));
					
					//If successful, make location and add new location
					Location newLoc = new Location(Bukkit.getWorlds().get(0),x,y,z);
					temp.add(newLoc);
					loadedLines ++;
				} catch (Exception e){
				}
				
				line = br.readLine();
			}
			
			spawnMap.put(name, temp);
			sender.sendMessage(ChatColor.GREEN + "Successfully loaded " + loadedLines + " of " + totalLines + " lines in " + fileName);
			sender.sendMessage(ChatColor.DARK_GREEN + "Saved ArrayList<Location> as " + name + "; please access with /loadspawns " + name);
		} catch (IOException e) {
			//Errors in reading files
			sender.sendMessage(ChatColor.DARK_RED + "Unable to load " + fileName + "");
			if(spawnMap.containsKey(name))
				spawnMap.remove(name);
		}
	}
	
	/**
	 * @param name the name of the ArrayList<Location> associated with it when loading the file
	 * @return spawnMap.get(name)
	 */
	public static ArrayList<Location> getSpawnList(String name){
		return spawnMap.get(name);
	}
}