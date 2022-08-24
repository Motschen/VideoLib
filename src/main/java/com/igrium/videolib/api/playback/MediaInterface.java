package com.igrium.videolib.api.playback;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

import com.igrium.videolib.api.VideoHandle;

import net.minecraft.util.Identifier;

/**
 * Behavior pertaining loading and playback of video media.
 * @param <T> The type that's expected for the video handle.
 */
public interface MediaInterface<T extends VideoHandle> {

    /**
     * Load a video and prepare it for playback.
     * @param handle Video handle.
     * @return Success.
     */
    public boolean load(T handle);

    public default boolean load(String uri) throws MalformedURLException, URISyntaxException {
        return load(getHandle(uri));
    }

    /**
     * Load a video and play it.
     * @param handle Video handle.
     * @return Success.
     */
    public boolean play(T handle);

    public default boolean play(String uri) throws MalformedURLException, URISyntaxException {
        return play(getHandle(uri));
    }

    /**
     * Check whether there is currently a video loaded.
     * @return Is there a video loaded?
     */
    public boolean hasMedia();

    /**
     * Get the handle of the currently loaded video.
     * @return An optional with the handle.
     */
    public Optional<T> currentMedia();

    /**
     * Get a media handle that this player will support from an identifier.
     * 
     * @param id ID to use.
     * @return The handle.
     */
    public T getHandle(Identifier id);

    /**
     * Get a media handle that this player will support from a uri.
     * 
     * @param uri Uri to use.
     * @return The handle.
     * @throws MalformedURLException If the URL protocol is not supported.
     */
    public T getHandle(URI uri) throws MalformedURLException;

    public default T getHandle(String uri) throws MalformedURLException, URISyntaxException {
        return getHandle(new URI(uri));
    }
}