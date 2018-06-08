package me.MCPFun.reference;

import java.util.List;

import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

/**
 * This class provides useful utility commands that can be referenced from anywhere within this plugin
 * @author Kevin
 *
 */
public class UtilityCommands {

	/**
	 * Deletes all non-player entities on the given server
	 * Warning: Resource heavy & cannot be undone.
	 * @param server
	 */
	public static void deleteMobs(Server server){
		List<World> worlds = server.getWorlds();
		for (World w: worlds){
			List<LivingEntity> ents = w.getLivingEntities();
			for (LivingEntity e: ents){
				if (!(e instanceof Player))
					e.setHealth(0.0);
			}
		}
	}

	/**
	 * Deletes all the dropped items on the given server
	 * Warning: Resource heavy & cannot be undone.
	 * @param server
	 */
	public static void deleteItems(Server server){
		List<World> worlds = server.getWorlds();
		for (World w: worlds){
			List<Entity> ents = w.getEntities();
			for (Entity e: ents){
				if (e instanceof Item)
					e.remove();
			}
		}
	}
}
