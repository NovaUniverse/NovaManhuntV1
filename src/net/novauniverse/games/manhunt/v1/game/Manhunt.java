package net.novauniverse.games.manhunt.v1.game;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import net.novauniverse.games.manhunt.v1.NovaManhunt;
import net.novauniverse.games.manhunt.v1.game.event.ManhuntGameEndEvent;
import net.novauniverse.games.manhunt.v1.game.event.ManhuntGameStartEvent;
import net.novauniverse.games.manhunt.v1.game.item.TrackerItem;
import net.novauniverse.games.manhunt.v1.game.team.ManhuntRole;
import net.novauniverse.games.manhunt.v1.game.team.ManhuntTeam;
import net.zeeraa.novacore.commons.log.Log;
import net.zeeraa.novacore.commons.tasks.Task;
import net.zeeraa.novacore.commons.utils.ListUtils;
import net.zeeraa.novacore.spigot.abstraction.VersionIndependentUtils;
import net.zeeraa.novacore.spigot.abstraction.enums.VersionIndependentSound;
import net.zeeraa.novacore.spigot.gameengine.module.modules.game.Game;
import net.zeeraa.novacore.spigot.gameengine.module.modules.game.GameEndReason;
import net.zeeraa.novacore.spigot.gameengine.module.modules.game.elimination.PlayerQuitEliminationAction;
import net.zeeraa.novacore.spigot.module.modules.customitems.CustomItemManager;
import net.zeeraa.novacore.spigot.module.modules.scoreboard.NetherBoardScoreboard;
import net.zeeraa.novacore.spigot.tasks.SimpleTask;
import net.zeeraa.novacore.spigot.teams.TeamManager;
import net.zeeraa.novacore.spigot.utils.ItemBuilder;
import net.zeeraa.novacore.spigot.utils.PlayerUtils;

public class Manhunt extends Game implements Listener {
	private boolean started;
	private boolean ended;

	private World overworld;

	private Task checkTask;

	private ManhuntRole winner;

	public Manhunt(World overworld) {
		super(NovaManhunt.getInstance());
		this.overworld = overworld;
	}

	@Override
	public String getName() {
		return "manhunt";
	}

	@Override
	public String getDisplayName() {
		return "Manhunt";
	}

	@Override
	public PlayerQuitEliminationAction getPlayerQuitEliminationAction() {
		return PlayerQuitEliminationAction.DELAYED;
	}

	@Override
	public boolean eliminatePlayerOnDeath(Player player) {
		ManhuntTeam team = (ManhuntTeam) TeamManager.getTeamManager().getPlayerTeam(player);

		if (team != null) {
			if (team.getRole() == ManhuntRole.SPEEDRUNNER) {
				return true;
			}
		}

		return false;
	}

	@Override
	public boolean isPVPEnabled() {
		return true;
	}

	@Override
	public boolean autoEndGame() {
		return true;
	}

	@Override
	public boolean hasStarted() {
		return started;
	}

	@Override
	public boolean hasEnded() {
		return ended;
	}

	@Override
	public boolean isFriendlyFireAllowed() {
		return false;
	}

	@Override
	public boolean canAttack(LivingEntity attacker, LivingEntity target) {
		return true;
	}

	@Override
	public void onLoad() {
		this.started = false;
		this.ended = false;
		this.checkTask = null;
	}

	@Override
	public void onStart() {
		if (hasStarted()) {
			return;
		}

		winner = ManhuntRole.HUNTER;

		started = true;

		Log.debug("Manhunt", "Player count is " + players.size());
		for (UUID uuid : players) {
			Log.debug("Manhunt", "Has player: " + uuid.toString());
		}

		if (players.size() == 0) {
			Log.error("Manhunt", "Cant start game with no players");
			endGame(GameEndReason.NO_PLAYERS_LEFT);
			return;
		}

		List<UUID> allPlayers = ListUtils.cloneList(players);
		Collections.shuffle(allPlayers);
		UUID speedrunner = allPlayers.get(0);

		for (UUID player : players) {
			ManhuntRole role = player.toString().equalsIgnoreCase(speedrunner.toString()) ? ManhuntRole.SPEEDRUNNER : ManhuntRole.HUNTER;

			ManhuntTeam team = NovaManhunt.getTeam(role);

			if (team.getRole() == ManhuntRole.HUNTER) {
				Player p = Bukkit.getServer().getPlayer(player);
				if (p != null) {
					if (p.isOnline()) {
						p.getInventory().addItem(CustomItemManager.getInstance().getCustomItemStack(TrackerItem.class, p));
					}
				}
			}

			team.addPlayer(player);
		}

		Map<UUID, ManhuntRole> playerRoles = new HashMap<>();

		for (Player player : Bukkit.getServer().getOnlinePlayers()) {
			player.teleport(overworld.getSpawnLocation());

			ManhuntTeam team = (ManhuntTeam) TeamManager.getTeamManager().getPlayerTeam(player);

			NetherBoardScoreboard.getInstance().setPlayerNameColorBungee(player, team.getTeamColor());

			playerRoles.put(player.getUniqueId(), team.getRole());

			switch (team.getRole()) {
			case HUNTER:
				VersionIndependentUtils.get().sendTitle(player, ChatColor.RED + "" + ChatColor.BOLD + "Hunter", ChatColor.RED + "Kill the speedrunner to win", 10, 100, 10);
				break;

			case SPEEDRUNNER:
				VersionIndependentUtils.get().sendTitle(player, ChatColor.GREEN + "" + ChatColor.BOLD + "Speedrunner", ChatColor.GREEN + "Complete the game to win", 10, 100, 10);
				break;

			default:
				break;
			}

			PlayerUtils.resetMaxHealth(player);
			PlayerUtils.fullyHealPlayer(player);
			PlayerUtils.resetPlayerXP(player);
			PlayerUtils.clearPlayerInventory(player);
			PlayerUtils.clearPotionEffects(player);
			player.setSaturation(20);

			if (team.getRole() == ManhuntRole.HUNTER) {
				player.getInventory().addItem(CustomItemManager.getInstance().getCustomItemStack(TrackerItem.class, player));
			}

			if (players.contains(player.getUniqueId())) {
				player.setGameMode(GameMode.SURVIVAL);
			} else {
				player.setGameMode(GameMode.SPECTATOR);
			}
		}

		if (NovaManhunt.getInstance().isUseGameRuleRespawning()) {
			for (World world : Bukkit.getServer().getWorlds()) {
				world.setGameRuleValue("doImmediateRespawn", "true");
			}
		}

		for (World world : Bukkit.getServer().getWorlds()) {
			world.setGameRuleValue("keepInventory", "true");
			world.setTime(1000);
			world.setStorm(false);
		}

		sendBeginEvent();

		ManhuntGameStartEvent e = new ManhuntGameStartEvent(playerRoles);
		Bukkit.getPluginManager().callEvent(e);

		checkTask = new SimpleTask(new Runnable() {
			@Override
			public void run() {
				int hunters = getTeamCount(ManhuntRole.HUNTER);
				int speedrunners = getTeamCount(ManhuntRole.SPEEDRUNNER);

				if (hunters == 0 || speedrunners == 0) {
					// endGame(GameEndReason.WIN);
				}
			}
		}, 1L);
		checkTask.start();
	}

	public int getTeamCount(ManhuntRole role) {
		int count = 0;

		for (UUID player : players) {
			if (((ManhuntTeam) TeamManager.getTeamManager().getPlayerTeam(player)).getRole() == role) {
				count++;
			}
		}

		return count;
	}

	@Override
	public void onEnd(GameEndReason reason) {
		if (hasEnded()) {
			return;
		}

		for (Player p : Bukkit.getServer().getOnlinePlayers()) {
			p.setHealth(p.getMaxHealth());
			p.setFoodLevel(20);
			PlayerUtils.clearPlayerInventory(p);
			PlayerUtils.resetPlayerXP(p);
			p.setGameMode(GameMode.SPECTATOR);
			VersionIndependentUtils.get().playSound(p, p.getLocation(), VersionIndependentSound.WITHER_DEATH, 1F, 1F);
		}

		ManhuntGameEndEvent e = new ManhuntGameEndEvent(winner, reason);
		Bukkit.getServer().getPluginManager().callEvent(e);

		Task.tryStopTask(checkTask);

		ended = true;
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerRespawn(PlayerRespawnEvent e) {
		if (hasStarted()) {
			ManhuntTeam team = (ManhuntTeam) TeamManager.getTeamManager().getPlayerTeam(e.getPlayer());

			if (team != null) {
				if (team.getRole() == ManhuntRole.HUNTER) {
					new BukkitRunnable() {
						@Override
						public void run() {
							e.getPlayer().getInventory().addItem(CustomItemManager.getInstance().getCustomItemStack(TrackerItem.class, e.getPlayer()));
						}
					}.runTaskLater(NovaManhunt.getInstance(), 5L);
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onEntityDeath(EntityDeathEvent e) {
		if (e.getEntity() instanceof EnderDragon) {
			Log.debug("Manhunt", "Enderdragon died");

			ManhuntTeam hunterTeam = NovaManhunt.getTeam(ManhuntRole.HUNTER);

			winner = ManhuntRole.SPEEDRUNNER;

			for (UUID uuid : hunterTeam.getMembers()) {
				players.remove(uuid);
				Player player = Bukkit.getServer().getPlayer(uuid);

				if (player != null) {
					if (player.isOnline()) {
						player.setGameMode(GameMode.SPECTATOR);
					}
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerJoin(PlayerJoinEvent e) {
		if (hasStarted()) {
			if (!players.contains(e.getPlayer().getUniqueId())) {
				e.getPlayer().setGameMode(GameMode.SPECTATOR);
			}
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerRepsawn(PlayerRespawnEvent e) {
		if (hasStarted()) {
			PlayerUtils.clearPlayerInventory(e.getPlayer());
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerDeath(PlayerDeathEvent e) {
		if (hasStarted()) {
			e.setKeepInventory(true);

			for (ItemStack item : e.getEntity().getInventory().getContents()) {
				try {
					if (item == null) {
						continue;
					}

					if (item.getType() == Material.AIR) {
						continue;
					}

					// if (CustomItemManager.getInstance().isType(item, TrackerItem.class)) {
					// continue;
					// }

					if (ItemBuilder.getItemDisplayName(item) != null) {
						if (ItemBuilder.getItemDisplayName(item).contains("Player tracker")) {
							continue;
						}
					}

					e.getEntity().getWorld().dropItem(e.getEntity().getLocation(), item.clone());
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}

			PlayerUtils.clearPlayerInventory(e.getEntity());

			if (!NovaManhunt.getInstance().isUseGameRuleRespawning()) {
				new BukkitRunnable() {
					@Override
					public void run() {
						e.getEntity().spigot().respawn();
					}
				}.runTaskLater(NovaManhunt.getInstance(), 20L);
			}
		}
	}
}