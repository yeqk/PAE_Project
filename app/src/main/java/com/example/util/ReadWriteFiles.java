package com.example.util;



import android.content.Context;
import android.util.Log;

import com.example.pae_project.R;

import org.json.JSONException;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

public class ReadWriteFiles {

    //copy the content of the JSONArray json to a geojson file in local storage with name destName
    public static void copyToGeoJsonFile(JSONArray json, String destName, Context ctx) {
        try{

        // loop array
        JSONArray msg = json;

        JSONObject object3g = new JSONObject();
        JSONArray features3g = new JSONArray();


        object3g.put("type", "FeatureCollection");
        for (int i = 0; i < msg.length(); i++) {
            System.out.println(msg.get(i));
            JSONObject aux = (JSONObject) msg.get(i);

            JSONObject feature3g = new JSONObject();
            feature3g.put("type", "Feature");

            //properties
            JSONObject properties = new JSONObject();
            properties.put("id", aux.get("_id"));
            properties.put("description", aux.get("description"));

            feature3g.put("properties", properties);

            //coordinates
            JSONArray coordinates = new JSONArray();


            coordinates.put(aux.get("longitude"));
            coordinates.put(aux.get("latitude"));

            JSONObject geometry = new JSONObject();
            geometry.put("type", "Point");
            geometry.put("coordinates", coordinates);

            feature3g.put("geometry", geometry);

            features3g.put(feature3g);
        }

        object3g.put("features", features3g);

        String str = object3g.toString();
        InputStream is = new ByteArrayInputStream(str.getBytes());
        File outFile = new File(ctx.getFilesDir(), destName);

            OutputStream out = new FileOutputStream(outFile);
            byte[] buffer = new byte[1024];
            int read;
            while((read = is.read(buffer)) != -1){
                out.write(buffer, 0, read);
            }
            is.close();
            is = null;
            out.flush();
            out.close();
            out = null;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

    //copy json to file
    public static void jsonToFile(JSONArray json, String destName, Context ctx) {
        String str = json.toString();
        InputStream is = new ByteArrayInputStream(str.getBytes());
        File outFile = new File(ctx.getFilesDir(), destName);

        OutputStream out = null;
        try {
            out = new FileOutputStream(outFile);
            byte[] buffer = new byte[1024];
            int read;
            while((read = is.read(buffer)) != -1){
                out.write(buffer, 0, read);
            }
            is.close();
            is = null;
            out.flush();
            out.close();
            out = null;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static String read_file(Context context, String filename) {
        try {
            FileInputStream fis = context.openFileInput(filename);
            InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
            BufferedReader bufferedReader = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            return sb.toString();
        } catch (FileNotFoundException e) {
            return "";
        } catch (UnsupportedEncodingException e) {
            return "";
        } catch (IOException e) {
            return "";
        }
    }

    //concatenate content
    public static void insettToJsonArrayFile(JSONObject json, String destName, Context ctx) {
        if (isFilePresent(destName, ctx)) {
            String oldContent = read_file(ctx, destName);
            try {
                JSONArray oldJsonArray = new JSONArray(oldContent);
                oldJsonArray.put(json);
                jsonToFile(oldJsonArray,"wifis.json",ctx);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }


    public static boolean isFilePresent(String fileName, Context context) {
        String path = context.getFilesDir().getAbsolutePath() + "/" + fileName;
        Log.d("fileDir:", path);
        File file = new File(path);
        return file.exists();
    }

    //save wifis to "wifis.json"
    public static void copyToJsonFileWifi(JSONArray response, String destName, Context ctx) {
        try{



            String str = response.toString();
            InputStream is = new ByteArrayInputStream(str.getBytes());
            File outFile = new File(ctx.getFilesDir(), destName);

            OutputStream out = new FileOutputStream(outFile);
            byte[] buffer = new byte[1024];
            int read;
            while((read = is.read(buffer)) != -1){
                out.write(buffer, 0, read);
            }
            is.close();
            is = null;
            out.flush();
            out.close();
            out = null;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
