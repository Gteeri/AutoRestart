package dev.gteeri.autorestart;

import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class RestartScheduler {

    private final AutoRestart plugin;
    private BukkitTask checkTask;
    private BukkitTask countdownTask;
    private LocalTime nextRestartTime;
    private int secondsRemaining;
    private boolean countdownActive;

    public RestartScheduler(AutoRestart plugin) {
        this.plugin = plugin;
    }

    public void start() {
        calculateNextRestart();
        checkTask = Bukkit.getScheduler().runTaskTimer(plugin, this::checkTime, 20L, 20L);
    }

    public void stop() {
        if (checkTask != null) {
            checkTask.cancel();
            checkTask = null;
        }
        cancelCountdown();
    }

    public void cancelCountdown() {
        if (countdownTask != null) {
            countdownTask.cancel();
            countdownTask = null;
        }
        countdownActive = false;
        secondsRemaining = 0;
    }

    public void triggerNow(int seconds) {
        cancelCountdown();
        secondsRemaining = seconds;
        countdownActive = true;
        startCountdown();
    }

    public boolean isCountdownActive() {
        return countdownActive;
    }

    public int getSecondsRemaining() {
        return secondsRemaining;
    }

    public LocalTime getNextRestartTime() {
        return nextRestartTime;
    }

    public String getTimeUntilRestart() {
        if (nextRestartTime == null) return "N/A";
        LocalTime now = LocalTime.now();
        long seconds;
        if (nextRestartTime.isAfter(now)) {
            seconds = Duration.between(now, nextRestartTime).getSeconds();
        } else {
            seconds = Duration.between(now, LocalTime.MAX).getSeconds() + Duration.between(LocalTime.MIN, nextRestartTime).getSeconds() + 1;
        }
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, secs);
    }

    private void calculateNextRestart() {
        List<String> times = plugin.getConfig().getStringList("restart-times");
        LocalTime now = LocalTime.now();
        LocalTime closest = null;
        long minDiff = Long.MAX_VALUE;

        for (String timeStr : times) {
            LocalTime time = LocalTime.parse(timeStr, DateTimeFormatter.ofPattern("HH:mm"));
            long diff = Duration.between(now, time).getSeconds();
            if (diff <= 0) diff += 86400;
            if (diff < minDiff) {
                minDiff = diff;
                closest = time;
            }
        }
        nextRestartTime = closest;
    }

    private void checkTime() {
        if (countdownActive) return;
        if (nextRestartTime == null) return;

        List<Integer> warnings = plugin.getConfig().getIntegerList("warnings");
        int maxWarning = warnings.stream().mapToInt(Integer::intValue).max().orElse(60);

        LocalTime now = LocalTime.now();
        long secondsUntil = Duration.between(now, nextRestartTime).getSeconds();
        if (secondsUntil < 0) secondsUntil += 86400;

        if (secondsUntil <= maxWarning && secondsUntil > 0) {
            if (plugin.getConfig().getBoolean("cancel-on-empty") && Bukkit.getOnlinePlayers().isEmpty()) {
                calculateNextRestart();
                return;
            }
            secondsRemaining = (int) secondsUntil;
            countdownActive = true;
            startCountdown();
        }
    }

    private void startCountdown() {
        List<Integer> warnings = plugin.getConfig().getIntegerList("warnings");

        countdownTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (secondsRemaining <= 0) {
                executeRestart();
                return;
            }

            if (plugin.getConfig().getBoolean("cancel-on-empty") && Bukkit.getOnlinePlayers().isEmpty()) {
                cancelCountdown();
                calculateNextRestart();
                Bukkit.getConsoleSender().sendMessage(
                        plugin.getMessages().format(plugin.getMessages().getRaw("cancelled"))
                );
                return;
            }

            if (warnings.contains(secondsRemaining)) {
                broadcastWarning(secondsRemaining);
            }

            secondsRemaining--;
        }, 0L, 20L);
    }

    private void broadcastWarning(int seconds) {
        String timeFormatted = formatTime(seconds);
        String warningMsg = plugin.getConfig().getString("warning-message", "&cServer restarting in {time}!");

        Bukkit.getOnlinePlayers().forEach(player -> {
            player.sendMessage(plugin.getMessages().format(warningMsg, "{time}", timeFormatted));

            if (plugin.getConfig().getBoolean("title-enabled")) {
                String titleMsg = plugin.getConfig().getString("title-message", "&c&lRESTART");
                String subtitleMsg = plugin.getConfig().getString("subtitle-message", "&7in {time} seconds");

                Title title = Title.title(
                        plugin.getMessages().format(titleMsg, "{time}", timeFormatted),
                        plugin.getMessages().format(subtitleMsg, "{time}", String.valueOf(seconds)),
                        Title.Times.times(Duration.ofMillis(200), Duration.ofSeconds(2), Duration.ofMillis(200))
                );
                player.showTitle(title);
            }

            try {
                String soundName = plugin.getConfig().getString("sound", "BLOCK_NOTE_BLOCK_PLING");
                org.bukkit.Sound bukkitSound = org.bukkit.Sound.valueOf(soundName);
                player.playSound(Sound.sound(
                        bukkitSound.key(),
                        Sound.Source.MASTER,
                        1.0f,
                        1.0f
                ));
            } catch (IllegalArgumentException ignored) {}
        });
    }

    private void executeRestart() {
        cancelCountdown();
        String restartMsg = plugin.getConfig().getString("restart-message", "&c&lServer is restarting now!");

        Bukkit.getOnlinePlayers().forEach(player ->
                player.kick(plugin.getMessages().format(restartMsg))
        );

        Bukkit.getScheduler().runTaskLater(plugin, () -> Bukkit.getServer().restart(), 20L);
    }

    private String formatTime(int seconds) {
        if (seconds >= 60) {
            int minutes = seconds / 60;
            int secs = seconds % 60;
            return secs > 0 ? minutes + "m " + secs + "s" : minutes + "m";
        }
        return seconds + "s";
    }
}
