package net.novauniverse.games.manhunt.v1;

import java.lang.reflect.InvocationTargetException;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Entity;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import net.novauniverse.games.manhunt.v1.game.Manhunt;
import net.novauniverse.games.manhunt.v1.game.item.TrackerItem;
import net.novauniverse.games.manhunt.v1.game.team.ManhuntRole;
import net.novauniverse.games.manhunt.v1.game.team.ManhuntTeam;
import net.novauniverse.games.manhunt.v1.game.tracker.ManhuntTracker;
import net.novauniverse.games.manhunt.v1.teammanager.EmptyTeamManager;
import net.zeeraa.novacore.commons.log.Log;
import net.zeeraa.novacore.spigot.NovaCore;
import net.zeeraa.novacore.spigot.module.ModuleManager;
import net.zeeraa.novacore.spigot.module.modules.compass.CompassTracker;
import net.zeeraa.novacore.spigot.module.modules.customitems.CustomItemManager;
import net.zeeraa.novacore.spigot.module.modules.game.GameManager;
import net.zeeraa.novacore.spigot.module.modules.game.elimination.PlayerEliminationReason;
import net.zeeraa.novacore.spigot.module.modules.game.messages.PlayerEliminationMessage;
import net.zeeraa.novacore.spigot.module.modules.gamelobby.GameLobby;
import net.zeeraa.novacore.spigot.teams.Team;
import net.zeeraa.novacore.spigot.teams.TeamManager;

public class NovaManhunt extends JavaPlugin implements Listener {
	private static NovaManhunt instance;
	private Manhunt game;

	private boolean useGameRuleRespawning = false;

	public static NovaManhunt getInstance() {
		return instance;
	}

	public Manhunt getGame() {
		return game;
	}
	
	public boolean isUseGameRuleRespawning() {
		return useGameRuleRespawning;
	}

	@Override
	public void onEnable() {
		NovaManhunt.instance = this;

		Log.info(getName(), ChatColor.AQUA + "This gamemode uses the normal worlds instead if creating separate worlds for the game. Do not disable nether, end or the normal world generation");

		game = new Manhunt(Bukkit.getServer().getWorld("world"));

		Bukkit.getServer().getPluginManager().registerEvents(this, this);

		ModuleManager.require(CompassTracker.class);
		ModuleManager.require(GameManager.class);
		ModuleManager.require(GameLobby.class);
		ModuleManager.require(CustomItemManager.class);

		GameLobby.getInstance().setIgnoreNoTeam(true);

		NovaCore.getInstance().setTeamManager(new EmptyTeamManager());

		GameManager.getInstance().setUseTeams(true);
		GameManager.getInstance().setAutoRespawn(false);

		Log.debug(ChatColor.GREEN + "Server package name: " + Bukkit.getServer().getClass().getPackage().getName());
		if (Bukkit.getServer().getClass().getPackage().getName().contains("1_16")) {
			useGameRuleRespawning = true;
			Log.info("NovaManhunt", ChatColor.GREEN + "Using gamerules instead of the spigot api for respawning");
		}

		try {
			CustomItemManager.getInstance().addCustomItem(TrackerItem.class);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
		}

		if (!TeamManager.hasTeamManager()) {
			Log.fatal(getName(), "Manhunt requires the TeamManager to be enabled");
			Bukkit.getServer().shutdown();
			return;
		}

		for (ManhuntRole role : ManhuntRole.values()) {
			TeamManager.getTeamManager().getTeams().add(new ManhuntTeam(role));
		}

		GameManager.getInstance().loadGame(game);
		CompassTracker.getInstance().setCompassTrackerTarget(new ManhuntTracker());
		CompassTracker.getInstance().setStrictMode(true);

		GameManager.getInstance().setPlayerEliminationMessage(new PlayerEliminationMessage() {
			@Override
			public void showPlayerEliminatedMessage(OfflinePlayer player, Entity killer, PlayerEliminationReason reason, int placement) {
				// Nothing here
			}
		});
	}

	@Override
	public void onDisable() {
		HandlerList.unregisterAll((Plugin) this);
		Bukkit.getScheduler().cancelTasks(this);
	}

	public static ManhuntTeam getTeam(ManhuntRole role) {
		for (Team team : TeamManager.getTeamManager().getTeams()) {
			if (((ManhuntTeam) team).getRole() == role) {
				return (ManhuntTeam) team;
			}
		}
		return null;
	}
}