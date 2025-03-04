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
    private String storageFolderPath;
    private String query;
    private String currentDB;
    private Map<String, Table> tables = new HashMap<>();
    String[] specialCharacters = {"(",")",",",";"};
    ArrayList<String> tokens = new ArrayList<String>();
    public static void main(String args[]) throws IOException {
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
    //MAKE A COMMANDPARSER
    //COMPILER
    /**
    * KEEP this signature (i.e. {@code edu.uob.DBServer.handleCommand(String)}) otherwise we won't be
    * able to mark your submission correctly.
    *
    * <p>This method handles all incoming DB commands and carries out the required actions.
    */
    public String handleCommand(String command) {
        // TODO implement your server logic here
        query = command;
        setupQuery();
        CommandParser parser = new CommandParser();
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
    //protected boool?
    protected boolean useDatabase(String DBName) {
        File DBDirectory = new File(storageFolderPath, DBName.toLowerCase());
        if (!DBDirectory.exists() || !DBDirectory.isDirectory()) return false;
        currentDB = DBName.toLowerCase();
        return true;
    }

    protected boolean createTable(String tableName, List<String> columnNames) {
        if (currentDB == null) {
            System.out.println("[ERROR] No database selected.");
            return false;
        }
        File tableFile = new File(storageFolderPath + File.separator + currentDB, tableName.toLowerCase() + ".tab");
        if (tableFile.exists()) {
            System.out.println("[ERROR] Table already exists.");
            return false;
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(tableFile))) {
            writer.write("id");
            for (String column : columnNames) {
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
    }

    protected boolean createDatabase(String DBName) {
        if (DBName == null) return false;
        File DBDirectory = new File(storageFolderPath, DBName.toLowerCase());
        if (DBDirectory.exists()) return false;
        return DBDirectory.mkdirs();
        //write it to disk
        //return true;
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
