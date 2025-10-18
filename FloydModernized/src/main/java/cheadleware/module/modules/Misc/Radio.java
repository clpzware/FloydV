//package cheadleware.module.modules.Misc;
//
//import cheadleware.event.EventTarget;
//import cheadleware.module.Module;
//import cheadleware.property.properties.FloatProperty;
//import cheadleware.property.properties.ModeProperty;
//import cheadleware.util.sound.RadioPlayer;
//
//public class Radio extends Module {
//    private final RadioPlayer radioPlayer;
//    public final FloatProperty volume;
//    public final ModeProperty radios;
//
//    public Radio() {
//        super("Radio", false);
//        this.radioPlayer = new RadioPlayer();
//        this.volume = new FloatProperty("Volume", 50.0F, 0.0F, 100.0F);
//        this.radios = new ModeProperty("RadioChannels", 0, new String[] {
//                "I love Radio",
//                "I love 2 Dance",
//                "I love ChillHop",
//                "I l Deutschrap",
//                "I l Greatest Hits",
//                "I love Hardstyle",
//                "I love Hip Hop",
//                "I love Mashup",
//                "I love The Club"
//        });
//    }
//
//    public void onEnable() {
//        this.radioPlayer.setCurrent("");
//        this.playMusic();
//    }
//
//    public void onDisable() {
//        if (this.radioPlayer != null) {
//            this.radioPlayer.stop();
//            this.radioPlayer.setCurrent("");
//        }
//    }
//
//    private void playMusic() {
//        if (this.radioPlayer != null && this.radios.getModeString().equals(this.radioPlayer.getCurrent())) {
//            return;
//        }
//        if (this.radioPlayer != null) {
//            this.radioPlayer.stop();
//        }
//        String selected = this.radios.getModeString();
//        switch (selected) {
//            case "I love Radio":
//                this.radioPlayer.start("https://streams.ilovemusic.de/iloveradio1.mp3", this.radios.getModeString());
//                break;
//            case "I love 2 Dance":
//                this.radioPlayer.start("https://streams.ilovemusic.de/iloveradio2.mp3", this.radios.getModeString());
//                break;
//            case "I love ChillHop":
//                this.radioPlayer.start("https://streams.ilovemusic.de/iloveradio17.mp3", this.radios.getModeString());
//                break;
//            case "I l Deutschrap":
//                this.radioPlayer.start("https://streams.ilovemusic.de/iloveradio6.mp3", this.radios.getModeString());
//                break;
//            case "I l Greatest Hits":
//                this.radioPlayer.start("https://streams.ilovemusic.de/iloveradio16.mp3", this.radios.getModeString());
//                break;
//            case "I love Hardstyle":
//                this.radioPlayer.start("https://streams.ilovemusic.de/iloveradio21.mp3", this.radios.getModeString());
//                break;
//            case "I love Hip Hop":
//                this.radioPlayer.start("https://streams.ilovemusic.de/iloveradio3.mp3", this.radios.getModeString());
//                break;
//            case "I love Mashup":
//                this.radioPlayer.start("https://streams.ilovemusic.de/iloveradio5.mp3", this.radios.getModeString());
//                break;
//            case "I love The Club":
//                this.radioPlayer.start("https://streams.ilovemusic.de/iloveradio20.mp3", this.radios.getModeString());
//                break;
//        }
//    }
//}