package com.igrium.videolib.vlc;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;


import com.igrium.videolib.api.VideoHandle;
import com.igrium.videolib.api.VideoHandleFactory;
import com.igrium.videolib.api.VideoPlayer;
import com.igrium.videolib.api.playback.CodecInterface;
import com.igrium.videolib.api.playback.ControlsInterface;
import com.igrium.videolib.api.playback.MediaInterface;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;
import uk.co.caprica.vlcj.media.VideoTrackInfo;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;

public class VLCVideoPlayer implements VideoPlayer {
    protected final Identifier id;
    protected final VLCVideoManager manager;

    protected EmbeddedMediaPlayer mediaPlayer;
    protected OpenGLVideoSurface surface;

    protected VLCMediaInterface mediaInterface = new VLCMediaInterface();
    protected VLCControlsInterface controlsInterface = new VLCControlsInterface();
    protected VLCCodecInterface codecInterface = new VLCCodecInterface();

    protected VLCEvents events = new VLCEvents();

    private boolean textureRegistered = false;
    
    protected VLCVideoPlayer(Identifier id, VLCVideoManager manager) {
        this.id = id;
        this.manager = manager;
    }

    protected void init() {
        if (mediaPlayer != null || !manager.hasNatives()) return;

        mediaPlayer = manager.getFactory().mediaPlayers().newEmbeddedMediaPlayer();
        surface = new OpenGLVideoSurface();
        mediaPlayer.videoSurface().set(surface);
        mediaPlayer.events().addMediaPlayerEventListener(events);
    }

    public final Identifier getId() {
        return id;
    }
    
    public Identifier getTexture() {
        Identifier texId = VideoPlayer.getTextureId(getId());
        if (!textureRegistered) registerTexture(texId);
        return texId;
    }

    // We can't do this in constructor because the texture manager isn't created yet.
    protected void registerTexture(Identifier texId) {
        if (textureRegistered) return;
        if (!RenderSystem.isOnRenderThread()) {
            RenderSystem.recordRenderCall(() -> registerTexture(texId));
            return;
        }

        MinecraftClient.getInstance().getTextureManager().registerTexture(texId, surface.getTexture());
        textureRegistered = true;
    }


    @Override
    public void close() {
        getControlsInterface().stop();
        surface.texture.close();        
    }

    @Override
    public MediaInterface getMediaInterface() {
        return mediaInterface;
    }

    @Override
    public ControlsInterface getControlsInterface() {
        return controlsInterface;
    }
    
    @Override
    public VLCCodecInterface getCodecInterface() {
        return codecInterface;
    }

    @Override
    public VLCEvents getEvents() {
        return events;
    }

    public class VLCMediaInterface implements MediaInterface {

        private String getAddress(VideoHandle handle) {
            Optional<String> address = handle.getAddress();
            if (address.isEmpty()) {
                throw new IllegalArgumentException("This video player only supports video handles with accessible URLs.");
            }
            return address.get();
        }

        @Override
        public boolean load(VideoHandle handle) {
            return mediaPlayer.media().prepare(getAddress(handle));
        }

        @Override
        public boolean play(VideoHandle handle) throws IllegalArgumentException {
            return mediaPlayer.media().play(getAddress(handle));
        }

        @Override
        public boolean hasMedia() {
            return mediaPlayer.status().isPlayable();
        }

        @Override
        public Optional<VideoHandle> currentMedia() {
            try {
                return Optional.of(new VideoHandle.UrlVideoHandle(new URL(mediaPlayer.media().info().mrl())));
            } catch (MalformedURLException e) {
                throw new RuntimeException("VLC returned an illegal URL.", e);
            }
        }

        @Override
        public VideoHandleFactory getVideoHandleFactory() {
            return manager.getVideoHandleFactory();
        }
    }

    public class VLCControlsInterface implements ControlsInterface {

        @Override
        public void play() {
            mediaPlayer.controls().play();       
        }

        @Override
        public void stop() {
            mediaPlayer.controls().stop();
        }

        @Override
        public void setPause(boolean pause) {
            mediaPlayer.controls().setPause(pause);
        }

        @Override
        public void setTime(long time) {
            mediaPlayer.controls().setTime(time);
        }

        @Override
        public long getTime() {
            return mediaPlayer.status().time();
        }

        @Override
        public long getLength() {
            return mediaPlayer.status().length();
        }

        @Override
        public void setRepeat(boolean repeat) {
            mediaPlayer.controls().setRepeat(repeat);
        }

        @Override
        public boolean repeat() {
            return mediaPlayer.controls().getRepeat();
        }
        
    }

    public class VLCCodecInterface implements CodecInterface {

        private VideoTrackInfo getVideoTrack() {
            return mediaPlayer.media().info().videoTracks().get(0);
        }
        private boolean hasVideoTrack() {
            try {
                return !mediaPlayer.media().info().videoTracks().isEmpty();
            } catch (NullPointerException e) {
                return false;
            }
        }

        @Override
        public int getWidth() {
            if (!hasVideoTrack()) return 0;
            return getVideoTrack().width();
        }

        @Override
        public int getHeight() {
            if (!hasVideoTrack()) return 0;
            return getVideoTrack().height();
        }

        @Override
        public float getFrameRate() {
            if (!hasVideoTrack()) return 0;
            VideoTrackInfo videoTrack = getVideoTrack();
            float fps = videoTrack.frameRate();
            float base = videoTrack.frameRateBase();
            return fps / base;
        }

        @Override
        public float getAspectRatio() {
            if (!hasVideoTrack()) return 1;

            VideoTrackInfo videoTrack = getVideoTrack();
            float ratio = videoTrack.sampleAspectRatio();
            float base = videoTrack.sampleAspectRatioBase();

            return CodecInterface.super.getAspectRatio() * (ratio / base);
        }
        
    }
}
