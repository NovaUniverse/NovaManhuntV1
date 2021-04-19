package net.novauniverse.games.manhunt.v1.game.item;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;

import net.novauniverse.games.manhunt.v1.game.team.ManhuntRole;
import net.novauniverse.games.manhunt.v1.game.team.ManhuntTeam;
import net.zeeraa.novacore.spigot.module.modules.customitems.CustomItem;
import net.zeeraa.novacore.spigot.teams.TeamManager;
import net.zeeraa.novacore.spigot.utils.ItemBuilder;

public class TrackerItem extends CustomItem {
	@Override
	public void onPlayerDropItem(PlayerDropItemEvent event) {
		if (TeamManager.getTeamManager().getPlayerTeam(event.getPlayer()) != null) {
			if (((ManhuntTeam) TeamManager.getTeamManager().getPlayerTeam(event.getPlayer())).getRole() == ManhuntRole.HUNTER) {
				event.setCancelled(true);
			}
		}
	}

	@Override
	protected ItemStack createItemStack(Player player) {
		return new ItemBuilder(Material.COMPASS).setName(ChatColor.GOLD + "" + ChatColor.BOLD + "Player tracker").build();
	}
}