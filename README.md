# ChatEx-Refresh

**A powerful chat management plugin with RGB colors, anti-spam for Minecraft servers.**

## ✨ Features

- **Full Minecraft 1.21+ support** — Folia, Leaf, Paper, Pufferfish, Purpur
- **HEX & Legacy colors** — both `#RRGGBB` and `&#RRGGBB` formats
- **Anti-Spam system** — configurable cooldowns to prevent chat flooding
- **Ad Blocker** — smart detection with IP/domain filtering
- **Range Mode** — local chat with configurable radius
- **Global Mode** — cross-server disabled (original BungeeCord feature removed)
- **35+ languages** — including English, Русский, Українська and more
- **Logging** — chat and ad logs with daily rotation
- **Message formatting** — fully customizable with PlaceholderAPI support

## 🔑 Permissions

| Permission | Default | Description |
|------------|---------|-------------|
| `chatex.allowchat` | true | Allows chatting |
| `chatex.chat.global` | op | Use global chat if ranged mode enabled |
| `chatex.chat.colorlegacy` | op | Use legacy colors (`&c`, `&a`, etc.) |
| `chatex.chat.colorhex` | op | Use HEX colors (`#RRGGBB`, `&#RRGGBB`) |
| `chatex.chat.magic` | op | Use magic color (`&k`) |
| `chatex.antispam.bypass` | op | Bypass the anti-spam system |
| `chatex.bypassads` | op | Bypass the AdBlocker |
| `chatex.notifyad` | op | Receive notification when a player tries to advertise |
| `chatex.notifyupdate` | op | Receive notification about new updates |
| `chatex.clear` | op | Use `/chatex clear` to clear the chat |
| `chatex.reload` | op | Use `/chatex reload` to reload config and locales |

## 📦 Requirements

- **Java 21+**
- **Folia / Leaf / Paper / Pufferfish / Purpur 1.21+**
- **PlaceholderAPI** — optional but recommended
- **LuckPerms** — optional for permission management

## ⚠️ Important Notes

### Server Software Compatibility
This plugin is designed and tested exclusively on **Folia**, **Leaf**, **Paper**, **Pufferfish**, **Purpur**. It may work on other forks, but **compatibility is NOT guaranteed**.

**Not supported:**
- ❌ Any Minecraft Bedrock
- ❌ Cardboard (Spigot-Fabric compatibility layer)
- ❌ Hybrid server software (Arclight, Magma, Mohist and etc)
- ❌ Mod loaders (Fabric, Forge, LegacyFabric, LiteLoader, NeoForge, Quilt and etc)
- ❌ Proxy servers (BungeeCord, Velocity, Waterfall and etc)
- ❌ Spigot, CraftBukkit and any non-Paper forks
- ❌ Vanilla Minecraft Java

## 📁 Commands

| Command | Permission | Description |
|---------|------------|-------------|
| `/chatex` | - | Shows plugin info |
| `/chatex reload` | `chatex.reload` | Reloads config and locales |
| `/chatex clear` | `chatex.clear` | Clears chat for all players |
| `/chatex help` | - | Shows command help |

## 🎨 Color Examples

| Format | Example | Result |
|--------|---------|--------|
| Legacy | `&cHello` | Red text |
| HEX | `&#FF0000Hello` | Red text |
| Alternative HEX | `#FF0000Hello` | Red text |

## 📣 bStats

![bStats](https://bstats.org/signatures/bukkit/ChatEx-Refresh.svg)

## 📄 License
This project is licensed under the **GNU General Public License v2.0** — you are free to use, modify, and distribute it, as long as you keep the same license.

## 💬 Support
If you encounter issues or have questions:
1. Check that you have **Java 21+**
2. Verify your server is running Folia / Leaf / Paper / Pufferfish / Purpur
3. Read the console for error messages

## 🌐 Contacts
- **Telegram:** @MrSpectrumYT
- **Bug reports & feature requests:** Contact via Telegram or Issues Github
