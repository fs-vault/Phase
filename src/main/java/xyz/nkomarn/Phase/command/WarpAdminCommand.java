package xyz.nkomarn.Phase.command;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import xyz.nkomarn.Kerosene.data.LocalStorage;
import xyz.nkomarn.Phase.Phase;
import xyz.nkomarn.Phase.type.Warp;
import xyz.nkomarn.Phase.util.Config;
import xyz.nkomarn.Phase.util.Search;
import xyz.nkomarn.Phase.util.WarpUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

public class WarpAdminCommand implements TabExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (!sender.hasPermission("phase.admin")) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', String.format(
                    "%sInsufficient permissions.", Config.getPrefix()
            )));
        } else if (args.length < 1) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', String.format(
                    "%sSpecify an operation.", Config.getPrefix()
            )));
        } else {
            Bukkit.getScheduler().runTaskAsynchronously(Phase.getPhase(), () -> {
                switch (args[0].toLowerCase()) {
                    case "info":
                        if (args.length < 2) {
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', String.format(
                                    "%sSpecify a warp to view info for.", Config.getPrefix()
                            )));
                        } else {
                            final String warpName = WarpUtil.argsToString(Arrays.copyOfRange(args, 1, args.length));

                            final Warp warp = Search.getWarpByName(warpName);
                            if (warp == null) {
                                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', String.format(
                                        "%sA warp by that name does not exist.", Config.getPrefix()
                                )));
                            } else {
                                StringBuilder information = new StringBuilder();
                                information.append(String.format("%sInfo for warp '%s':\n", Config.getPrefix(), warp.getName()));
                                final OfflinePlayer owner = Bukkit.getOfflinePlayer(warp.getOwnerUUID());
                                information.append(String.format(ChatColor.GRAY + " Owner: &e%s&7 (%s)\n", owner.getName(), warp.getOwnerUUID().toString()));
                                final Location warpLocation = warp.getLocation();
                                information.append(String.format(ChatColor.GRAY + " X: &e%s&7, Y: &e%s&7, Z: &e%s&7, " +
                                                "pitch: &e%s&7, yaw: &e%s&7\n",
                                        (int) warpLocation.getX(), (int) warpLocation.getY(), (int) warpLocation.getZ(),
                                        (int) warpLocation.getPitch(), (int) warpLocation.getYaw()));
                                information.append(String.format(ChatColor.GRAY + " Visits: &e%s&7\n", warp.getVisits()));
                                information.append(String.format(ChatColor.GRAY + " Featured: &e%s&7\n", warp.isFeatured()));
                                information.append(String.format(ChatColor.GRAY + " Expired: &e%s&7\n", warp.isExpired()));
                                information.append(String.format(ChatColor.GRAY + " Last renewed: &e%s\n", warp.getRenewedTime()));
                                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', information.toString()));
                            }
                        }
                        break;
                    case "feature":
                        if (args.length < 2) {
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', String.format(
                                    "%sSpecify the warp for which to toggle featured status.", Config.getPrefix()
                            )));
                        } else {
                            final String warpName = WarpUtil.argsToString(Arrays.copyOfRange(args, 1, args.length));

                            try (Connection connection = LocalStorage.getConnection()) {
                                try (PreparedStatement statement = connection.prepareStatement("UPDATE `warps` SET `featured` = NOT " +
                                        "featured WHERE name LIKE ?;")) {
                                    statement.setString(1, warpName);
                                    statement.executeUpdate();

                                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', String.format("%sToggled " +
                                            "featured status.", Config.getPrefix())));
                                }
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
                        }
                        break;
                    case "delete":
                        if (args.length < 2) {
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', String.format(
                                    "%sSpecify a warp to delete.", Config.getPrefix()
                            )));
                        } else {
                            final String warpName = WarpUtil.argsToString(Arrays.copyOfRange(args, 1, args.length));

                            try (Connection connection = LocalStorage.getConnection()) {
                                try (PreparedStatement statement = connection.prepareStatement("DELETE FROM warps WHERE name LIKE ?;")) {
                                    statement.setString(1, warpName);
                                    statement.execute();

                                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', String.format("%sDeleted " +
                                            "the warp.", Config.getPrefix())));
                                }
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
                        }
                        break;
                    default:
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', String.format(
                                "%sUnknown operation.", Config.getPrefix()
                        )));
                        break;
                }
            });
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String s, String[] args) {
        if (!sender.hasPermission("phase.admin")) return null;
        if (args.length != 1) return null;
        return Arrays.asList("delete", "feature", "info");
    }
}
