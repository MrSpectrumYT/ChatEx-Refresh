# ChatEx-Refresh

## ⚠️ WARNING

**This plugin is not ChatEx. It is a fork of the ChatEx plugin. The authors of the original ChatEx — TheJeterLP and Wizard_x. ChatEx repository — https://github.com/TheJeterLP/ChatEx.**

**ChatEx-Refresh — a powerful chat management plugin with MiniMessage, RGB colors, gradients and anti-spam for Minecraft servers.**

## ✨ Features

- **Minecraft 1.16.5+ support** — Spigot, Paper and Paper forks
- **Full MiniMessage support** — colors, HEX, gradients, rainbow, hover, click, fonts, keybind, lang, newline
- **Legacy + HEX + `&#` + `#` + `&x`** — all in a single line, mixed freely
- **Custom MiniMessage parser** — no Adventure dependency, works on Spigot too
- **Gradients** — unlimited colors, modifiers, legacy and HEX mixed
- **Anti-Spam system** — configurable cooldowns to prevent chat flooding
- **Ad Blocker** — smart detection with IP/domain filtering
- **Range Mode** — local chat with configurable radius
- **Player Mentions** — mention online players with configurable sound
- **Personal Chat Color** — players can set their own chat color via `/color`
- **Interactive messages** — click / hover commands and links in help and usage
- **Command Filtering** — apply chat rules to `/msg`, `/me`, and other commands
- **Split MiniMessage permissions** — control every tag separately
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

### General
| Permission | Default | Description |
|------------|---------|-------------|
| `chatex.allowchat` | true | Allows chatting |
| `chatex.color` | op | Use `/color` to change color of chat |
| `chatex.clear` | op | Use `/chatex clear` to clear the chat |
| `chatex.reload` | op | Use `/chatex reload` to reload config and locales |
| `chatex.chat.global` | op | Use global chat if ranged mode enabled |
| `chatex.mention` | op | Mention other players with sound |
| `chatex.antispam.bypass` | op | Bypass the anti-spam system |
| `chatex.bypassads` | op | Bypass the AdBlocker |
| `chatex.notifyad` | op | Receive notification when a player tries to advertise |
| `chatex.notifyupdate` | op | Receive notification about new updates |

### Legacy colors
| Permission | Default | Description |
|------------|---------|-------------|
| `chatex.chat.colorlegacy` | op | Use legacy colors (`&c`, `&a`, ...) |
| `chatex.chat.colormodifier` | op | Use formatting codes (`&l`, `&m`, `&n`, `&o`, `&r`) |
| `chatex.chat.colorhex` | op | Use HEX colors (`#RRGGBB`, `&#RRGGBB`) |
| `chatex.chat.colormagic` | op | Use magic color (`&k`) |
| `chatex.chat.colorgradient` | op | Use gradient colors (`/color #FF0000,#00FF00&l`) |

### MiniMessage (split per tag)
| Permission | Default | Description |
|------------|---------|-------------|
| `chatex.chat.minimessagelegacy` | op | Named colors (`<red>`, `<gold>`, ...) |
| `chatex.chat.minimessagemodifier` | op | Formatting (`<bold>`, `<italic>`, `<underlined>`, `<strikethrough>`, `<reset>`) |
| `chatex.chat.minimessagehex` | op | HEX colors (`<#FFAA00>`, `<color:#FFAA00>`) |
| `chatex.chat.minimessagemagic` | op | `<obfuscated>` |
| `chatex.chat.minimessagegradient` | op | `<gradient:...>` |
| `chatex.chat.minimessagerainbow` | op | `<rainbow>` |
| `chatex.chat.minimessageurl` | op | `<click:open_url:...>` |
| `chatex.chat.minimessagecommand` | op | `<click:run_command:...>` |
| `chatex.chat.minimessagesuggest` | op | `<click:suggest_command:...>` |
| `chatex.chat.minimessagecopy` | op | `<click:copy_to_clipboard:...>` |
| `chatex.chat.minimessagehover` | op | `<hover:show_text:'...'>` |
| `chatex.chat.minimessageinsertion` | op | `<insertion:...>` |
| `chatex.chat.minimessagekeybind` | op | `<key:key.jump>` |
| `chatex.chat.minimessagefont` | op | `<font:minecraft:uniform>` |
| `chatex.chat.minimessagelang` | op | `<lang:block.minecraft.stone>` |
| `chatex.chat.minimessagenewline` | op | `<newline>`, `<br>` |

### Umbrella permissions
| Permission | Default | Description |
|------------|---------|-------------|
| `chatex.chat.color` | op | All legacy color permissions |
| `chatex.chat.minimessage` | op | All MiniMessage permissions |

> **💡 Note:** `chatex.color` is a standalone permission for the `/color` command — it does **not** grant any MiniMessage or legacy color permissions.

## 📦 Requirements

- **Java 17+**
- **Spigot, Paper and Paper forks 1.16.5+**
- **PlaceholderAPI** — optional but recommended
- **LuckPerms** — optional for permission management

## 🎨 Color Examples

### Legacy & HEX
| Format | Example | Result |
|--------|---------|--------|
| Legacy | `&cHello` | Red text |
| HEX (with `&`) | `&#FF0000Hello` | Red text |
| HEX (without `&`) | `#FF0000Hello` | Red text |
| Bungee HEX | `&x&F&F&0&0&0&0Hello` | Red text |

### Personal Color
| Format | Example | Result |
|--------|---------|--------|
| Personal (Legacy) | `/color &5&l` | Dark purple bold text |
| Personal (HEX) | `/color &#E43A96` | Pink text |
| Personal (HEX alt) | `/color #E43A96` | Pink text |
| Reset | `/color reset` | Removes personal color |

### Gradients
| Format | Example | Result |
|--------|---------|--------|
| Legacy gradient | `/color &f,&c` | White → red |
| HEX gradient | `/color #FF0000,#00FF00` | Red → green |
| 3-color gradient | `/color #FF0000,#FFAA00,#00FF00` | Red → gold → green |
| Gradient + modifier | `/color #FF0000,#00FF00&l` | Gradient + bold |
| Mixed formats | `/color #FF0000,&#00FF00` | `#` and `&#` together |
| Unlimited colors | `/color #FF0000,&#FFAA00,#00FF00,#0000FF,#FF00FF&l&n` | 5 colors + bold + underlined |

### MiniMessage
| Tag | Example | Result |
|-----|---------|--------|
| Named color | `<red>Hello</red>` | Red text |
| HEX | `<#FFAA00>Hello</#FFAA00>` | Gold text |
| Gradient | `<gradient:#FF0000:#00FF00>Hello</gradient>` | Red → green |
| Rainbow | `<rainbow>Hello</rainbow>` | Rainbow text |
| Bold | `<bold>Hello</bold>` | Bold text |
| Click (URL) | `<click:open_url:'https://example.com'>Click</click>` | Opens URL |
| Click (run) | `<click:run_command:'/spawn'>Click</click>` | Runs `/spawn` |
| Click (suggest) | `<click:suggest_command:'/color reset'>Click</click>` | Suggests in chat |
| Click (copy) | `<click:copy_to_clipboard:'/color reset'>Copy</click>` | Copies to clipboard |
| Hover | `<hover:show_text:'<red>Hover!'>Text</hover>` | Shows tooltip |
| Font | `<font:minecraft:uniform>Text</font>` | Monospace font |
| Keybind | `<key:key.jump>` | Shows jump key |
| Lang | `<lang:block.minecraft.stone>` | Translated name |
| Newline | `Line 1<newline>Line 2` | Two lines |

> **💡 Note:** All formats can be mixed in a single line — `<red>Hello &cWorld #FFAA00!</red>` works perfectly. The parser converts everything on the fly.

## ⚠️ Important Notes

### Server Software Compatibility
This plugin is designed and tested exclusively on **Spigot, Paper and Paper forks** (1.16.5+). It may work on other forks, but **compatibility is NOT guaranteed**.

**Not supported:**
- ❌ Any Minecraft Bedrock
- ❌ Cardboard (Spigot-Fabric compatibility layer)
- ❌ Hybrid server software (Arclight, Magma, Mohist and etc)
- ❌ Mod loaders (Fabric, Forge, LegacyFabric, LiteLoader, NeoForge, Quilt and etc)
- ❌ Proxy servers (BungeeCord, Velocity, Waterfall and etc)
- ❌ Vanilla Minecraft Java

### MiniMessage notes
- `<hover:show_item>`, `<hover:show_entity>` — not supported
- `<selector>`, `<score>`, `<nbt>` — not supported
- `<transition>`, `<shadow>`, `<pride>` — not supported
- `<click:open_file>`, `<click:change_page>` — not supported
- Unsupported tags are printed as plain text

## 📣 bStats

![bStats](https://bstats.org/signatures/bukkit/ChatEx-Refresh.svg)

## 📄 License
This project is licensed under the **GNU General Public License v2.0** — you are free to use, modify, and distribute it, as long as you keep the same license.

## 💬 Support
If you encounter issues or have questions:
1. Check that you have **Java 17+**
2. Verify your server is running **Spigot, Paper or a Paper forks (1.16.5+)**
3. Read the console for error messages

## 🌐 Contacts
- **Telegram or Discord:** @MrSpectrumYT
- **Bug reports & feature requests:** Contact via Telegram/Discord or Issues GitHub
