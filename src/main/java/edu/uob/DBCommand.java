package edu.uob;

import java.util.ArrayList;
import java.util.List;

public abstract class DBCommand extends CommandParser {
    String[] commandType;
    String DBName;
    List<String> tableNames = new ArrayList<String>();
    List<String> columnNames = new ArrayList<String>();
    public static void parse(String s) {
        //conditions, colnames, tablenames, dbname, commandtype
    }
    String query(DBServer s) {

        return "";
    }

    //public abstract void query(CommandParser commandParser);
}


//DBCommand d = use();
//d.query(this);

//Sring query(DBServer s)