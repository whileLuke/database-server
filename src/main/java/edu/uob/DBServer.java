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
    public static String storageFolderPath;
    private final DBStorage storage;
    private final Tokeniser tokeniser;
    //private String query;
    public static String currentDB;
    public static Map<String, DBTable> tables = new HashMap<String, DBTable>();
    //String[] specialCharacters = {"(",")",",",";","!",">","<","="};
    //ArrayList<String> tokens = new ArrayList<String>();

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
        storage = new DBStorage(storageFolderPath);
        tokeniser = new Tokeniser();
    }

    /**
    * KEEP this signature (i.e. {@code edu.uob.DBServer.handleCommand(String)}) otherwise we won't be
    * able to mark your submission correctly.
    *
    * <p>This method handles all incoming DB commands and carries out the required actions.
    */
    public String handleCommand(String command) throws IOException {
        if (command == null || command.isEmpty()) {
            return DBResponse.error("Empty command.").toString();
        }

        if (!command.endsWith(";")) {
            return DBResponse.error("Command must end with a semicolon (';').").toString();
        }

        List<String> tokens = tokeniser.tokenise(command);
        CommandParser parser = new CommandParser(this);
        try {
            DBResponse response = parser.parseCommand(tokens);
            return response.toString();
        } catch (Exception e) {
            return DBResponse.error("Command processing error: " + e.getMessage()).toString();
        }
    }

    public boolean saveCurrentDB() throws IOException {
        if (currentDB == null) return false;
        return storage.saveTables(tables, currentDB);
    }

    public boolean loadTables(String dbName) throws IOException {
        if (!storage.databaseExists(dbName)) return false;
        tables = storage.loadTables(dbName);
        currentDB = dbName.toLowerCase();
        return true;
    }

    public boolean useDatabase(String dbName) throws IOException {
        if (!storage.databaseExists(dbName)) return false;
        return loadTables(dbName);
    }

    public boolean createDatabase(String dbName) {
        if (dbName == null) return false;
        return storage.createDatabase(dbName);
    }

    public boolean deleteDatabase(String dbName) {
        if (currentDB != null && currentDB.equalsIgnoreCase(dbName)) {
            tables.clear();
            currentDB = null;
        }
        return storage.deleteDatabase(dbName);
    }

    public String getCurrentDB() {
        return currentDB;
    }

   // public void setCurrentDB(String dbName) { this.currentDB = dbName; }

    public Map<String, DBTable> getTables() { return tables; }

    public String getStorageFolderPath() {
        return storageFolderPath;
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
