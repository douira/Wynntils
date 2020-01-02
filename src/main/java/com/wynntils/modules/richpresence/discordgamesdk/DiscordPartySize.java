package com.wynntils.modules.richpresence.discordgamesdk;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;

import java.util.Arrays;
import java.util.List;

/**
 * <i>native declaration : line 239</i><br>
 * This file was autogenerated by
 * <a href="http://jnaerator.googlecode.com/">JNAerator</a>,<br>
 * a tool written by <a href="http://ochafik.com/">Olivier Chafik</a> that
 * <a href="http://code.google.com/p/jnaerator/wiki/CreditsAndLicense">uses a
 * few opensource projects.</a>.<br>
 * For help, please visit
 * <a href="http://nativelibs4java.googlecode.com/">NativeLibs4Java</a> ,
 * <a href="http://rococoa.dev.java.net/">Rococoa</a>, or
 * <a href="http://jna.dev.java.net/">JNA</a>.
 */
public class DiscordPartySize extends Structure {
    public int current_size;
    public int max_size;

    public DiscordPartySize() {
        super();
    }

    protected List<String> getFieldOrder() {
        return Arrays.asList("current_size", "max_size");
    }

    public DiscordPartySize(int current_size, int max_size) {
        super();
        this.current_size = current_size;
        this.max_size = max_size;
    }

    public DiscordPartySize(Pointer peer) {
        super(peer);
    }

    public static class ByReference extends DiscordPartySize implements Structure.ByReference {

    };

    public static class ByValue extends DiscordPartySize implements Structure.ByValue {

    };
}
