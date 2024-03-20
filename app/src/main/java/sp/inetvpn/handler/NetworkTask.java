package sp.inetvpn.handler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NetworkTask {

    public interface NetworkTaskListener {
        void onNetworkTaskCompleted(String result);

        void onNetworkTaskFailed(Exception e);
    }

    public static void executeTask(String urlString, NetworkTaskListener listener) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                URL url = new URL(urlString);
                URLConnection connection = url.openConnection();
                InputStream inputStream = connection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder content = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line).append("\n");
                }
                reader.close();
                String result = content.toString();
                executor.shutdown(); // تمام کردن اجرای تسک
                listener.onNetworkTaskCompleted(result);
            } catch (IOException e) {
                listener.onNetworkTaskFailed(e);
            }
        });
    }
}
