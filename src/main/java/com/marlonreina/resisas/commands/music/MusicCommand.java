package com.marlonreina.resisas.commands.music;

import com.marlonreina.resisas.commands.Command;
import com.marlonreina.resisas.commands.CommandContext;
import com.marlonreina.resisas.dto.TrackLoadResult;
import com.marlonreina.resisas.music.GuildMusicManager;
import com.marlonreina.resisas.service.MusicService;
import com.marlonreina.resisas.utils.EmbedUtil;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.audio.hooks.ConnectionListener;
import net.dv8tion.jda.api.audio.hooks.ConnectionStatus;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.managers.AudioManager;

import java.util.List;
import java.util.Locale;

public class MusicCommand implements Command {

    private static final int QUEUE_LIMIT = 10;

    private final MusicService musicService;
    private final String defaultAction;

    public MusicCommand(MusicService musicService, String defaultAction) {
        this.musicService = musicService;
        this.defaultAction = defaultAction;
    }

    @Override
    public void execute(CommandContext context) {
        String action = resolveAction(context);

        switch (action) {
            case "play" -> play(context);
            case "pause" -> pause(context);
            case "resume" -> resume(context);
            case "queue" -> showQueue(context);
            case "skip", "next" -> skip(context);
            case "prev", "previous" -> previous(context);
            case "stop" -> stop(context);
            case "now", "np", "nowplaying" -> nowPlaying(context);
            case "volume" -> volume(context);
            case "loop", "repeat" -> loop(context);
            case "shuffle" -> shuffle(context);
            case "join" -> join(context);
            case "leave" -> leave(context);
            default -> showMenu(context);
        }
    }

    private String resolveAction(CommandContext context) {
        if (!"menu".equals(defaultAction)) {
            return defaultAction;
        }

        String[] args = context.getArgs();
        if (args.length == 0) {
            return "menu";
        }
        return args[0].toLowerCase(Locale.ROOT);
    }

    private void showMenu(CommandContext context) {
        var embed = EmbedUtil.info("Musica");
        embed.setDescription("Reproduce musica en tu canal de voz.");
        embed.addField("Reproducir", "`" + context.usage("music play <url o busqueda>") + "`", false);
        embed.addField("Controles", "`pause` - `resume` - `skip` - `prev` - `stop`", false);
        embed.addField("Cola", "`queue` - `now` - `shuffle` - `loop`", false);
        embed.addField("Voz", "`join` - `leave` - `volume <0-100>`", false);
        context.getEvent().getChannel().sendMessageEmbeds(embed.build()).queue();
    }

    private void play(CommandContext context) {
        String query = extractQuery(context);
        if (query.isBlank()) {
            context.getEvent().getChannel().sendMessageEmbeds(
                    EmbedUtil.usage(context.usage("music play <url o busqueda>")).build()
            ).queue();
            return;
        }

        if (!connectToVoiceChannel(context)) {
            return;
        }

        context.getEvent().getChannel().sendMessageEmbeds(
                EmbedUtil.loading("Buscando pista...").build()
        ).queue(message -> musicService.loadAndPlay(context.getEvent().getGuild(), query)
                .thenAccept(result -> editTrackLoadResult(message, result)));
    }

    private void pause(CommandContext context) {
        GuildMusicManager manager = musicService.getMusicManager(context.getEvent().getGuild());
        manager.getAudioPlayer().setPaused(true);
        context.getEvent().getChannel().sendMessageEmbeds(
                EmbedUtil.success("Pausa activada").build()
        ).queue();
    }

    private void resume(CommandContext context) {
        GuildMusicManager manager = musicService.getMusicManager(context.getEvent().getGuild());
        manager.getAudioPlayer().setPaused(false);
        context.getEvent().getChannel().sendMessageEmbeds(
                EmbedUtil.success("Reproduccion reanudada").build()
        ).queue();
    }

    private void showQueue(CommandContext context) {
        GuildMusicManager manager = musicService.getMusicManager(context.getEvent().getGuild());
        List<AudioTrack> queue = manager.getScheduler().getQueueSnapshot();
        StringBuilder description = new StringBuilder();
        description.append("Ahora: ")
                .append(musicService.formatTrack(manager.getAudioPlayer().getPlayingTrack()))
                .append(System.lineSeparator())
                .append(System.lineSeparator());

        if (queue.isEmpty()) {
            description.append("La cola esta vacia.");
        } else {
            for (int i = 0; i < Math.min(queue.size(), QUEUE_LIMIT); i++) {
                description.append("`#")
                        .append(i + 1)
                        .append("` ")
                        .append(musicService.formatTrack(queue.get(i)))
                        .append(System.lineSeparator());
            }
            if (queue.size() > QUEUE_LIMIT) {
                description.append("... y ")
                        .append(queue.size() - QUEUE_LIMIT)
                        .append(" mas.");
            }
        }

        context.getEvent().getChannel().sendMessageEmbeds(
                EmbedUtil.info("Cola de musica").setDescription(description.toString()).build()
        ).queue();
    }

    private void skip(CommandContext context) {
        GuildMusicManager manager = musicService.getMusicManager(context.getEvent().getGuild());
        AudioTrack nextTrack = manager.getScheduler().skip();
        context.getEvent().getChannel().sendMessageEmbeds(
                EmbedUtil.success("Pista saltada")
                        .setDescription(musicService.formatTrack(nextTrack))
                        .build()
        ).queue();
    }

    private void previous(CommandContext context) {
        GuildMusicManager manager = musicService.getMusicManager(context.getEvent().getGuild());
        AudioTrack previousTrack = manager.getScheduler().previous();
        if (previousTrack == null) {
            context.getEvent().getChannel().sendMessageEmbeds(
                    EmbedUtil.error("No hay una pista anterior disponible.").build()
            ).queue();
            return;
        }

        context.getEvent().getChannel().sendMessageEmbeds(
                EmbedUtil.success("Volviendo a la pista anterior")
                        .setDescription(musicService.formatTrack(previousTrack))
                        .build()
        ).queue();
    }

    private void stop(CommandContext context) {
        GuildMusicManager manager = musicService.getMusicManager(context.getEvent().getGuild());
        manager.getScheduler().stop();
        context.getEvent().getGuild().getAudioManager().closeAudioConnection();
        context.getEvent().getChannel().sendMessageEmbeds(
                EmbedUtil.success("Musica detenida").build()
        ).queue();
    }

    private void nowPlaying(CommandContext context) {
        GuildMusicManager manager = musicService.getMusicManager(context.getEvent().getGuild());
        context.getEvent().getChannel().sendMessageEmbeds(
                EmbedUtil.info("Reproduciendo ahora")
                        .setDescription(musicService.formatTrack(manager.getAudioPlayer().getPlayingTrack()))
                        .build()
        ).queue();
    }

    private void volume(CommandContext context) {
        String[] args = context.getArgs();
        int volumeIndex = "menu".equals(defaultAction) ? 1 : 0;
        if (args.length <= volumeIndex) {
            context.getEvent().getChannel().sendMessageEmbeds(
                    EmbedUtil.usage(context.usage("music volume <0-100>")).build()
            ).queue();
            return;
        }

        int volume = parseVolume(args[volumeIndex]);
        if (volume < 0 || volume > 100) {
            context.getEvent().getChannel().sendMessageEmbeds(
                    EmbedUtil.error("El volumen debe estar entre 0 y 100.").build()
            ).queue();
            return;
        }

        GuildMusicManager manager = musicService.getMusicManager(context.getEvent().getGuild());
        manager.getAudioPlayer().setVolume(volume);
        context.getEvent().getChannel().sendMessageEmbeds(
                EmbedUtil.success("Volumen actualizado")
                        .setDescription("Volumen: `" + volume + "`")
                        .build()
        ).queue();
    }

    private void loop(CommandContext context) {
        GuildMusicManager manager = musicService.getMusicManager(context.getEvent().getGuild());
        boolean repeating = !manager.getScheduler().isRepeating();
        manager.getScheduler().setRepeating(repeating);
        context.getEvent().getChannel().sendMessageEmbeds(
                EmbedUtil.success(repeating ? "Loop activado" : "Loop desactivado").build()
        ).queue();
    }

    private void shuffle(CommandContext context) {
        GuildMusicManager manager = musicService.getMusicManager(context.getEvent().getGuild());
        manager.getScheduler().shuffle();
        context.getEvent().getChannel().sendMessageEmbeds(
                EmbedUtil.success("Cola mezclada").build()
        ).queue();
    }

    private void join(CommandContext context) {
        if (connectToVoiceChannel(context)) {
            context.getEvent().getChannel().sendMessageEmbeds(
                    EmbedUtil.success("Conectado al canal de voz").build()
            ).queue();
        }
    }

    private void leave(CommandContext context) {
        context.getEvent().getGuild().getAudioManager().closeAudioConnection();
        context.getEvent().getChannel().sendMessageEmbeds(
                EmbedUtil.success("Desconectado del canal de voz").build()
        ).queue();
    }

    private boolean connectToVoiceChannel(CommandContext context) {
        Member member = context.getEvent().getMember();
        if (member == null || member.getVoiceState() == null || !member.getVoiceState().inAudioChannel()) {
            context.getEvent().getChannel().sendMessageEmbeds(
                    EmbedUtil.error("Debes estar en un canal de voz.").build()
            ).queue();
            return false;
        }

        Guild guild = context.getEvent().getGuild();
        AudioChannel channel = member.getVoiceState().getChannel();
        if (!hasVoicePermissions(guild, channel)) {
            context.getEvent().getChannel().sendMessageEmbeds(
                    EmbedUtil.error("No tengo permisos para conectar y hablar en ese canal de voz.").build()
            ).queue();
            return false;
        }

        GuildMusicManager musicManager = musicService.getMusicManager(guild);

        AudioManager audioManager = guild.getAudioManager();

        audioManager.setSendingHandler(musicManager.getSendHandler());
        audioManager.openAudioConnection(channel);


        guild.getAudioManager().setConnectionListener(new ConnectionListener() {
            @Override
            public void onStatusChange(ConnectionStatus status) {
                System.out.println("[VOICE] Status: " + status);
            }

            @Override public void onPing(long ping) {}
        });
        System.out.println("[VOICE] Handler set? " + (audioManager.getSendingHandler() != null));

        return true;
    }

    private boolean hasVoicePermissions(Guild guild, AudioChannel channel) {
        Member selfMember = guild.getSelfMember();
        if (channel instanceof VoiceChannel voiceChannel) {
            return selfMember.hasPermission(voiceChannel, Permission.VOICE_CONNECT, Permission.VOICE_SPEAK);
        }
        return selfMember.hasPermission(channel, Permission.VOICE_CONNECT, Permission.VOICE_SPEAK);
    }

    private String extractQuery(CommandContext context) {
        String[] args = context.getArgs();
        int startIndex = "menu".equals(defaultAction) ? 1 : 0;
        if (args.length <= startIndex) {
            return "";
        }

        return String.join(" ", List.of(args).subList(startIndex, args.length));
    }

    private void editTrackLoadResult(net.dv8tion.jda.api.entities.Message message, TrackLoadResult result) {
        if (result.loaded()) {
            message.editMessageEmbeds(
                    EmbedUtil.success(result.message())
                            .setDescription(result.title())
                            .build()
            ).queue();
            return;
        }

        message.editMessageEmbeds(
                EmbedUtil.error(result.message()).build()
        ).queue();
    }

    private int parseVolume(String rawVolume) {
        try {
            return Integer.parseInt(rawVolume);
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
