package net.novauniverse.games.manhunt.v1.game.tracker;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import net.novauniverse.games.manhunt.v1.game.team.ManhuntRole;
import net.novauniverse.games.manhunt.v1.game.team.ManhuntTeam;
import net.zeeraa.novacore.spigot.module.modules.compass.CompassTarget;
import net.zeeraa.novacore.spigot.module.modules.compass.CompassTrackerTarget;
import net.zeeraa.novacore.spigot.module.modules.game.GameManager;
import net.zeeraa.novacore.spigot.teams.TeamManager;

public class ManhuntTracker implements CompassTrackerTarget {
	@Override
	public CompassTarget getCompassTarget(Player player) {
		ManhuntTeam playerTeam = (ManhuntTeam) TeamManager.getTeamManager().getPlayerTeam(player);

		if(playerTeam == null) {
			return null;
		}
		
		if (playerTeam.getRole() != ManhuntRole.HUNTER) {
			return null;
		}

		if (GameManager.getInstance().hasGame()) {
			List<UUID> players = (List<UUID>) new ArrayList<UUID>(GameManager.getInstance().getActiveGame().getPlayers());

			players.remove(player.getUniqueId());

			double closestDistance = Double.MAX_VALUE;
			CompassTarget result = null;

			for (UUID uuid : players) {
				Player p = Bukkit.getServer().getPlayer(uuid);

				if (p.getUniqueId().toString().equalsIgnoreCase(player.getUniqueId().toString())) {
					continue;
				}

				ManhuntTeam team = (ManhuntTeam) TeamManager.getTeamManager().getPlayerTeam(uuid);

				if(team == null) {
					continue;
				}
				
				if (team.getRole() != ManhuntRole.SPEEDRUNNER) {
					continue;
				}

				if (p != null) {
					if (p.isOnline()) {
						if (GameManager.getInstance().hasGame()) {
							if (!GameManager.getInstance().getActiveGame().getPlayers().contains(p.getUniqueId())) {
								continue;
							}
						}

						if (p.getLocation().getWorld() == player.getLocation().getWorld()) {
							double dist = player.getLocation().distance(p.getLocation());

							if (dist < closestDistance) {
								closestDistance = dist;
								result = new CompassTarget(p.getLocation(), "Tracking player " + p.getName());
							}
						}
					}
				}
			}

			return result;
		}
		return null;
	}
}