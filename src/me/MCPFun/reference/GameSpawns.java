package me.MCPFun.reference;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.lang.StringUtils;
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
	private static PrintWriter writer;
	private static String fileName;
	
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
		fileName = "plugins/" + fileName + ".spawn";
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

				int count = StringUtils.countMatches(line, ",");

				if (count == 4){
					//Attempt to parse location
					try{
						//Get indices of numbers
						int index1 = line.indexOf(',');
						int index2 = line.indexOf(',', index1 + 1);
						int index3 = line.indexOf(',', index2 + 1);
						int index4 = line.indexOf(',', index3 + 1);

						//Attempt to parse
						double x = Double.parseDouble(line.substring(0, index1));
						double y = Double.parseDouble(line.substring(index1 + 1, index2));
						double z = Double.parseDouble(line.substring(index2 + 1, index3));
						float yaw = Float.parseFloat(line.substring(index3 + 1, index4));
						float pitch = Float.parseFloat(line.substring(index4 + 1));

						//If successful, make location and add new location
						Location newLoc = new Location(Bukkit.getWorlds().get(0),x,y,z);
						newLoc.setYaw(yaw);
						newLoc.setPitch(pitch);
						temp.add(newLoc);
						loadedLines ++;
						System.out.println("Successfully loaded a yaw/pitch line.");
					} catch (Exception e){
					}
				}
				else{
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
						System.out.println("Successfully loaded a normal line.");
					} catch (Exception e){
					}
				}

				line = br.readLine();
			}
			
			spawnMap.put(name, temp);
			sender.sendMessage(ChatColor.GREEN + "Successfully loaded " + loadedLines + " of " + totalLines + " lines in " + fileName);
			sender.sendMessage(ChatColor.DARK_GREEN + "Saved ArrayList<Location> as " + name + "; please access with /ac loadspawns " + name);
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
	
	/**
	 * Creates a new .spawn file with the give name
	 * Note: .spawn not needed
	 * @return true if successful, false otherwise
	 */
	public static boolean createFile(String name){
		name = "plugins/" + name + ".spawn";
		try {
			writer = new PrintWriter(new File(name), "UTF-8");
			fileName = name;
			return true;
		} catch (Exception e){
			System.out.println("Error initializing PrintWriter for " + name);
			return false;
		}		
	}
	
	/**
	 * Writes the given string to the next line of the current file
	 * -2 writer doesn't exist
	 * -1 error writing line: IO exception
	 * 0 line successfully written
	 */
	public static int writeLine(String line){
		if (writer == null)
			return -2;
		
		try{
			writer.println(line);
			return 0;
		} catch (Exception e){
			System.out.println("Error writing line to PrintWriter.");
			return -1;
		}
	}
	
	/**
	 * Closes the current PrintWritter
	 * @return saved file's name if successful, null otherwise
	 */
	public static String closeWriter(){
		try{
			writer.close();
			writer = null;
			String toReturn = fileName;
			fileName = null;
			return toReturn;
		} catch (Exception e){
			return null;
		}
	}
	
	/**
	 * @return true if there is a writer, false otherwise
	 */
	public static boolean hasWriter(){
		return !(writer == null);
	}
	
	/**
	 * @return the name of the file that the user is currently editing, null if the user is not editing any file;
	 */
	public static String getFileName(){
		return fileName;
	}
}