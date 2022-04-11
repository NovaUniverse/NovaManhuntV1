package net.novauniverse.games.manhunt.v1.game.team;

import net.md_5.bungee.api.ChatColor;
import net.zeeraa.novacore.spigot.teams.Team;

public class ManhuntTeam extends Team {
	private ManhuntRole role;
	
	public ManhuntTeam(ManhuntRole role) {
		this.role = role;
	}

	@Override
	public ChatColor getTeamColor() {
		return role.getColor();
	}

	@Override
	public String getDisplayName() {
		return role.getDisplayName();
	}

	public ManhuntRole getRole() {
		return role;
	}
}