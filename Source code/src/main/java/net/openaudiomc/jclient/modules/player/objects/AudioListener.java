package net.openaudiomc.jclient.modules.player.objects;

import lombok.Getter;
import lombok.Setter;

import net.openaudiomc.jclient.OpenAudioApi;
import net.openaudiomc.jclient.OpenAudioMc;
import net.openaudiomc.jclient.modules.socket.objects.OaPacket;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.*;

public class AudioListener {

    @Getter private Player player;
    @Getter private String token;
    @Getter @Setter private String placingSpeaker = null;
    @Getter private Boolean isConnected = false;
    private OpenAudioApi api = new OpenAudioApi();
    private List<String> regions = new ArrayList<>();
    private Map<String, Integer> speakers = new HashMap<>();
    private int speakerRadius = OpenAudioMc.getInstance().getConfig().getInt("web.speaker_radius");

    public AudioListener(Player player) {
        this.player = player;
        updateToken();
    }

    public void sendLink() {
        String url = OpenAudioMc.getInstance().getConfig().getString("web.url");

        updateToken();

        String tokenRAW = this.player.getName() +
                ":" +
                OpenAudioMc.getInstance().getSocketModule().getKeyHolder().getPublickey() +
                ":" +
                this.token;

        url = url + "?s=" + new String(Base64.getEncoder().encode(tokenRAW.getBytes()));

        String message = "[\"\",{\"text\":\"" + ChatColor.translateAlternateColorCodes('&', OpenAudioMc.getInstance().getConfig().getString("messages.provide_url")) + "\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"" + url + "\"}}]";
        OpenAudioMc.getInstance().getReflection().sendChatPacket(player, message);
    }

    public void updateSpeakers() {
        List<Location> near = new ArrayList<>(OpenAudioMc.getInstance().getMediaModule().getSpeakers().keySet());
        near.removeIf(l -> player.getLocation().distance(l) > this.speakerRadius);

        Map<String, Integer> nearest = new HashMap<>();

        near.forEach(l -> {
            String id = OpenAudioMc.getInstance().getMediaModule().getSpeakers().get(l);
            if (nearest.get(id) == null || (int) player.getLocation().distance(l) > nearest.get(id)) {
                int a = (((int) player.getLocation().distance(l)) - this.speakerRadius);
                a = (a < 0 ? - a : a);
                nearest.put(id, a);
            }
        });

        Set<String> updatedSpeakers = nearest.keySet();

        //((this.speakerRadius / 100) * (this.speakerRadius - nearest.get(s))

        List<String> newRegions = new ArrayList<String>(updatedSpeakers);
        newRegions.removeAll(speakers.keySet());

        List<String> leftRegions = new ArrayList<String>(speakers.keySet());
        leftRegions.removeAll(updatedSpeakers);

        for (String s : nearest.keySet()) {
            if (speakers.get(s) != nearest.get(s)) {
                double a = ((double) nearest.get(s)) / ((double) this.speakerRadius);
                a = a * 100;
                this.api.setSpeakerVolume(this, s, (int) a);
            }
        }

        for (String s : newRegions) {
            this.api.startSpeaker(this, s, nearest.get(s));
        }

        for (String s : leftRegions) {
            this.api.stopSpeaker(this, s);
        }

        speakers = nearest;
    }

    public void updateRegions(List<String> c) {

        List<String> newRegions = new ArrayList<String>(c);
        newRegions.removeAll(regions);

        List<String> leftRegions = new ArrayList<String>(regions);
        leftRegions.removeAll(c);

        for (String s : newRegions) {
            api.startRegion(this, s);
        }

        for (String s : leftRegions) {
            api.stopRegion(this, s);
        }

        regions = c;
    }

    public void onConnect() {
        isConnected = true;
        this.regions.clear();
        this.speakers.clear();
        System.out.println("[OpenAudioMc-Connector] User " + player.getName() + " connected!");
        if (OpenAudioMc.getInstance().getConfig().getString("messages.connected").equals("-")) return;
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', OpenAudioMc.getInstance().getConfig().getString("messages.connected")));
    }

    public void onDisconnect() {
        isConnected = false;
        System.out.println("[OpenAudioMc-Connector] User " + player.getName() + " disconnected!");
        if (OpenAudioMc.getInstance().getConfig().getString("messages.disconnected").equals("-")) return;
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', OpenAudioMc.getInstance().getConfig().getString("messages.disconnected")));
    }

    public void onQuit() {

    }

    public void sendPacket(OaPacket p) {
        if (isConnected) {
            p.setPlayer(this);
            OpenAudioMc.getInstance().getSocketModule().getSocket().emit("toplayer", p.serialize());
        }
    }

    public Boolean isAllowedConnection(String key) {
        return  (!isConnected && token.equals(key));
    }

    public void updateToken() {
        char[] chars = "abcdefghijklmnopqrstuvwxyz".toCharArray();
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 15; i++) {
            char c = chars[random.nextInt(chars.length)];
            sb.append(c);
        }
        String output = sb.toString();
        this.token = output;
    }

}
