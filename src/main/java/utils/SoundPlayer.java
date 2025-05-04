package utils;

import javafx.scene.media.AudioClip;
import java.net.URL;

public class SoundPlayer {
    private static AudioClip clickSound;

    static {
        try {
            URL resource = SoundPlayer.class.getResource("/sounds/click.wav");
            if (resource != null) {
                clickSound = new AudioClip(resource.toString());
            }
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement du son: " + e.getMessage());
        }
    }

    public static void playButtonClickSound() {
        try {
            if (clickSound != null) {
                clickSound.play();
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de la lecture du son: " + e.getMessage());
        }
    }
}
