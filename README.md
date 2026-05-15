![Paper](https://img.shields.io/badge/Paper-1.21+-blue?style=flat-square) ![Java](https://img.shields.io/badge/Java-21-orange?style=flat-square) ![License](https://img.shields.io/badge/License-MIT-green?style=flat-square)

![Paper](https://img.shields.io/badge/Paper-1.21+-blue?style=flat-square) ![Java](https://img.shields.io/badge/Java-21-orange?style=flat-square) ![License](https://img.shields.io/badge/License-MIT-green?style=flat-square)

# AutoRestart

Lightweight scheduled server restart system for Paper 1.21+.

## Features

- Scheduled restarts at configurable times
- Countdown warnings with customizable intervals
- Title and subtitle notifications
- Sound alerts before restart
- Cancel restart if server is empty
- Multi-language support (EN, RU)
- Manual restart with custom countdown
- Cancel active restarts
- Full tab completion

## Commands

| Command | Description |
|---------|-------------|
| `/ar reload` | Reload configuration and language files |
| `/ar now [seconds]` | Trigger immediate restart with optional countdown |
| `/ar cancel` | Cancel an active restart countdown |
| `/ar status` | Show current restart status |
| `/ar time` | Show time until next scheduled restart |

**Permission:** `autorestart.admin` (default: op)

## Configuration

```yaml
restart-times:
  - "06:00"
  - "18:00"

warnings:
  - 60
  - 30
  - 10
  - 5
  - 3
  - 2
  - 1

warning-message: "&c&lServer restarting in {time}!"
restart-message: "&c&lServer is restarting now!"

title-enabled: true
title-message: "&c&lRESTART"
subtitle-message: "&7in {time} seconds"

sound: BLOCK_NOTE_BLOCK_PLING
cancel-on-empty: false
language: en
```

## Installation

1. Download the latest release JAR
2. Place it in your server's `plugins/` folder
3. Start or restart the server
4. Edit `plugins/AutoRestart/config.yml` to your needs
5. Run `/ar reload` to apply changes

## Requirements

- Paper 1.21+
- Java 21
