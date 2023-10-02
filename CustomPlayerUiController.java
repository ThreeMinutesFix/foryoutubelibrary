package com.dooo.android;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatSeekBar;
import androidx.core.content.ContextCompat;

import com.google.android.material.slider.Slider;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.utils.YouTubePlayerTracker;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;

import org.jetbrains.annotations.NotNull;

import java.util.Locale;

import kotlin.Unit;
import kotlin.jvm.functions.Function2;
import og.android.lib.toggleiconview.ToggleIconView;


class CustomPlayerUiController extends AbstractYouTubePlayerListener {
    private final YouTubePlayerTracker playerTracker;
    private final Context context;
    private final YouTubePlayer youTubePlayer;
    private final YouTubePlayerView youTubePlayerView;
    AppCompatSeekBar Slider;
    private View panel;
    private View progressbar;
    private TextView videoCurrentTimeTextView;
    private TextView videoDurationTextView;
    private boolean fullscreen = false;

    CustomPlayerUiController(Context context, View customPlayerUi, YouTubePlayer youTubePlayer, YouTubePlayerView youTubePlayerView) {
        this.context = context;
        this.youTubePlayer = youTubePlayer;
        this.youTubePlayerView = youTubePlayerView;

        playerTracker = new YouTubePlayerTracker();
        youTubePlayer.addListener(playerTracker);

        initViews(customPlayerUi);
    }

    private void initViews(View playerUi) {
        panel = playerUi.findViewById(R.id.panel);

        videoCurrentTimeTextView = playerUi.findViewById(R.id.current_time);
        videoDurationTextView = playerUi.findViewById(R.id.video_duration);
        Slider = playerUi.findViewById(R.id.progress_timer);
        ToggleIconView toggleIconView = playerUi.findViewById(R.id.playPause);
        if (playerTracker.getState() == PlayerConstants.PlayerState.PLAYING) {
            toggleIconView.setChecked(true);
        } else {
            toggleIconView.setChecked(false);
        }

        Slider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                if (b) {
                    youTubePlayer.seekTo(progress);
                }
            }


            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        toggleIconView.setOnClickListener(view -> toggleIconView.toggle());
        toggleIconView.setOnCheckedChangeListener((toggleIconView1, checked) -> {
            if (checked) {
                youTubePlayer.pause();
            } else {
                youTubePlayer.play();
            }
        });


    }



    @Override
    public void onReady(@NonNull YouTubePlayer youTubePlayer) {


    }

    @Override
    public void onStateChange(@NonNull YouTubePlayer youTubePlayer, @NonNull PlayerConstants.PlayerState state) {
        if (state == PlayerConstants.PlayerState.PLAYING || state == PlayerConstants.PlayerState.PAUSED || state == PlayerConstants.PlayerState.VIDEO_CUED)
            panel.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));
        else if (state == PlayerConstants.PlayerState.BUFFERING)
            panel.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));
        else if (state == PlayerConstants.PlayerState.ENDED)
        {

        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onCurrentSecond(@NonNull YouTubePlayer youTubePlayer, float second) {
        String formattedTime = formatDuration(second);
        videoCurrentTimeTextView.setText(formattedTime+"");
        Slider.setProgress((int) second);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onVideoDuration(@NonNull YouTubePlayer youTubePlayer, float duration) {
        String formattedDuration = formatDuration(duration);
        videoDurationTextView.setText(formattedDuration + "");
        Slider.setMax((int) duration);
    }

    public String formatDuration(float duration) {
        int totalSeconds = (int) duration;
        int hours = totalSeconds / 3600;
        int minutes = (totalSeconds % 3600) / 60;
        int seconds = totalSeconds % 60;

        // Use String.format to format the time components with leading zeros
        return String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds);
    }

}
