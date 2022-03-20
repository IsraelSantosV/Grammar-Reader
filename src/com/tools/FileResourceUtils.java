package com.tools;

import sun.misc.IOUtils;

import org.json.*;
import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import com.opencsv.CSVWriter;

public class FileResourceUtils {

    private static FileResourceUtils m_Instance;

    public static FileResourceUtils getInstance() {
        if(m_Instance == null){
            m_Instance = new FileResourceUtils();
        }

        return m_Instance;
    }

    private FileResourceUtils() { }

    public static String getFileAsString(String filePath){
        try(BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
                sb.append(line);
                sb.append(System.lineSeparator());
                line = br.readLine();
            }

            return sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "";
    }

    public static JSONObject readJson(String jsonPath){
        String jsonString = getInstance().getFileFromResourceAsString(jsonPath);
        return new JSONObject(jsonString);
    }

    public static void printInputStream(InputStream input){
        try (InputStreamReader streamReader = new InputStreamReader(input, StandardCharsets.UTF_8);
             BufferedReader reader = new BufferedReader(streamReader)) {

            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void printFile(File file) {
        List<String> lines;
        try {
            lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
            lines.forEach(System.out::println);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public InputStream getFileFromResourceAsStream(String fileName){
        String targetFileName = "resources/" + fileName;
        ClassLoader classLoader = getClass().getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream(targetFileName);

        if(inputStream == null){
            throw new IllegalArgumentException("File not found: " + targetFileName);
        }

        return inputStream;
    }

    public String getFileFromResourceAsString(String fileName){
        String targetFileName = "resources/" + fileName;
        ClassLoader classLoader = getClass().getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream(targetFileName);

        if(inputStream == null){
            throw new IllegalArgumentException("File not found: " + targetFileName);
        }

        Scanner scanner = new Scanner(inputStream).useDelimiter("\\A");
        return scanner.hasNext() ? scanner.next() : "";
    }

    public File getFileFromResource(String fileName) throws URISyntaxException {
        String targetFileName = "resources/" + fileName;
        ClassLoader classLoader = getClass().getClassLoader();
        URL resource = classLoader.getResource(targetFileName);
        if(resource == null){
            throw new IllegalArgumentException("File not found: " + targetFileName);
        }

        return new File(resource.toURI());
    }

    public static void writeMatrixCSV(String[] header, List<String[]> linesValues, String fileName) throws IOException {
        Writer writer = Files.newBufferedWriter(Paths.get(fileName + ".csv"));
        CSVWriter csvWriter = new CSVWriter(writer);

        csvWriter.writeNext(header);
        csvWriter.writeAll(linesValues);

        csvWriter.flush();
        writer.close();
    }

}
