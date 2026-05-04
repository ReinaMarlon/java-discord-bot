package com.marlonreina.resisas.music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@SuppressFBWarnings(
        value = "EI_EXPOSE_REP2",
        justification = "The scheduler must control the shared guild AudioPlayer instance."
)
public class TrackScheduler extends AudioEventAdapter {

    private static final int MAX_HISTORY_SIZE = 20;

    private final AudioPlayer audioPlayer;
    private final Queue<AudioTrack> queue = new ConcurrentLinkedQueue<>();
    private final Deque<AudioTrack> history = new ArrayDeque<>();
    private boolean repeating;

    public TrackScheduler(AudioPlayer audioPlayer) {
        this.audioPlayer = audioPlayer;
    }

    public void queue(AudioTrack track) {
        System.out.println("[SCHEDULER] Queueing: " + track.getInfo().title);

        boolean started = audioPlayer.startTrack(track, true);

        System.out.println("[SCHEDULER] startTrack result: " + started);

        if (!started) {
            System.out.println("[SCHEDULER] Added to queue");
            queue.offer(track);
        } else {
            System.out.println("[SCHEDULER] Started playing immediately");
        }
    }

    public AudioTrack skip() {
        AudioTrack nextTrack = queue.poll();
        audioPlayer.startTrack(nextTrack, false);
        return nextTrack;
    }

    public AudioTrack previous() {
        if (history.isEmpty()) {
            return null;
        }

        AudioTrack previousTrack = history.pollLast().makeClone();
        AudioTrack currentTrack = audioPlayer.getPlayingTrack();
        if (currentTrack != null) {
            queue.offer(currentTrack.makeClone());
        }
        audioPlayer.startTrack(previousTrack, false);
        return previousTrack;
    }

    public void stop() {
        queue.clear();
        audioPlayer.stopTrack();
    }

    public void shuffle() {
        List<AudioTrack> tracks = new ArrayList<>(queue);
        Collections.shuffle(tracks);
        queue.clear();
        queue.addAll(tracks);
    }

    public List<AudioTrack> getQueueSnapshot() {
        return List.copyOf(queue);
    }

    public boolean isRepeating() {
        return repeating;
    }

    public void setRepeating(boolean repeating) {
        this.repeating = repeating;
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        if (endReason.mayStartNext) {
            remember(track);
            if (repeating) {
                player.startTrack(track.makeClone(), false);
                return;
            }
            skip();
        }
    }

    private void remember(AudioTrack track) {
        history.addLast(track.makeClone());
        while (history.size() > MAX_HISTORY_SIZE) {
            history.pollFirst();
        }
    }
}
