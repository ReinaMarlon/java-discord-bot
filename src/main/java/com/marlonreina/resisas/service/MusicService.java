package com.marlonreina.resisas.service;

import com.marlonreina.resisas.dto.TrackLoadResult;
import com.marlonreina.resisas.music.GuildMusicManager;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import net.dv8tion.jda.api.entities.Guild;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class MusicService {

    private static final int DEFAULT_VOLUME = 60;

    private final AudioPlayerManager playerManager;
    private final Map<Long, GuildMusicManager> musicManagers = new ConcurrentHashMap<>();

    public MusicService() {
        this.playerManager = new DefaultAudioPlayerManager();
        playerManager.registerSourceManager(new dev.lavalink.youtube.YoutubeAudioSourceManager());
        AudioSourceManagers.registerRemoteSources(
                playerManager,
                com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager.class
        );
        AudioSourceManagers.registerLocalSource(playerManager);
    }

    public GuildMusicManager getMusicManager(Guild guild) {
        long guildId = guild.getIdLong();
        GuildMusicManager musicManager = musicManagers.computeIfAbsent(guildId, id -> {
            GuildMusicManager manager = new GuildMusicManager(playerManager.createPlayer());
            manager.getAudioPlayer().setVolume(DEFAULT_VOLUME);
            return manager;
        });
        guild.getAudioManager().setSendingHandler(musicManager.getSendHandler());
        return musicManager;
    }

    public CompletableFuture<TrackLoadResult> loadAndPlay(Guild guild, String query) {
        CompletableFuture<TrackLoadResult> future = new CompletableFuture<>();
        GuildMusicManager musicManager = getMusicManager(guild);
        String identifier = normalizeIdentifier(query);

        playerManager.loadItemOrdered(musicManager, identifier, new AudioLoadResultHandler() {

            @Override
            public void trackLoaded(AudioTrack track) {
                System.out.println("[MUSIC] Track loaded: " + track.getInfo().title);

                musicManager.getScheduler().queue(track);
                future.complete(new TrackLoadResult(true, track.getInfo().title, "Agregado a la cola."));
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                System.out.println("[MUSIC] Playlist loaded: " + playlist.getName());

                AudioTrack selectedTrack = playlist.getSelectedTrack();
                AudioTrack track = selectedTrack == null ? playlist.getTracks().get(0) : selectedTrack;

                musicManager.getScheduler().queue(track);
                future.complete(new TrackLoadResult(true, track.getInfo().title, "Agregado desde playlist."));
            }

            @Override
            public void noMatches() {
                System.out.println("[MUSIC] ❌ No matches for: " + identifier);

                future.complete(new TrackLoadResult(false, null, "No encontre resultados."));
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                System.out.println("[MUSIC] ❌ Load failed: " + identifier);
                exception.printStackTrace(); // 🔥 CLAVE

                future.complete(new TrackLoadResult(false, null, "Error cargando pista."));
            }
        });

        return future;
    }

    public String formatTrack(AudioTrack track) {
        if (track == null) {
            return "Nada reproduciendose.";
        }

        AudioTrackInfo info = track.getInfo();
        return "[" + info.title + "](" + info.uri + ")";
    }

    private String normalizeIdentifier(String query) {
        if (query.startsWith("http://") || query.startsWith("https://")) {
            return normalizeUrl(query);
        }
        return "ytsearch:" + query;
    }

    private String normalizeUrl(String query) {
        try {
            URI uri = new URI(query);
            if (!isYoutubeWatchUrl(uri) || uri.getQuery() == null) {
                return query;
            }

            String videoId = findQueryValue(uri.getQuery(), "v");
            if (videoId == null || videoId.isBlank()) {
                return query;
            }

            return "https://www.youtube.com/watch?v=" + videoId;
        } catch (URISyntaxException e) {
            return query;
        }
    }

    private boolean isYoutubeWatchUrl(URI uri) {
        String host = uri.getHost();
        return host != null
                && (host.equalsIgnoreCase("youtube.com") || host.equalsIgnoreCase("www.youtube.com"))
                && "/watch".equals(uri.getPath());
    }

    private String findQueryValue(String query, String key) {
        String[] params = query.split("&");
        for (String param : params) {
            String[] pair = param.split("=", 2);
            if (pair.length == 2 && key.equals(pair[0])) {
                return pair[1];
            }
        }
        return null;
    }

    private void disconnectIfIdle(Guild guild, GuildMusicManager musicManager) {

    }
}
