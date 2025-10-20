//package fr.ambient.external.resource;
//
//import fr.ambient.util.InstanceAccess;
//
//import java.io.BufferedInputStream;
//import java.io.File;
//import java.io.FileOutputStream;
//import java.io.InputStream;
//import java.net.HttpURLConnection;
//import java.net.URL;
//
//public class ExternalResourceDownloader implements InstanceAccess {
//
//    private static final String ip = "";
//
//    public ExternalResourceDownloader(){
//        // Erm What The Sigma
//    }
//
//
//    public File downloadExternalData(String id){
//        File file = new File(mc.mcDataDir, "/ambient/external");
//        file.mkdirs();
//        File dlFile = new File(file, id);
//        downloadFile(ip + "?reqid=" + id, dlFile);
//        return dlFile;
//    }
//
//    public static void downloadFile(String fileURL, File savePath) {
//        HttpURLConnection conn = null;
//        try {
//            URL url = new URL(fileURL);
//            conn = (HttpURLConnection) url.openConnection();
//            conn.setRequestMethod("GET");
//            int rc = conn.getResponseCode();
//            if (rc == HttpURLConnection.HTTP_OK) {
//                InputStream is = new BufferedInputStream(conn.getInputStream());
//                FileOutputStream os = new FileOutputStream(savePath.getAbsoluteFile());
//                byte[] buffer = new byte[8192];
//                int br;
//                while ((br = is.read(buffer)) != -1) {
//                    os.write(buffer, 0, br);
//                }
//                os.close();
//                is.close();
//            } else {
//                System.out.println("Failed HTTP DL : " + rc);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            if (conn != null) {
//                conn.disconnect();
//            }
//        }
//    }
//}
