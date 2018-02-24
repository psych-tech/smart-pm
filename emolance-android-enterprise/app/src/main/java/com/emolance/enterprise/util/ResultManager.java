package com.emolance.enterprise.util;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.emolance.enterprise.R;
import com.emolance.enterprise.data.LevelResult;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.ReferenceType;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import javax.xml.transform.Result;

/**
 * Created by yusun on 2/24/18.
 */

public class ResultManager {

    private Map<String, LevelResult> resultMap;
    private static ResultManager instance;

    public static ResultManager getInstance(Context context) {
        if (instance == null) {
            try {
                InputStream is = context.getResources().openRawResource(R.raw.results);
                Writer writer = new StringWriter();
                char[] buffer = new char[1024];
                try {
                    Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                    int n;
                    while ((n = reader.read(buffer)) != -1) {
                        writer.write(buffer, 0, n);
                    }
                } finally {
                    is.close();
                }

                String jsonString = writer.toString();
                instance = new ResultManager();
                ObjectMapper objectMapper = new ObjectMapper();
                instance.resultMap = objectMapper.readValue(jsonString, new TypeReference<Map<String, LevelResult>>(){});
            } catch (Exception e) {
                Log.e("JSON", "Failed to parse the json file.");
            }
        }
        return instance;
    }

    public LevelResult getLevelResult(int level) {
        return resultMap.get(Integer.toString(level));
    }

    public LevelResult getLevelResult(String level) {
        return resultMap.get(level);
    }
}
