package net.novauniverse.games.manhunt.v1.game.event;

import java.util.Map;
import java.util.UUID;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import net.novauniverse.games.manhunt.v1.game.team.ManhuntRole;

public class ManhuntGameStartEvent extends Event {
	private static final HandlerList HANDLERS_LIST = new HandlerList();

	private Map<UUID, ManhuntRole> playerRoles;

	public ManhuntGameStartEvent(Map<UUID, ManhuntRole> playerRoles) {
		this.playerRoles = playerRoles;
	}

	public Map<UUID, ManhuntRole> getPlayerRoles() {
		return playerRoles;
	}

	@Override
	public HandlerList getHandlers() {
		return HANDLERS_LIST;
	}

	public static HandlerList getHandlerList() {
		return HANDLERS_LIST;
	}
}