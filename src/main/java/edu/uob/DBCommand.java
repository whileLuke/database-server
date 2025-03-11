package edu.uob;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public abstract class DBCommand extends CommandParser {
    public String commandType;
    public String DBName;
    public List<String> tableNames = new ArrayList<>();
    public List<String> columnNames = new ArrayList<>();
    public List<String> values = new ArrayList<>();

    public abstract String query(DBServer server) throws IOException;
}
