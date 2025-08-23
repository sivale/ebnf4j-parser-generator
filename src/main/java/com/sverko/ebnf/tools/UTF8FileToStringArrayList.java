package com.sverko.ebnf.tools;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class UTF8FileToStringArrayList {
    public static List<String> loadFileIntoStringList(Path filename) throws IOException {
        List<String> stringList = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(
            new InputStreamReader(new FileInputStream(filename.toString()), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                stringList.add(line);
            }
        }
        return stringList;
    }
}
