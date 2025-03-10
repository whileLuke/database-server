package edu.uob;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CommandParser extends DBServer {
    List<String> tokens = new ArrayList<>();
    public String parseCommand(List<String> tokensList) throws Exception {
        tokens = tokensList;
        if (tokens.isEmpty()) return "[ERROR] No command entered.";
        if (tokens.size() == 1) return "[ERROR] Command is not long enough.";
        DBCommand cmd = null;
        String firstToken = tokens.get(0).toUpperCase();
        switch (firstToken) {
            case "USE": cmd = parseUse(); break;
            case "CREATE": cmd = parseCreate(); break;
            case "DROP": cmd = parseDrop(); break;
            case "ALTER": cmd = parseAlter(); break;
            case "INSERT": cmd = parseInsert(); break;
            case "SELECT": cmd = parseSelect(); break;
            case "UPDATE": cmd = parseUpdate(); break;
            case "JOIN": cmd = parseJoin(); break;
            case "DELETE": cmd = parseDelete(); break;
            default: return error("Invalid command type: " + firstToken);
        }
        if(cmd == null) return error(firstToken + " command formatted incorrectly.");
        return cmd.query(this);
    }

    private String error(String message) { return "[ERROR] " + message; }

    private boolean endsWithSemicolon() { return tokens.get(tokens.size() - 1).equals(";"); }

    private boolean isAsterisk(String name) { return Objects.equals(name, "*"); }

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
            tables.put(tableName, new Table(tableName, null));
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

        tables.put(tableName, new Table(tableName, cmd.columnNames));
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
            for (int i = startId + 1; i < tokens.size(); i++) {
                String token = tokens.get(i).replace(";", "");
                if (token.equalsIgnoreCase("AND") || token.equalsIgnoreCase("OR")) {
                    if (!condition.isEmpty()) {
                        conditions.add(condition.toString().trim());
                        condition.setLength(0); // Clear for the next condition
                    }
                }
                condition.append(token).append(" ");
            }
            if (!condition.isEmpty()) {
                conditions.add(condition.toString().trim()); // Add the last condition
            }
            if (cmd instanceof SelectCommand) {
                ((SelectCommand) cmd).setConditions(conditions);
            } else if (cmd instanceof DeleteCommand) {
                ((DeleteCommand) cmd).setConditions(conditions);
            } else if (cmd instanceof UpdateCommand) {
                ((UpdateCommand) cmd).setConditions(conditions);
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
        if (tokens.size() != 8) return null;

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

    private boolean checkForSemicolon() {
        return tokens.get(tokens.size() - 1).equals(";");
    }

    private boolean expectedToken(int index, String expected) {
        return tokens.get(index).equalsIgnoreCase(expected);
    }

    private List<String> parseList(int startIndex, List<String> stopWords) {
        List<String> list = new ArrayList<>();
        for (int i = startIndex; i < tokens.size(); i++) {
            String token = tokens.get(i).replace(",", "");
            if (stopWords.contains(token.toUpperCase()) || token.equals(";")) break;
            list.add(token);
        }
        return list;
    }

    private String parseCondition(int startIndex) {
        StringBuilder condition = new StringBuilder();
        for (int i = startIndex; i < tokens.size(); i++) {
            String token = tokens.get(i);
            if (token.equals(";")) break;
            condition.append(token).append(" ");
        }
        return condition.toString().trim();
    }

    private String parseTableName(int index) {
        String name = tokens.get(index);
        return name.equals("*") ? null : name;
    }

    private String parseColumnName(int index) {
        String name = tokens.get(index);
        return name.equals("*") ? null : name;
    }
}







//buikld up a command, takes all data need. all parsing first step, execute comand second step
//recursive to send parser / recursive dissent parser
//mechanical process based on BNF
//parse whats coming in based on that
//if command is select, call another method does next thin g of select.; rather than returning from select. go to parsewildattriobuteslist. if its a star look from from next to that. if not a star check it matsaches ther thing uit cioud ve.
//once from look for table names needsot be a stirng or valid for table names
//look at the garmmar buiuld up string based solely from the grammar
//start implementing first bitchs of grammar that allow ineraction w tables and database
//reading table from file like use database and writnig to file like create database
//simple comands irst of all
//parser: as it parses gonna build a subclass of DB commands. this is returned to DBServer (where initial string ocmes in)
//server class will hold string of all tables. within it have a map of table names.
//eg mammals has tables mareptiles primates etc. string name of table as the key to get to the in memory representation of the table calss
//returned to DBserver passes a reference to itself as an argmenmt.
//faciliitates error handling command bulding
//go through string comes in. select * from actors etc.
//go through recursive call stack, descending into it. all fit grammar in select statemnet. store it all as a DBcmd

//so well have DBcmd
//all can potentially contain data to execute comand
//if select statment need table name and * and attributes list
//conditional statement for selecting rows from table. needs to be stored in subclass of DBcmd in parsing.
//abstract class DBcmd has unilitatised attributes for not comands nd one initalised that is correct
//use blobs of te data based on what u ned.
//eg conditions, colnames, tablenames, dbname, commandtype
//will all get filled up
//selectcmd or whatever
//if first thing is select
//dbcommand c = new selectcmd
//if firs tthing is insert
//dbcommand= new insertcmd
//left hand side polymorphism

//subclasses of dbcmd depedning on the first word.
// once fnished parsing expression, returnes a DBcmd
//execute a single method of that dbcmd which is query
//dbcmd comes back eg c
//c.query(DBserver s)
//can access all thestates of that server class
//selecting thigs from table, need to be able to access table name
///map of tables, key for that map will be string for name of table like actors
//column name likenames, folllow reference all states in tables via the this referce thatcomes in
//similar to syntax tree -> budikling u[ a big data blob with all attributes needed to execute command
//query command will do eerthig for us
//diff depending on command
//some will mutate or delete
//String query(DBServer s)
//diff subclasses will have diff implementations of that command
//subclasses of dbcmd will have their own method of doing query
//so abstract command dbcmd - with subclasses for all the diff types of command

//table class
//luist of lists repping rows
//column names
//mehods in there for getting stuff romf tables
//repping database in memory map hash table
//key table name to  key to table.
//key will be a string
// on disc rep of database subdirectory of database folder and tab files
//with the call of a method ca write to there or pul to here into memeory

//everytime mutates state of table, writing to disc. or eery time do something, write it to diskso its synchronised
//keep map in server class
//when server class passes the command in, itll be exposing all that state of subclass dbcmd executing that query.

//fill sup evberything indbcmd

//query will bave diff implementations in every subclass
