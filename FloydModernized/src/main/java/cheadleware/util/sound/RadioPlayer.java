//package cheadleware.util.sound;
//
//import java.io.BufferedInputStream;
//import java.io.IOException;
//import java.net.URL;
//import javax.sound.sampled.*;
//
//import cheadleware.Cheadleware;
//import cheadleware.module.ModuleManager;
//import cheadleware.module.modules.Misc.Radio;
//import cheadleware.util.TimerUtil;
//
//public class RadioPlayer {
//    private Thread thread;
//    private SourceDataLine line;
//    private AudioInputStream audioStream;
//    private String current;
//    private final TimerUtil timeHelper;
//    private FloatControl volumeControl;
//    private volatile boolean running;
//
//    public RadioPlayer() {
//        this.line = null;
//        this.timeHelper = new TimerUtil();
//        this.running = false;
//    }
//
//    public void start(final String url, final String current) {
//        if (this.timeHelper.hasTimeElapsed(2000L)) {
//            this.stop();
//
//            this.running = true;
//            this.thread = new Thread(() -> {
//                try {
//                    URL streamUrl = new URL(url);
//                    AudioInputStream rawStream = AudioSystem.getAudioInputStream(
//                            new BufferedInputStream(streamUrl.openStream())
//                    );
//
//                    AudioFormat baseFormat = rawStream.getFormat();
//                    AudioFormat decodedFormat = new AudioFormat(
//                            AudioFormat.Encoding.PCM_SIGNED,
//                            baseFormat.getSampleRate(),
//                            16,
//                            baseFormat.getChannels(),
//                            baseFormat.getChannels() * 2,
//                            baseFormat.getSampleRate(),
//                            false
//                    );
//
//                    this.audioStream = AudioSystem.getAudioInputStream(decodedFormat, rawStream);
//                    DataLine.Info info = new DataLine.Info(SourceDataLine.class, decodedFormat);
//                    this.line = (SourceDataLine) AudioSystem.getLine(info);
//                    this.line.open(decodedFormat);
//
//                    if (this.line.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
//                        this.volumeControl = (FloatControl) this.line.getControl(FloatControl.Type.MASTER_GAIN);
//                        setVolume();
//                    }
//
//                    this.line.start();
//
//                    byte[] buffer = new byte[4096];
//                    int bytesRead;
//                    while (this.running && (bytesRead = this.audioStream.read(buffer, 0, buffer.length)) != -1) {
//                        this.line.write(buffer, 0, bytesRead);
//                    }
//
//                } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
//                    e.printStackTrace();
//                } finally {
//                    cleanup();
//                }
//            });
//
//            this.thread.start();
//            this.timeHelper.reset();
//            this.current = "" + current;
//        }
//    }
//
//    public void setVolume() {
//        if (this.volumeControl != null && this.thread != null) {
//            float volumeValue = (float) (new Radio().volume.getValue() * 0.8600000143051147 - 80.0);
//            float min = this.volumeControl.getMinimum();
//            float max = this.volumeControl.getMaximum();
//            this.volumeControl.setValue(Math.max(min, Math.min(max, volumeValue)));
//        }
//    }
//
//    public void stop() {
//        this.running = false;
//
//        if (this.thread != null) {
//            this.thread.interrupt();
//            try {
//                this.thread.join(1000);
//            } catch (InterruptedException e) {
//                Thread.currentThread().interrupt();
//            }
//            this.thread = null;
//        }
//
//        cleanup();
//    }
//
//    private void cleanup() {
//        if (this.line != null) {
//            this.line.drain();
//            this.line.stop();
//            this.line.close();
//            this.line = null;
//        }
//
//        if (this.audioStream != null) {
//            try {
//                this.audioStream.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            this.audioStream = null;
//        }
//
//        this.volumeControl = null;
//    }
//
//    public String getCurrent() {
//        return this.current;
//    }
//
//    public void setCurrent(final String current) {
//        this.current = current;
//    }
//
//    public boolean isPlaying() {
//        return this.running && this.line != null && this.line.isActive();
//    }
//}