package com.igrium.videolib;

import com.igrium.videolib.api.MediaManager;
import com.igrium.videolib.api.VideoManager;
import com.igrium.videolib.test.AddVideoPlayerCommand;
import com.igrium.videolib.test.TestRenderDispatcher;
import com.igrium.videolib.vlc.VLCVideoManager;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v1.ClientCommandManager;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;

public class VideoLibClient implements ClientModInitializer {

    private static VideoLibClient instance;

    private TestRenderDispatcher testRenderDispatcher;
    private VideoManager videoManager;

    public static VideoLibClient getInstance() {
        return instance;
    }

    public VideoManager getVideoManager() {
        return videoManager;
    }


    public TestRenderDispatcher getTestRenderDispatcher() {
        return testRenderDispatcher;
    }

    @Override
    public void onInitializeClient() {
        instance = this;
        testRenderDispatcher = new TestRenderDispatcher();

        videoManager = new VLCVideoManager();

        WorldRenderEvents.AFTER_ENTITIES.register(testRenderDispatcher::render);
        AddVideoPlayerCommand.register(ClientCommandManager.DISPATCHER);
    }
    
}