package com.gold.hamrahvpn.util;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

/**
 * by Mehrab
 */
public class FileUtil {

    public static String readAssetFile(Context context, String fileName) {
//        StringBuilder stringBuilder = new StringBuilder();
//            InputStream inputStream = context.getAssets().open(fileName);
//            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
//            String line;
//            while ((line = bufferedReader.readLine()) != null) {
//                stringBuilder.append(line);
//            }
//            bufferedReader.close();
//        return stringBuilder.toString();
        AssetManager assetManager = context.getAssets();
        StringBuilder stringBuilder = new StringBuilder();

        try {
            InputStream inputStream = assetManager.open(fileName);
            Scanner scanner = new Scanner(inputStream);
            while (scanner.hasNextLine()) {
                stringBuilder.append(scanner.nextLine()).append("\n");
            }
            scanner.close();
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return stringBuilder.toString();
    }
}
