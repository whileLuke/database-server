package edu.uob;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CommandParser {
    private List<String> tokens = new ArrayList<>();
    private final DBServer server;

    public CommandParser(DBServer server) {
        this.server = server;
    }

    public DBResponse parseCommand(List<String> tokensList) throws IOException {
        tokens = tokensList;
        if (tokens.size() < 2) return DBResponse.error("The command you've entered is not long enough (or is empty). Every command must be at least 2 words long (followed by a semi-colon).");
        DBCommand cmd;
        String cmdType = tokens.get(0).toUpperCase();
        switch (cmdType) {
            case "USE" -> cmd = parseUse();
            case "CREATE" -> cmd = parseCreate();
            case "DROP" -> cmd = parseDrop();
            case "ALTER" -> cmd = parseAlter();
            case "INSERT" -> cmd = parseInsert();
            case "SELECT" -> cmd = parseSelect();
            case "UPDATE" -> cmd = parseUpdate();
            case "JOIN" -> cmd = parseJoin();
            case "DELETE" -> cmd = parseDelete();
            default -> { return DBResponse.error("'" + cmdType + "' is an not a valid command type. Valid command types are: USE, CREATE, DROP, ALTER, INSERT, SELECT, UPDATE, JOIN, DELETE"); }
        }
        if(cmd == null) return DBResponse.error("Your command was unable to be executed - double check that it was formatted correctly");
        cmd.setServer(server);
        return cmd.query();
    }

    private boolean endsWithSemicolon() { return tokens.get(tokens.size() - 1).equals(";"); }

    private boolean isAsterisk(String name) { return name != null && name.equals("*"); }

    private DBCommand parseUse() {
        if (tokens.size() == 3 && endsWithSemicolon() && !isAsterisk(tokens.get(1))) {
            UseCommand useCmd = new UseCommand();
            useCmd.DBName = tokens.get(1);
            return useCmd;
        } else return null;
    }

    private DBCommand parseCreate() {
        if (tokens.size() < 4) return null;
        String createType = tokens.get(1).toLowerCase();
        if (createType.equals("database")) return createDatabase();
        if (createType.equals("table")) return createTable();
        return null;
    }

    private DBCommand createTable() {
        if (!endsWithSemicolon() || tokens.size() < 4) return null;

        String tableName = tokens.get(2);
        CreateTableCommand cmd = new CreateTableCommand();
        cmd.tableNames.add(tableName);
        if(tokens.size() == 4) {
            server.getTables().put(tableName, new Table(tableName, null));
            return cmd;
        }
        int start = tokens.indexOf("(");
        int end = tokens.lastIndexOf(")");
        if (start < 0 || end < 0 || end <= start) return null;

        List<String> columns = tokens.subList(start + 1, end);
        for (int i = 0; i < columns.size(); i += 2) {
            String column = columns.get(i);
            if (isAsterisk(column)) return null;
            cmd.columnNames.add(column);
            if (i + 1 < columns.size() && !columns.get(i + 1).equals(",") && !columns.get(i + 1).equals(")")) return null;
        }

        server.getTables().put(tableName, new Table(tableName, cmd.columnNames));
        return cmd;
    }

    private DBCommand createDatabase() {
        if (tokens.size() == 4 && endsWithSemicolon() && !isAsterisk(tokens.get(2))) {
            CreateDatabaseCommand cmd = new CreateDatabaseCommand();
            cmd.DBName = tokens.get(2);
            return cmd;
        } else return null;
    }

    private DBCommand parseDrop() {
        if (tokens.size() != 4 || !endsWithSemicolon()) return null;
        DropCommand cmd = new DropCommand();
        if (tokens.get(1).equalsIgnoreCase("database")) cmd.DBName = tokens.get(2);
        else if (tokens.get(1).equalsIgnoreCase("table")) cmd.tableNames.add(tokens.get(2));
        else return null;
        return cmd;
    }

    private DBCommand parseAlter() {
        if (tokens.size() < 6 || !endsWithSemicolon()) return null;
        String operation = tokens.get(3).toUpperCase();
        if (!(operation.equals("ADD") || operation.equals("DROP"))) return null;

        AlterCommand cmd = new AlterCommand();
        cmd.tableNames.add(tokens.get(2));
        cmd.commandType = operation;
        cmd.columnNames.add(tokens.get(4));
        return cmd;
    }

    private DBCommand parseInsert() {
        if (tokens.size() < 5 || !endsWithSemicolon() || !tokens.get(3).equalsIgnoreCase("values")) return null;

        InsertCommand cmd = new InsertCommand();
        cmd.tableNames.add(tokens.get(2));

        for (int i = 4; i < tokens.size() - 1; i++) { // -1 to skip final semicolon
            String token = tokens.get(i).replaceAll("[(),]", "").trim();
            if (!token.isEmpty()) cmd.values.add(token);
        }
        return cmd;
    }

    private DBCommand parseSelect() {
        if (tokens.size() < 4 || !endsWithSemicolon()) return null;

        SelectCommand cmd = new SelectCommand();
        int index = 1;
        while (index < tokens.size() && !tokens.get(index).equalsIgnoreCase("FROM")) {
            cmd.columnNames.add(tokens.get(index++));
            if (index < tokens.size() && tokens.get(index).equals(",")) index++;
        }
        if (index >= tokens.size()) return null;
        cmd.tableNames.add(tokens.get(++index));

        parseOptionalCondition(cmd, index + 1);
        return cmd;
    }

    private void parseOptionalCondition(DBCommand cmd, int startId) {
        if (startId < tokens.size() && tokens.get(startId).equalsIgnoreCase("WHERE")) {
            List<String> conditions = new ArrayList<>();
            StringBuilder condition = new StringBuilder();
            boolean inQuotes = false;

            for (int i = startId + 1; i < tokens.size(); i++) {
                String token = tokens.get(i);
                if (i == tokens.size() - 1 && token.endsWith(";")) {
                    token = token.substring(0, token.length() - 1);
                }

                if (token.startsWith("\"") && !token.endsWith("\"") || token.startsWith("'") && !token.endsWith("'")) {
                    inQuotes = true;
                } else if ((token.endsWith("\"") && !token.startsWith("\"")) || (token.endsWith("'") && !token.startsWith("'"))) {
                    inQuotes = false;
                }

                if (!inQuotes && (token.equalsIgnoreCase("AND") || token.equalsIgnoreCase("OR"))) {
                    if (!condition.isEmpty()) {
                        conditions.add(condition.toString().trim());
                        condition.setLength(0); // Clear for next condition
                    }
                    condition.append(token).append(" ");
                } else {
                    condition.append(token).append(" ");
                }
            }

            if (!condition.isEmpty()) {
                conditions.add(condition.toString().trim()); // Add last condition
            }

            System.out.println("[DEBUG] Extracted WHERE conditions: " + conditions);

            if (cmd instanceof SelectCommand) {
                ((SelectCommand) cmd).setConditions(conditions);
            } else if (cmd instanceof UpdateCommand) {
                ((UpdateCommand) cmd).setConditions(conditions);
            } else if (cmd instanceof DeleteCommand) {
                ((DeleteCommand) cmd).setConditions(conditions);
            }
        }
    }

    private DBCommand parseUpdate() {
        if (tokens.size() < 7 || !tokens.get(2).equalsIgnoreCase("SET")) return null;

        UpdateCommand cmd = new UpdateCommand();
        cmd.tableNames.add(tokens.get(1));

        int i = 3;
        while (i < tokens.size() && !tokens.get(i).equalsIgnoreCase("WHERE")) {
            cmd.columnNames.add(tokens.get(i));
            if (!tokens.get(++i).equals("=")) return null;
            cmd.values.add(tokens.get(++i).replace("'", ""));
            if (tokens.get(++i).equals(",")) i++;
        }

        parseOptionalCondition(cmd, i);
        return cmd;
    }

    private DBCommand parseJoin() {
        if (tokens.size() != 9) return null;

        JoinCommand cmd = new JoinCommand();
        if (!tokens.get(2).equalsIgnoreCase("AND") || !tokens.get(4).equalsIgnoreCase("ON") || !tokens.get(6).equalsIgnoreCase("AND"))
            return null;

        cmd.tableNames.add(tokens.get(1));
        cmd.tableNames.add(tokens.get(3));
        cmd.columnNames.add(tokens.get(5));
        cmd.columnNames.add(tokens.get(7).replace(";", ""));
        return cmd;
    }

    private DBCommand parseDelete() {
        if (tokens.size() < 5 || !tokens.get(1).equalsIgnoreCase("FROM")) return null;
        DeleteCommand cmd = new DeleteCommand();
        cmd.tableNames.add(tokens.get(2));
        parseOptionalCondition(cmd, 3);
        return cmd;
    }
}
