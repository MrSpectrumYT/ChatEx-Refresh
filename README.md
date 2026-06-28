# ChatEx-Refresh

**A powerful chat management plugin with RGB colors, anti-spam for Minecraft servers.**

## ✨ Features

- **Full Minecraft 1.21+ support** — Folia, Leaf, Paper, Pufferfish, Purpur, Spigot
- **HEX & Legacy colors** — both `#RRGGBB` and `&#RRGGBB` formats
- **Anti-Spam system** — configurable cooldowns to prevent chat flooding
- **Ad Blocker** — smart detection with IP/domain filtering
- **Range Mode** — local chat with configurable radius
- **Player Mentions** — mention online players with configurable sound
- **Personal Chat Color** — players can set their own chat color via `/color`
- **34 languages** — including English, Русский, Українська and more
- **Logging** — chat and ad logs with daily rotation
- **Message formatting** — fully customizable with PlaceholderAPI support

## 📁 Commands

| Command | Aliases | Permission | Description |
|---------|---------|------------|-------------|
| `/chatex` | `/chat` | - | Shows help for plugin |
| `/chatex help` | `/chat help` | - | Shows command help |
| `/chatex clear` | `/chat clear` | `chatex.clear` | Clears chat for all players |
| `/chatex reload` | `/chat reload` | `chatex.reload` | Reloads config and locales |
| `/color` | `/chatcolor`, `/msgcolor` | `chatex.color` | Set personal chat color |

## 🔑 Permissions

| Permission | Default | Description |
|------------|---------|-------------|
| `chatex.allowchat` | true | Allows chatting |
| `chatex.color` | op | Use `/color` to change color of chat |
| `chatex.chat.global` | op | Use global chat if ranged mode enabled |
| `chatex.chat.colorlegacy` | op | Use legacy colors (`&c`, `&a`, etc.) |
| `chatex.chat.colormodifier` | op | Use formatting codes (`&l`, `&m`, `&n`, `&o`, `&r`) |
| `chatex.chat.colorhex` | op | Use HEX colors (`#RRGGBB`, `&#RRGGBB`) |
| `chatex.chat.colorgradient` | op | Use gradient colors (`/color &#FF0000,#00FF00&l`) |
| `chatex.chat.magic` | op | Use magic color (`&k`) |
| `chatex.mention` | op | Mention other players with sound |
| `chatex.antispam.bypass` | op | Bypass the anti-spam system |
| `chatex.bypassads` | op | Bypass the AdBlocker |
| `chatex.notifyad` | op | Receive notification when a player tries to advertise |
| `chatex.notifyupdate` | op | Receive notification about new updates |
| `chatex.clear` | op | Use `/chatex clear` to clear the chat |
| `chatex.reload` | op | Use `/chatex reload` to reload config and locales |

## 📦 Requirements

- **Java 21+**
- **Folia / Leaf / Paper / Pufferfish / Purpur / Spigot 1.21+**
- **PlaceholderAPI** — optional but recommended
- **LuckPerms** — optional for permission management

## 🎨 Color Examples

| Format | Example | Result |
|--------|---------|--------|
| Legacy | `&cHello` | Red text |
| HEX (with `&`) | `&#FF0000Hello` | Red text |
| HEX (without `&`) | `#FF0000Hello` | Red text |
| Personal Color (Legacy) | `/color &5&l` | Dark purple bold text |
| Personal Color (HEX) | `/color &#E43A96` | Pink text |
| Personal Color (HEX alt) | `/color #E43A96` | Pink text |
| Gradient (2 colors) | `/color #FF0000,#00FF00` | Gradient from red to green |
| Gradient (3+ colors) | `/color #FF0000,#FFAA00,#00FF00` | Gradient with 3 colors |
| Gradient + Modifier | `/color #FF0000,#00FF00&l` | Gradient + bold text |
| Gradient + Mixed Formats | `/color #FF0000,&#00FF00` | Works with both `#` and `&#` |
| Reset Color | `/color reset` | Removes personal color |

> **💡 Note:** Both `#FF0000` and `&#FF0000` formats are fully supported everywhere — in chat, in `/color`, and in config messages. You can freely mix them in gradients: `/color #FF0000,&#00FF00,#0000FF` works perfectly!

## ⚠️ Important Notes

### Server Software Compatibility
This plugin is designed and tested exclusively on **Folia**, **Leaf**, **Paper**, **Pufferfish**, **Purpur** and **Spigot**. It may work on other forks, but **compatibility is NOT guaranteed**.

**Not supported:**
- ❌ Any Minecraft Bedrock
- ❌ Cardboard (Spigot-Fabric compatibility layer)
- ❌ Hybrid server software (Arclight, Magma, Mohist and etc)
- ❌ Mod loaders (Fabric, Forge, LegacyFabric, LiteLoader, NeoForge, Quilt and etc)
- ❌ Proxy servers (BungeeCord, Velocity, Waterfall and etc)
- ❌ Vanilla Minecraft Java

## 📣 bStats

![bStats](https://bstats.org/signatures/bukkit/ChatEx-Refresh.svg)

## 📄 License
This project is licensed under the **GNU General Public License v2.0** — you are free to use, modify, and distribute it, as long as you keep the same license.

## 💬 Support
If you encounter issues or have questions:
1. Check that you have **Java 21+**
2. Verify your server is running Folia / Leaf / Paper / Pufferfish / Purpur / Spigot
3. Read the console for error messages

## 🌐 Contacts
- **Telegram:** @MrSpectrumYT
- **Bug reports & feature requests:** Contact via Telegram or Issues GitHub
