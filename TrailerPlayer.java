package com.dooo.android;

import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.android.volley.VolleyError;
import com.dooo.android.list.YTStreamList;
import com.dooo.android.utils.HelperUtils;
import com.dooo.android.utils.Yts;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.utils.YouTubePlayerUtils;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;

import org.imaginativeworld.oopsnointernet.dialogs.signal.DialogPropertiesSignal;
import org.imaginativeworld.oopsnointernet.dialogs.signal.NoInternetDialogSignal;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class TrailerPlayer extends AppCompatActivity {


    private boolean vpnStatus;
    private HelperUtils helperUtils;
    YouTubePlayerView youTubePlayerView;
    String youtubeId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
            getWindow().getAttributes().layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }

        if (AppConfig.FLAG_SECURE) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,
                    WindowManager.LayoutParams.FLAG_SECURE);
        }

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setRequestedOrientation(SCREEN_ORIENTATION_SENSOR_LANDSCAPE);

        Window window = this.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.black));

        setContentView(R.layout.activity_trailer_player);
        youTubePlayerView = findViewById(R.id.YoutubePlayerViewer);
        getLifecycle().addObserver(youTubePlayerView);
        Intent intent = getIntent();
        String trailerUrl = intent.getExtras().getString("Trailer_URL");
        Pattern pattern = Pattern.compile("(?<=watch\\?v=|/videos/|embed\\/|youtu.be\\/|\\/v\\/|\\/e\\/|watch\\" +
                "?v%3D|watch\\?feature=player_embedded&v=|%2Fvideos%2F|embed%\u200C\u200B2F|youtu.be%2F|%2Fv%2F)[^#\\&\\?\\n]*");
        Matcher matcher = pattern.matcher(trailerUrl);

        if (matcher.find()) {
            youtubeId = matcher.group();
            PlayYTTrailer(youtubeId);
        }


        if (!AppConfig.allowVPN) {
            //check vpn connection
            helperUtils = new HelperUtils(TrailerPlayer.this);
            vpnStatus = helperUtils.isVpnConnectionAvailable();
            if (vpnStatus) {
                helperUtils.showWarningDialog(TrailerPlayer.this, "VPN!", "You are Not Allowed To Use VPN Here!", R.raw.network_activity_icon);
            }
        }


        // No Internet Dialog: Signal
        NoInternetDialogSignal.Builder builder = new NoInternetDialogSignal.Builder(
                this,
                getLifecycle()
        );
        DialogPropertiesSignal properties = builder.getDialogProperties();
        // Optional
        properties.setConnectionCallback(hasActiveConnection -> {
            // ...
        });
        properties.setCancelable(true); // Optional
        properties.setNoInternetConnectionTitle("No Internet"); // Optional
        properties.setNoInternetConnectionMessage("Check your Internet connection and try again"); // Optional
        properties.setShowInternetOnButtons(true); // Optional
        properties.setPleaseTurnOnText("Please turn on"); // Optional
        properties.setWifiOnButtonText("Wifi"); // Optional
        properties.setMobileDataOnButtonText("Mobile data"); // Optional

        properties.setOnAirplaneModeTitle("No Internet"); // Optional
        properties.setOnAirplaneModeMessage("You have turned on the airplane mode."); // Optional
        properties.setPleaseTurnOffText("Please turn off"); // Optional
        properties.setAirplaneModeOffButtonText("Airplane mode"); // Optional
        properties.setShowAirplaneModeOffButtons(true); // Optional
        builder.build();

        fullScreenall();
        View customPlayerUi = youTubePlayerView.inflateCustomPlayerUi(R.layout.traier_player_controlls);

        YouTubePlayerListener listener = new AbstractYouTubePlayerListener() {

            @Override
            public void onReady(@NonNull YouTubePlayer youTubePlayer) {
                CustomPlayerUiController customPlayerUiController = new CustomPlayerUiController(TrailerPlayer.this, customPlayerUi, youTubePlayer, youTubePlayerView);
                youTubePlayer.addListener(customPlayerUiController);
            }
        };

// disable iframe ui
        youTubePlayerView.setEnableAutomaticInitialization(false);
        //we will handle everythng
        IFramePlayerOptions options = new IFramePlayerOptions.Builder().controls(0).build();
        youTubePlayerView.initialize(listener, options);
    }

    private void PlayYTTrailer(String youtubeId) {
        youTubePlayerView.addYouTubePlayerListener(new AbstractYouTubePlayerListener() {
            @Override
            public void onReady(@NonNull YouTubePlayer youTubePlayer) {
                youTubePlayer.loadVideo(youtubeId, 0);
            }
        }
        );
    }

    public void fullScreenall() {
        if (Build.VERSION.SDK_INT > 11 && Build.VERSION.SDK_INT < 19) { // lower api
            View v = this.getWindow().getDecorView();
            v.setSystemUiVisibility(View.GONE);
        } else if (Build.VERSION.SDK_INT >= 19) {
            //for new api versions.
            View decorView = getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            decorView.setSystemUiVisibility(uiOptions);
        }
    }


    @Override
    protected void onResume() {
        super.onResume();

        if (!AppConfig.allowVPN) {
            //check vpn connection
            helperUtils = new HelperUtils(TrailerPlayer.this);
            vpnStatus = helperUtils.isVpnConnectionAvailable();
            if (vpnStatus) {
                helperUtils.showWarningDialog(TrailerPlayer.this, "VPN!", "You are Not Allowed To Use VPN Here!", R.raw.network_activity_icon);
            }
        }
    }
}