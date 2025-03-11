package edu.uob;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.util.*;

//need a command parser and a command lexer

/** This class implements the DB server. */
public class DBServer {

    private static final char END_OF_TRANSMISSION = 4;
    public static final String FILE_EXTENSION = ".tab";
    public String storageFolderPath;
    private String query;
    public static String currentDB;
    public static Map<String, Table> tables = new HashMap<String, Table>();
    String[] specialCharacters = {"(",")",",",";","!",">","<","="};
    ArrayList<String> tokens = new ArrayList<String>();

    public static void main(String args[]) throws Exception {
        DBServer server = new DBServer();
        server.blockingListenOn(8888);
    }

    /**
    * KEEP this signature otherwise we won't be able to mark your submission correctly.
    */
    public DBServer() {
        storageFolderPath = Paths.get("databases").toAbsolutePath().toString();
        try {
            // Create the database storage folder if it doesn't already exist !
            Files.createDirectories(Paths.get(storageFolderPath));
        } catch(IOException ioe) {
            System.out.println("Can't seem to create database storage folder " + storageFolderPath);
        }
    }

    /**
    * KEEP this signature (i.e. {@code edu.uob.DBServer.handleCommand(String)}) otherwise we won't be
    * able to mark your submission correctly.
    *
    * <p>This method handles all incoming DB commands and carries out the required actions.
    */
    public String handleCommand(String command) throws IOException {
        if (command == null || command.isEmpty()) return "[ERROR] Empty command.";
        if (!command.endsWith(";")) return "[ERROR] Command must end with a semicolon (';').";
        tokens.clear();
        query = command;
        setupQuery();
        CommandParser parser = new CommandParser(this);
        System.out.println("tables is looking like" + tables);
        return parser.parseCommand(tokens);
    }

    void setupQuery() {
        String[] fragments = query.split("'");
        for (int i=0; i<fragments.length; i++) {
            if (i%2 != 0) tokens.add("'" + fragments[i] + "'");
            else {
                String[] nextBatchOfTokens = tokenise(fragments[i]);
                tokens.addAll(Arrays.asList(nextBatchOfTokens));
            }
        }
        for (String token : tokens) System.out.println(token);
    }

    String[] tokenise(String input) {
        System.out.println("[DEBUG] Raw input before tokenization: " + input);
        for (String specialCharacter : specialCharacters) {
            input = input.replace(specialCharacter, " " + specialCharacter + " ");
        }
        while (input.contains("  ")) input = input.replace("  ", " "); // Replace double spaces with single
        input = input.trim();
        String[] initialTokens = input.split(" ");
        ArrayList<String> tokensList = tokeniseCompoundOperators(initialTokens);
        return tokensList.toArray(new String[0]);
    }

    private static ArrayList<String> tokeniseCompoundOperators(String[] initialTokens) {
        ArrayList<String> tokensList = new ArrayList<>();
        for (int i = 0; i < initialTokens.length; i++) {
            if (i < initialTokens.length - 1 &&
                    (initialTokens[i].equals(">") || initialTokens[i].equals("<") || initialTokens[i].equals("=") || initialTokens[i].equals("!")) &&
                    initialTokens[i+1].equals("=")) {
                tokensList.add(initialTokens[i] + initialTokens[i+1]);
                i++;
            } else tokensList.add(initialTokens[i]);
        }
        return tokensList;
    }

    public boolean saveCurrentDB() throws IOException {
        if (currentDB == null) return false;
        File DBDirectory = new File(storageFolderPath, currentDB);
        if (!DBDirectory.exists() && !DBDirectory.mkdirs()) return false;
        for (Map.Entry<String, Table> entry : tables.entrySet()) {
            boolean success = TableStorage.saveToFile(entry.getValue(), DBDirectory.getPath());
            if (!success) return false;
            File tableFile = new File(DBDirectory, entry.getKey() + ".tab");
            System.out.println("[DEBUG] Saved file contents: " + new String(Files.readAllBytes(tableFile.toPath())));
        }
        System.out.println("[SUCCESS] Database saved!");
        return true;
    }

    public boolean loadTables(String DBName) throws IOException {
        File DBDirectory = new File(storageFolderPath, DBName.toLowerCase());
        if (!DBDirectory.exists() || !DBDirectory.isDirectory()) return false;
        tables.clear();
        File[] tableFiles = new File(DBDirectory.getPath()).listFiles();
        if (tableFiles != null) {
            for (File tableFile : tableFiles) {
                String tableName = tableFile.getName().replace(FILE_EXTENSION, "");
                Table table = TableStorage.loadFromFile(DBDirectory.getPath(), tableName);
                if (table != null) {
                    tables.put(tableName.toLowerCase(), table);
                }
            }
        }
        currentDB = DBName.toLowerCase();
        return true;
    }

    public boolean useDatabase(String DBName) throws IOException {
        File DBDirectory = new File(storageFolderPath, DBName.toLowerCase());
        if (!DBDirectory.exists() || !DBDirectory.isDirectory()) return false;
        return loadTables(DBName.toLowerCase());
    }

    public boolean createDatabase(String DBName) {
        if (DBName == null) return false;
        File DBDirectory = new File(storageFolderPath, DBName.toLowerCase());
        if (DBDirectory.exists()) return false;
        return DBDirectory.mkdirs();
    }

    public boolean deleteDatabase(String DBName) {
        File DBDirectory = new File(storageFolderPath, DBName.toLowerCase());
        if (!DBDirectory.exists() || !DBDirectory.isDirectory()) return false;
        if (currentDB != null && currentDB.equalsIgnoreCase(DBName)) {
            tables.clear();
            currentDB = null;
        }
        return deleteDirectory(DBDirectory);
    }

    private boolean deleteDirectory(File directory) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    if (!deleteDirectory(file)) return false;
                } else if (!file.delete()) return false;
            }
        }
        return directory.delete();
    }

    // Getter methods for properties that DBCommand needs
    public String getCurrentDB() {
        return currentDB;
    }

    public void setCurrentDB(String dbName) {
        this.currentDB = dbName;
    }

    public Map<String, Table> getTables() {
        return tables;
    }

        //  === Methods below handle networking aspects of the project - you will not need to change these ! ===

    public void blockingListenOn(int portNumber) throws IOException, Exception {
        try (ServerSocket s = new ServerSocket(portNumber)) {
            System.out.println("Server listening on port " + portNumber);
            while (!Thread.interrupted()) {
                try {
                    blockingHandleConnection(s);
                } catch (IOException e) {
                    System.err.println("Server encountered a non-fatal IO error:");
                    e.printStackTrace();
                    System.err.println("Continuing...");
                }
            }
        }
    }

    private void blockingHandleConnection(ServerSocket serverSocket) throws IOException, Exception {
        try (Socket s = serverSocket.accept();
        BufferedReader reader = new BufferedReader(new InputStreamReader(s.getInputStream()));
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()))) {

            System.out.println("Connection established: " + serverSocket.getInetAddress());
            while (!Thread.interrupted()) {
                String incomingCommand = reader.readLine();
                System.out.println("Received message: " + incomingCommand);
                String result = handleCommand(incomingCommand);
                writer.write(result);
                writer.write("\n" + END_OF_TRANSMISSION + "\n");
                writer.flush();
            }
        }
    }
}
