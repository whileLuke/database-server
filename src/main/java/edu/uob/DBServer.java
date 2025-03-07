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
    //private static final char END_OF_TRANSMISSION = 4;
    public String storageFolderPath;
    private String query;
    public static String currentDB;
    public static Map<String, Table> tables = new HashMap<String, Table>();
    String[] specialCharacters = {"(",")",",",";"};
    ArrayList<String> tokens = new ArrayList<String>();

    public static void main(String args[]) throws IOException {
        DBServer server = new DBServer();
        server.blockingListenOn(8889);
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
    //MAKE A COMMANDPARSER
    //COMPILER
    /**
    * KEEP this signature (i.e. {@code edu.uob.DBServer.handleCommand(String)}) otherwise we won't be
    * able to mark your submission correctly.
    *
    * <p>This method handles all incoming DB commands and carries out the required actions.
    */
    public String handleCommand(String command) throws IOException {
        // TODO implement your server logic here
        tokens.clear();
        query = command;
        setupQuery();
        CommandParser parser = new CommandParser();
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
        for (int i=0; i<tokens.size(); i++) System.out.println(tokens.get(i));
    }

    String[] tokenise(String input) {
        for (int i=0; i<specialCharacters.length ;i++) {
            input = input.replace(specialCharacters[i], " " + specialCharacters[i] + " ");
        }
        while (input.contains("  ")) input = input.replace("  ", " "); // Replace two spaces by one
        input = input.trim();
        return input.split(" ");
    }

    public boolean saveCurrentDB() throws IOException {
        if (currentDB == null) return false;
        File DBDirectory = new File(storageFolderPath, currentDB);
        if (!DBDirectory.exists() && !DBDirectory.mkdirs()) return false;
        for (Map.Entry<String, Table> entry : tables.entrySet()) {
            entry.getValue().saveToFile(DBDirectory.getPath(), entry.getKey());
        }
        return true;
    }

    public boolean loadTables(String DBName) {
        File DBDirectory = new File(storageFolderPath, DBName.toLowerCase());
        if (!DBDirectory.exists() || !DBDirectory.isDirectory()) return false;
        tables.clear();
        /*for (File tableFile : Objects.requireNonNull(DBDirectory.listFiles((dir, name) -> name.endsWith(".tab")))) {
            String tableName = tableFile.getName().replace(".tab", "");
            Table table = Table.loadFromFile(DBDirectory.getPath(), tableName);
            if (table != null) {
                tables.put(tableName.toLowerCase(), table); // Add table to memory
            }
        }*/
        File[] tableFiles = new File(DBDirectory.getPath()).listFiles();
        if (tableFiles != null) {
            for (File tableFile : tableFiles) {
                String tableName = tableFile.getName().replace(".tab", "");
                Table table = Table.loadFromFile(DBDirectory.getPath(), tableName);
                if (table != null) {
                    tables.put(tableName.toLowerCase(), table);
                }
            }
        }
        currentDB = DBName.toLowerCase();
        return true;
    }

    //protected boool?
    protected boolean useDatabase(String DBName) {
        File DBDirectory = new File(storageFolderPath, DBName.toLowerCase());
        if (!DBDirectory.exists() || !DBDirectory.isDirectory()) return false;
        return loadTables(DBName.toLowerCase());
    }

  /*  protected boolean createTable(String tableName, List<String> columnNames) throws IOException {
        System.out.println("Creating table " + tableName + "TEST");
        System.out.println(currentDB);
        if (currentDB == null) {
            System.out.println("[ERROR] No database selected.");
            return false;
        }
        /*System.out.println("Creating table " + tableName + "TEST2");
        File tableFile = new File(storageFolderPath + File.separator + currentDB, tableName.toLowerCase() + ".tab");
        if (tableFile.exists()) {
            System.out.println("[ERROR] Table already exists.");
            return false;
        }
        System.out.println("Creating table " + tableName + "TEST3");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(tableFile))) {
            writer.write("id");
            for (String column : columnNames) {
                System.out.println("Creating table " + tableName + "TEST11");
                writer.write("\t" + column);
            }
            writer.newLine();
            Table newTable = new Table(columnNames);
            tables.put(tableName.toLowerCase(), newTable);
            System.out.println("[OK] Table '" + tableName + "' created.");
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        if (tables.containsKey(tableName.toLowerCase())) {
            return false;
        }
        if (!columnNames.contains("id")) {
            columnNames.add(0, "id");
        }
        tables.put(tableName.toLowerCase(), new Table(columnNames));
        saveCurrentDB();
        return true;
    }
*/
    protected boolean createDatabase(String DBName) {
        if (DBName == null) return false;
        File DBDirectory = new File(storageFolderPath, DBName.toLowerCase());
        if (DBDirectory.exists()) return false;
        return DBDirectory.mkdirs();
        //write it to disk
        //return true;
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

        //  === Methods below handle networking aspects of the project - you will not need to change these ! ===

    public void blockingListenOn(int portNumber) throws IOException {
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

    private void blockingHandleConnection(ServerSocket serverSocket) throws IOException {
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
