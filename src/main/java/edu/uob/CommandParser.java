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

    public String parseCommand(List<String> tokensList) throws IOException {
        tokens = tokensList;
        if (tokens.size() < 2) return "[ERROR] The command you've entered is not long enough (or is empty).";
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
            default -> { return "[ERROR] '" + cmdType + "' is not a valid command type."; }
        }
        if(cmd == null) return "[ERROR] Your command was not formatted correctly.";
        cmd.setServer(server);
        return cmd.query();
    }

    private boolean endsWithSemicolon() { return tokens.get(tokens.size() - 1).equals(";"); }

    private boolean isAsterisk(String name) { return name.equals("*"); }

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
        if (createType.equals("database")) return parseCreateDatabase();
        if (createType.equals("table")) return parseCreateTable();
        return null;
    }

    private DBCommand parseCreateTable() {
        if (tokens.size() < 4 || !endsWithSemicolon()) return null;
        String tableName = tokens.get(2);
        CreateTableCommand cmd = new CreateTableCommand();
        cmd.tableNames.add(tableName);
        if (tokens.size() == 4) return cmd;
        int openingBracket = tokens.indexOf("(");
        int closingBracket = tokens.lastIndexOf(")");
        if (openingBracket < 0 || closingBracket < 0 || closingBracket <= openingBracket) return null;
        if (!parseTableColumns(openingBracket, closingBracket, cmd)) return null;
        //server.getTables().put(tableName, new DBTable(tableName, cmd.columnNames));
        return cmd;
    }

    private boolean parseTableColumns(int openingBracket, int closingBracket, CreateTableCommand cmd) {
        List<String> columns = tokens.subList(openingBracket + 1, closingBracket);
        for (int i = 0; i < columns.size(); i += 2) {
            String column = columns.get(i);
            if (isAsterisk(column)) return false;
            cmd.columnNames.add(column);
            if (i + 1 < columns.size() && !columns.get(i + 1).equals(",") && !columns.get(i + 1).equals(")"))
                return false;
        }
        return true;
    }

    private DBCommand parseCreateDatabase() {
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
        if (tokens.size() < 5 || !endsWithSemicolon() ||
                !tokens.get(3).equalsIgnoreCase("values")) return null;
        InsertCommand cmd = new InsertCommand();
        cmd.tableNames.add(tokens.get(2));
        getValuesFromTokens(cmd);
        return cmd;
    }

    private void getValuesFromTokens(InsertCommand cmd) {
        for (int i = 4; i < tokens.size() - 1; i++) {
            String token = tokens.get(i).replaceAll("[(),]", "").trim();
            if (!token.isEmpty()) cmd.values.add(token);
        }
    }

    private DBCommand parseSelect() {
        if (tokens.size() < 4 || !endsWithSemicolon()) return null;
        SelectCommand cmd = new SelectCommand();
        int index = getColumnsFromTokens(cmd);
        parseWhereClause(cmd, index + 1);
        return cmd;
    }

    private int getColumnsFromTokens(SelectCommand cmd) {
        int index = 1;
        while (index < tokens.size() && !tokens.get(index).equalsIgnoreCase("FROM")) {
            cmd.columnNames.add(tokens.get(index).toLowerCase());
            index++;
            if (index < tokens.size() && tokens.get(index).equals(",")) index++;
        }
        index++;
        cmd.tableNames.add(tokens.get(index));
        return index;
    }

    private void parseWhereClause(DBCommand cmd, int index) {
        if (index < tokens.size() && tokens.get(index).equalsIgnoreCase("WHERE")) {
            List<String> conditions = getConditions(index + 1);
            cmd.setConditions(conditions);
        }
    }

    private List<String> getConditions(int index) {
        List<String> conditions = new ArrayList<>();
        StringBuilder condition = new StringBuilder();
        boolean inQuotes = false;
        for (int i = index; i < tokens.size(); i++) {
            String token = tokens.get(i);
            if (i == tokens.size() - 1 && token.endsWith(";")) token = token.substring(0, token.length() - 1);
            if (token.startsWith("\"") && !token.endsWith("\"") || token.startsWith("'") && !token.endsWith("'")) {
                inQuotes = true;
            } else if ((token.endsWith("\"") && !token.startsWith("\"")) ||
                    (token.endsWith("'") && !token.startsWith("'"))) inQuotes = false;
            if (!inQuotes && (token.equalsIgnoreCase("AND") || token.equalsIgnoreCase("OR"))) {
                if (!condition.isEmpty()) {
                    conditions.add(condition.toString().trim());
                    condition.setLength(0);
                }
                condition.append(token).append(" ");
            } else condition.append(token).append(" ");
        }
        if (!condition.isEmpty()) conditions.add(condition.toString().trim());
        return conditions;
    }

    private DBCommand parseUpdate() {
        if (tokens.size() < 7 || !tokens.get(2).equalsIgnoreCase("SET")) return null;
        UpdateCommand cmd = new UpdateCommand();
        cmd.tableNames.add(tokens.get(1));
        int i = parseSetClause(cmd);
        if (i == -1) return null;
        parseWhereClause(cmd, i);
        return cmd;
    }

    private int parseSetClause(UpdateCommand cmd) {
        int index = 3;
        while (index < tokens.size() && !tokens.get(index).equalsIgnoreCase("WHERE")) {
            cmd.columnNames.add(tokens.get(index));
            index++;
            if (index >= tokens.size() || !tokens.get(index).equals("=")) return -1;
            index++;
            cmd.values.add(tokens.get(index).replace("'", "").replace("\"", "")); //Not sure If this is needed. Test it out wit some practice Select commands.
            index++;
            if (index >= tokens.size()) return -1;
            if (tokens.get(index).equals(",")) index++;
        }
        return index;
    }

    private DBCommand parseJoin() {
        if (tokens.size() != 9 || !isValidJoinSyntax()) return null;
        JoinCommand cmd = new JoinCommand();
        cmd.tableNames.add(tokens.get(1));
        cmd.tableNames.add(tokens.get(3));
        cmd.columnNames.add(tokens.get(5));
        cmd.columnNames.add(tokens.get(7));
        return cmd;
    }

    private boolean isValidJoinSyntax() {
        return tokens.get(2).equalsIgnoreCase("AND") &&
                tokens.get(4).equalsIgnoreCase("ON") &&
                tokens.get(6).equalsIgnoreCase("AND");
    }

    private DBCommand parseDelete() {
        if (tokens.size() < 5 || !tokens.get(1).equalsIgnoreCase("FROM")) return null;
        DeleteCommand cmd = new DeleteCommand();
        cmd.tableNames.add(tokens.get(2));
        parseWhereClause(cmd, 3);
        return cmd;
    }
}
