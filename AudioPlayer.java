import java.io.File;
import java.io.IOException;
import java.net.URL;
import javax.sound.sampled.*; 

public class AudioPlayer {

    private Clip clip;
    private String audioFilePath;

    public AudioPlayer(String audioFilePath) {
        this.audioFilePath = audioFilePath;
    }

    public void play() {
        try {
            URL audioURL = getClass().getResource("/" + audioFilePath);
            AudioInputStream audioStream;

            if (audioURL != null) {
                audioStream = AudioSystem.getAudioInputStream(audioURL);
            } else {
                File audioFile = new File(audioFilePath);
                if (!audioFile.exists()) {
                    System.err.println("ERRO: Arquivo de áudio não encontrado. Verifique o caminho e o nome do arquivo: " + audioFilePath);
                    return;
                }
                audioStream = AudioSystem.getAudioInputStream(audioFile);
            }

            clip = AudioSystem.getClip();
            clip.open(audioStream);

            clip.loop(Clip.LOOP_CONTINUOUSLY);
            clip.start();

        } catch (UnsupportedAudioFileException e) {
            System.err.println("Formato de áudio não suportado (apenas WAV, AU, AIFF): " + e.getMessage());
            e.printStackTrace();
        } catch (LineUnavailableException e) {
            System.err.println("Linha de áudio não disponível: " + e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("Erro de E/S ao carregar o áudio: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Ocorreu um erro inesperado ao tentar tocar o áudio: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void stop() {
        if (clip != null && clip.isRunning()) {
            clip.stop();
            clip.close();
        }
    }
}