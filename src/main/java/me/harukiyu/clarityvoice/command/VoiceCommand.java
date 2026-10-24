package me.harukiyu.clarityvoice.command;

import de.maxhenkel.voicechat.api.Group;
import de.maxhenkel.voicechat.api.VoicechatConnection;
import de.maxhenkel.voicechat.api.VoicechatServerApi;
import me.harukiyu.clarityvoice.config.VoiceConfig;
import me.harukiyu.clarityvoice.service.OtpManager;
import me.harukiyu.clarityvoice.service.SessionManager;
import me.harukiyu.clarityvoice.service.TranslationService;
import me.harukiyu.clarityvoice.voice.ClarityVoiceChatPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles /clarityvoice, /cv, /voice, and /voiceweb commands with clean interactive
 * Adventure chat components, tab completion, and multi-language support.
 */
public class VoiceCommand implements CommandExecutor, TabCompleter {

    private final OtpManager otpManager;
    private final SessionManager sessionManager;
    private final ClarityVoiceChatPlugin voicePlugin;
    private final VoiceConfig config;
    private final TranslationService translationService;

    public VoiceCommand(
        OtpManager otpManager,
        SessionManager sessionManager,
        ClarityVoiceChatPlugin voicePlugin,
        VoiceConfig config,
        TranslationService translationService
    ) {
        this.otpManager = otpManager;
        this.sessionManager = sessionManager;
        this.voicePlugin = voicePlugin;
        this.config = config;
        this.translationService = translationService;
    }

    public VoiceCommand(
        OtpManager otpManager,
        SessionManager sessionManager,
        ClarityVoiceChatPlugin voicePlugin,
        VoiceConfig config
    ) {
        this(otpManager, sessionManager, voicePlugin, config, null);
    }

    @Override
    public boolean onCommand(
        @NotNull CommandSender sender,
        @NotNull Command command,
        @NotNull String label,
        @NotNull String[] args
    ) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Perintah ini hanya dapat dijalankan oleh pemain di dalam game.", NamedTextColor.RED));
            return true;
        }

        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            sendHelp(player, label);
            return true;
        }

        String sub = args[0].toLowerCase();
        switch (sub) {
            case "otp" -> handleOtp(player);
            case "disconnect", "dc" -> handleDisconnect(player);
            case "status" -> handleStatus(player);
            default -> sendHelp(player, label);
        }

        return true;
    }

    private void handleOtp(Player player) {
        String otp = otpManager.generateOtp(player.getUniqueId());
        String webUrl = config.getPublicUrl(); // Clean URL: player enters OTP themselves

        Component title = getComponent("command.otp.title", player, "<aqua>Clarity Voice</aqua>");
        Component codeLine = getComponent("command.otp.code", player,
            "<gray>Kode OTP Anda: <gold><otp></gold> <dark_gray>(Berlaku <duration> detik)</dark_gray></gray>",
            Placeholder.parsed("otp", otp),
            Placeholder.parsed("duration", String.valueOf(config.getOtpDurationSeconds()))
        );

        // Clickable COPY OTP button (bold button only)
        Component copyBtn = getComponent("command.otp.copy_button", player, "<yellow><bold>[SALIN KODE]</bold></yellow>")
            .clickEvent(ClickEvent.copyToClipboard(otp))
            .hoverEvent(HoverEvent.showText(getComponent("command.otp.copy_hover", player,
                "<yellow>Klik untuk menyalin kode OTP (<otp>) ke clipboard</yellow>",
                Placeholder.parsed("otp", otp))));

        // Clickable OPEN WEB button (bold button only)
        Component webBtn = getComponent("command.otp.web_button", player, "<aqua><bold>[BUKA WEB VOICE]</bold></aqua>")
            .clickEvent(ClickEvent.openUrl(webUrl))
            .hoverEvent(HoverEvent.showText(getComponent("command.otp.web_hover", player,
                "<aqua>Klik untuk membuka Web Voice di browser</aqua>")));

        Component actions = getComponent("command.otp.actions", player, "<gray>Aksi Cepat: </gray>")
            .append(copyBtn)
            .append(Component.text("  "))
            .append(webBtn);

        player.sendMessage(title);
        player.sendMessage(codeLine);
        player.sendMessage(actions);
    }

    private void handleDisconnect(Player player) {
        boolean disconnected = sessionManager.disconnect(player.getUniqueId(), "Diputuskan oleh pemain via in-game command.");
        if (disconnected) {
            sendMessage(player, "command.disconnect.success", "<green>Sesi Web Voice Anda berhasil diputuskan.</green>");
        } else {
            sendMessage(player, "command.disconnect.no_session", "<red>Anda tidak memiliki sesi Web Voice yang sedang aktif.</red>");
        }
    }

    private void handleStatus(Player player) {
        VoicechatServerApi api = voicePlugin.getServerApi();
        boolean modConnected = false;
        String groupName = null;

        if (api != null) {
            VoicechatConnection conn = api.getConnectionOf(player.getUniqueId());
            if (conn != null) {
                modConnected = conn.isConnected();
                if (conn.isInGroup()) {
                    Group group = conn.getGroup();
                    if (group != null) {
                        groupName = group.getName();
                    }
                }
            }
        }

        boolean webConnected = sessionManager.isWebConnected(player.getUniqueId());

        Component connectedText = getComponent("command.status.connected", player, "<green>Terhubung</green>");
        Component disconnectedText = getComponent("command.status.disconnected", player, "<red>Terputus</red>");

        Component title = getComponent("command.status.title", player, "<aqua>Clarity Voice - Status Koneksi</aqua>");
        Component modLine = getComponent("command.status.mod", player, "<gray>Mod Voice Chat: <value></gray>",
            Placeholder.component("value", modConnected ? connectedText : disconnectedText));
        Component webLine = getComponent("command.status.web", player, "<gray>Web Voice Chat: <value></gray>",
            Placeholder.component("value", webConnected ? connectedText : disconnectedText));

        Component groupLine;
        if (groupName != null) {
            groupLine = getComponent("command.status.group", player, "<gray>Grup Suara: <aqua><group></aqua></gray>",
                Placeholder.parsed("group", groupName));
        } else {
            Component noGroup = getComponent("command.status.no_group", player, "<dark_gray>Tidak Ada</dark_gray>");
            groupLine = getComponent("command.status.group", player, "<gray>Grup Suara: <group></gray>",
                Placeholder.component("group", noGroup));
        }

        player.sendMessage(title);
        player.sendMessage(modLine);
        player.sendMessage(webLine);
        player.sendMessage(groupLine);
    }

    private void sendHelp(Player player, String label) {
        Component title = getComponent("command.help.title", player, "<aqua>Clarity Voice - Daftar Perintah</aqua>");
        Component otpLine = getComponent("command.help.otp", player, "<gray>/<cmd> otp - Menghasilkan kode OTP untuk login Web Voice</gray>",
            Placeholder.parsed("cmd", label));
        Component dcLine = getComponent("command.help.disconnect", player, "<gray>/<cmd> disconnect - Memutuskan sesi Web Voice aktif</gray>",
            Placeholder.parsed("cmd", label));
        Component statusLine = getComponent("command.help.status", player, "<gray>/<cmd> status - Menampilkan status koneksi Voice</gray>",
            Placeholder.parsed("cmd", label));

        player.sendMessage(title);
        player.sendMessage(otpLine);
        player.sendMessage(dcLine);
        player.sendMessage(statusLine);
    }

    private Component getComponent(String subKey, Player player, String fallback, net.kyori.adventure.text.minimessage.tag.resolver.TagResolver... resolvers) {
        if (translationService != null) {
            return translationService.getComponent(subKey, player, fallback, resolvers);
        }
        return net.kyori.adventure.text.minimessage.MiniMessage.miniMessage().deserialize(fallback, resolvers);
    }

    private void sendMessage(Player player, String subKey, String fallback, net.kyori.adventure.text.minimessage.tag.resolver.TagResolver... resolvers) {
        if (player == null) return;
        player.sendMessage(getComponent(subKey, player, fallback, resolvers));
    }

    @Override
    public @Nullable List<String> onTabComplete(
        @NotNull CommandSender sender,
        @NotNull Command command,
        @NotNull String alias,
        @NotNull String[] args
    ) {
        if (args.length == 1) {
            List<String> completions = new ArrayList<>();
            String input = args[0].toLowerCase();
            for (String sub : List.of("otp", "disconnect", "status", "help")) {
                if (sub.startsWith(input)) {
                    completions.add(sub);
                }
            }
            return completions;
        }
        return List.of();
    }
}
