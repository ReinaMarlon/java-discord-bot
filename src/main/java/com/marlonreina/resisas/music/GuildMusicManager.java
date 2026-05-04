package com.marlonreina.resisas.music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings(
        value = {"EI_EXPOSE_REP", "EI_EXPOSE_REP2"},
        justification = "The guild manager intentionally exposes LavaPlayer objects for command controls."
)
public class GuildMusicManager {

    private final AudioPlayer audioPlayer;
    private final TrackScheduler scheduler;
    private final AudioPlayerSendHandler sendHandler;

    public GuildMusicManager(AudioPlayer audioPlayer) {
        this.audioPlayer = audioPlayer;
        this.scheduler = new TrackScheduler(audioPlayer);
        this.sendHandler = new AudioPlayerSendHandler(audioPlayer);
        audioPlayer.addListener(scheduler);
    }

    public AudioPlayer getAudioPlayer() {
        return audioPlayer;
    }

    public TrackScheduler getScheduler() {
        return scheduler;
    }

    public AudioPlayerSendHandler getSendHandler() {
        return sendHandler;
    }
}
