package net.novauniverse.games.manhunt.v1.teammanager;

import org.bukkit.entity.Player;

import net.zeeraa.novacore.spigot.teams.TeamManager;

public class EmptyTeamManager extends TeamManager {
	@Override
	public boolean requireTeamToJoin(Player player) {
		return false;
	}
}