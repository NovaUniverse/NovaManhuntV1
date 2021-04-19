package net.novauniverse.games.manhunt.v1.game.team;

import org.bukkit.ChatColor;

public enum ManhuntRole {
	HUNTER("Hunter", ChatColor.RED), SPEEDRUNNER("Speedrunner", ChatColor.GREEN);

	private String displayName;
	private ChatColor color;

	private ManhuntRole(String displayName, ChatColor color) {
		this.displayName = displayName;
		this.color = color;
	}

	public String getDisplayName() {
		return displayName;
	}

	public ChatColor getColor() {
		return color;
	}
}