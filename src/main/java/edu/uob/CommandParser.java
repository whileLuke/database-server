package edu.uob;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class CommandParser extends DBServer {
    List<String> tokens = new ArrayList<>();
    public String parseCommand(List<String> tokensList) {
        System.out.println("Test 1");
        tokens = tokensList;
        System.out.println(tokens);
        if (tokens.isEmpty()) return "[ERROR] No command entered.";
        if (tokens.size() == 1) return "[ERROR] Command is not long enough.";
        DBCommand d = null;
        switch (tokens.get(0).toUpperCase()) {
            case "USE":
                System.out.println("Test 1 bil");
                d = use();
                break;
            case "CREATE":
                System.out.println("Test 2 bil");
                if(Objects.equals(tokens.get(1).toLowerCase(), "table")) d = createTable();
                else if(Objects.equals(tokens.get(1).toLowerCase(), "database")) d = createDatabase();
                break;
            case "DROP":
                if (Objects.equals(tokens.get(1).toLowerCase(), "table")) d = dropTable();
                else if (Objects.equals(tokens.get(1).toLowerCase(), "database")) d = dropDatabase();
                break;
            case "ALTER":
                d = alterTable();
                break;
            case "INSERT":
                d = insert();
                break;
            case "SELECT":
                d = select();
                break;
            case "UPDATE":
                d = update();
                break;
            default:
              return "[ERROR] Invalid command type: " + tokens.get(0) + ".";
        }
        if(d == null){
            return "[ERROR] " + tokens.get(0).toUpperCase() + " command formatted incorrectly.";
        }
        return d.query(this);
        //DBCommand.parse(tokens.get(0));
    }

    private DBCommand use() {
        if(tokens.size() != 3) return null;
        if(!tokens.get(2).equals(";")) return null;
        if(tokens.get(1).equals("*")) return null;
        //Maybe add a chek that its an instance of string
        DBCommand d = new UseCommand();
        d.DBName = tokens.get(1);
        return d;
    }

    private DBCommand createTable() {
        System.out.println("Test 3 TABLE");
        if(tokens.size() < 4) return null;
        if (!Objects.equals(tokens.get(tokens.size() - 1), ";")) return null;
        DBCommand d = new CreateTableCommand();
        d.tableNames.add(tokens.get(2));
        //Make sure it's not a star.
        System.out.println("Test 5 TABLE");
        if(tokens.size() > 4) {
            System.out.println("Test 7 TABLE");
            if(!tokens.get(3).equals("(")) return null;
            System.out.println("Test 9 TABLE");
            for(int i = 4; i < tokens.size(); i += 2) {
                System.out.println("Test 11 TABLE");
                String column = tokens.get(i);
                if (Objects.equals(column, "*")) return null;
                System.out.println("Test 13 TABLE");
                String punctuation = tokens.get(i + 1);
                d.columnNames.add(column);
                if(!punctuation.equals(",") && !punctuation.equals(")")) return null;
                System.out.println("Test 15 TABLE");
                if (punctuation.equals(")")) {
                    return d;
                    //if (i == tokens.size() - 2) return d; // we've reached the end of attributes
                    //else return null;
                }
                System.out.println("Test 17 TABLE");
                if(i + 2 >= tokens.size()) return null;
            }
        }
        return d;
    }

    private DBCommand createDatabase() {
        if(tokens.size() != 4) return null;
        if (!tokens.get(3).equals(";")) return null;
        if(tokens.get(2).equals("*")) return null;
        DBCommand d = new CreateDatabaseCommand();
        d.DBName = tokens.get(2);
        return d;
    }

    private DBCommand dropTable() {
        if (tokens.size() != 4) return null;
        if (!tokens.get(3).equals(";")) return null;

        DBCommand d = new DropCommand();
        d.tableNames.add(tokens.get(2));
        return d;
    }

    private DBCommand dropDatabase() {
        if (tokens.size() != 4) return null;
        if (!tokens.get(3).equals(";")) return null;

        DBCommand d = new DropCommand();
        d.DBName = tokens.get(2);
        return d;
    }

    private DBCommand alterTable() {
        if (tokens.size() < 6) return null;
        if (!tokens.get(tokens.size() - 1).equals(";")) return null;
        DBCommand d = new AlterCommand();
        d.tableNames.add(tokens.get(2));
        String operation = tokens.get(3).toUpperCase();
        if (Objects.equals(operation, "ADD")) {
            d.commandType = "ADD";
            d.columnNames.add(tokens.get(4));
        } else if (Objects.equals(operation, "DROP")) {
            d.commandType = "DROP";
            d.columnNames.add(tokens.get(4));
        } else {
            return null;
        }
        return d;
    }

    private DBCommand insert() {
        if (tokens.size() < 5) return null;
        DBCommand d = new InsertCommand();
        d.tableNames.add(tokens.get(2));
        if (!tokens.get(3).equalsIgnoreCase("values")) return null;

        for (int i = 4; i < tokens.size(); i++) {
            if (tokens.get(i).equals(";")) break;
            d.values.add(tokens.get(i).replace("", "'"));
        }
        return d;
    }

    private DBCommand select() {
        if (tokens.size() < 4) return null;
        DBCommand d = new SelectCommand();
        if (tokens.get(1).equals("*")) d.columnNames.add("*");
        else {
            for (String column : tokens.get(1).split(",")) {
                d.columnNames.add(column);
            }
        }
        if (!tokens.get(2).equals("FROM")) return null;
        d.tableNames.add(tokens.get(3));
        return d;
    }

    private DBCommand update() {
        if (tokens.size() < 7) return null;
        DBCommand d = new UpdateCommand();
        d.tableNames.add(tokens.get(1));
        if (!tokens.get(2).equals("SET")) return null;
        for (int i = 3; i < tokens.size(); i += 4) {
            if (!tokens.get(i + 1).equals("=")) return null;
            d.columnNames.add(tokens.get(i)); // Column name
            d.values.add(tokens.get(i + 2).replace("'", "")); // Column value
            if (tokens.get(i + 3).equals(";")) break;
        }
        return d;
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
