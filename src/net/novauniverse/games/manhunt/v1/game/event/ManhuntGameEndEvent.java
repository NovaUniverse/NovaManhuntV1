package net.novauniverse.games.manhunt.v1.game.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import net.novauniverse.games.manhunt.v1.game.team.ManhuntRole;
import net.zeeraa.novacore.spigot.gameengine.module.modules.game.GameEndReason;

public class ManhuntGameEndEvent extends Event {
	private static final HandlerList HANDLERS_LIST = new HandlerList();

	private ManhuntRole winner;
	private GameEndReason gameEndReason;

	public ManhuntGameEndEvent(ManhuntRole winner, GameEndReason gameEndReason) {
		this.winner = winner;
		this.gameEndReason = gameEndReason;
	}

	public ManhuntRole getWinner() {
		return winner;
	}
	
	public GameEndReason getGameEndReason() {
		return gameEndReason;
	}

	@Override
	public HandlerList getHandlers() {
		return HANDLERS_LIST;
	}

	public static HandlerList getHandlerList() {
		return HANDLERS_LIST;
	}
}