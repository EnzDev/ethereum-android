package com.sqli.blockchain.android_geth;

import android.content.Context;
import android.content.res.AssetManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by gunicolas on 3/08/16.
 */
public abstract class Utils {


    /*
        Save given asset file to storage.
        If file already exists it override it.
     */
    public static void saveAssetOnStorage(Context context, String assetFilename, String storagePath) throws Exception {
        AssetManager asset = context.getAssets();
        InputStream in = asset.open(assetFilename);
        String filePath = storagePath + "/" + assetFilename;
        File f = new File(filePath);
        boolean exists = f.exists();
        if( exists ){
            f.delete();
        }
        f.createNewFile();
        OutputStream out = new FileOutputStream(filePath);
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
        //TODO try finally or closeable
        in.close();
        in = null;
        out.flush();
        out.close();
        out = null;

    }

}
