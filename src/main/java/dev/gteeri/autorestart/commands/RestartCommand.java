package dev.gteeri.autorestart.commands;

import dev.gteeri.autorestart.AutoRestart;
import dev.gteeri.autorestart.lang.Messages;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class RestartCommand implements CommandExecutor, TabCompleter {

    private static final List<String> SUBCOMMANDS = List.of("reload", "now", "cancel", "status", "time");
    private final AutoRestart plugin;

    public RestartCommand(AutoRestart plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        Messages messages = plugin.getMessages();

        if (!sender.hasPermission("autorestart.admin")) {
            sender.sendMessage(messages.get("no-permission"));
            return true;
        }

        if (args.length == 0) {
            sendUsage(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload" -> {
                plugin.reload();
                sender.sendMessage(messages.get("reload-success"));
            }
            case "now" -> {
                int seconds = 10;
                if (args.length > 1) {
                    try {
                        seconds = Integer.parseInt(args[1]);
                    } catch (NumberFormatException ignored) {}
                }
                plugin.getScheduler().triggerNow(seconds);
                sender.sendMessage(messages.get("manual-restart", "{player}", sender.getName()));
            }
            case "cancel" -> {
                plugin.getScheduler().cancelCountdown();
                sender.sendMessage(messages.get("cancelled"));
            }
            case "status" -> {
                if (plugin.getScheduler().isCountdownActive()) {
                    String time = plugin.getScheduler().getNextRestartTime() != null
                            ? plugin.getScheduler().getNextRestartTime().format(DateTimeFormatter.ofPattern("HH:mm"))
                            : "N/A";
                    String remaining = plugin.getScheduler().getSecondsRemaining() + "s";
                    sender.sendMessage(messages.get("status-active", "{time}", time, "{remaining}", remaining));
                } else {
                    sender.sendMessage(messages.get("status-inactive"));
                }
            }
            case "time" -> {
                if (plugin.getScheduler().getNextRestartTime() != null) {
                    sender.sendMessage(messages.get("next-restart", "{time}", plugin.getScheduler().getTimeUntilRestart()));
                } else {
                    sender.sendMessage(messages.get("status-inactive"));
                }
            }
            default -> sendUsage(sender);
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        if (!sender.hasPermission("autorestart.admin")) return List.of();

        if (args.length == 1) {
            return SUBCOMMANDS.stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("now")) {
            return List.of("10", "30", "60");
        }

        return List.of();
    }

    private void sendUsage(CommandSender sender) {
        Messages messages = plugin.getMessages();
        sender.sendMessage(messages.format("&c&lAutoRestart &7- Commands:"));
        sender.sendMessage(messages.format("&7/ar reload &8- &fReload configuration"));
        sender.sendMessage(messages.format("&7/ar now [seconds] &8- &fRestart now"));
        sender.sendMessage(messages.format("&7/ar cancel &8- &fCancel active restart"));
        sender.sendMessage(messages.format("&7/ar status &8- &fShow restart status"));
        sender.sendMessage(messages.format("&7/ar time &8- &fTime until next restart"));
    }
}
