package edu.uob;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Duration;

public class DBServerTests {

    private DBServer server;

    // Create a new server _before_ every @Test
    @BeforeEach
    public void setup() {
        server = new DBServer();
    }

    // Random name generator - useful for testing "bare earth" queries (i.e. where tables don't previously exist)
    private String generateRandomName() {
        String randomName = "";
        for(int i=0; i<10 ;i++) randomName += (char)( 97 + (Math.random() * 25.0));
        return randomName;
    }

    private String sendCommandToServer(String command) {
        // Try to send a command to the server - this call will timeout if it takes too long (in case the server enters an infinite loop)
        return assertTimeoutPreemptively(Duration.ofMillis(1000), () -> { return server.handleCommand(command);},
                "Server took too long to respond (probably stuck in an infinite loop)");
    }

    @Test
    public void testTranscript() {
        System.out.println(sendCommandToServer("CREATE DATABASE markbook;"));
        System.out.println(sendCommandToServer("USE markbook;"));
        System.out.println(sendCommandToServer("CREATE TABLE marks (name, mark, pass);"));
        System.out.println(sendCommandToServer("INSERT INTO marks VALUES ('Simon', 65, TRUE);"));
        System.out.println(sendCommandToServer("INSERT INTO marks VALUES ('Sion', 55, TRUE);"));
        System.out.println(sendCommandToServer("INSERT INTO marks VALUES ('Rob', 35, FALSE);"));
        System.out.println(sendCommandToServer("INSERT INTO marks VALUES ('Chris', 20, FALSE);"));
        System.out.println(sendCommandToServer("SELECT * FROM marks;"));
        System.out.println(sendCommandToServer("SELECT * FROM marks WHERE name != 'Sion';"));
        System.out.println(sendCommandToServer("SELECT * FROM marks WHERE pass == TRUE;"));
        System.out.println(sendCommandToServer("CREATE TABLE coursework (task, submission);"));
        System.out.println(sendCommandToServer("INSERT INTO coursework VALUES ('OXO', 3);"));
        System.out.println(sendCommandToServer("INSERT INTO coursework VALUES ('DB', 1);"));
        System.out.println(sendCommandToServer("INSERT INTO coursework VALUES ('OXO', 4);"));
        System.out.println(sendCommandToServer("INSERT INTO coursework VALUES ('STAG', 2);"));
        System.out.println(sendCommandToServer("SELECT * FROM coursework;"));
        System.out.println(sendCommandToServer("JOIN coursework AND marks ON submission AND id;"));
        System.out.println(sendCommandToServer("UPDATE marks SET mark = 38 WHERE name == 'Chris';"));
        System.out.println(sendCommandToServer("SELECT * FROM marks WHERE name == 'Chris';"));
        System.out.println(sendCommandToServer("DELETE FROM marks WHERE name == 'Sion';"));
        System.out.println(sendCommandToServer("SELECT * FROM marks;"));
        System.out.println(sendCommandToServer("SELECT * FROM marks WHERE (pass == FALSE) AND (mark > 35);"));
        System.out.println(sendCommandToServer("SELECT * FROM marks WHERE name LIKE 'i';"));
        System.out.println(sendCommandToServer("SELECT pass FROM marks WHERE name LIKE 'bob';"));
        System.out.println(sendCommandToServer("SELECT pass, id FROM marks WHERE pass == FALSE;"));
        System.out.println(sendCommandToServer("SELECT name FROM marks WHERE mark>60;"));
        System.out.println(sendCommandToServer("SELECT name FROM marks WHERE mark >60;"));
        System.out.println(sendCommandToServer("SELECT name FROM marks WHERE mark> 60;"));
        System.out.println(sendCommandToServer("DELETE FROM marks WHERE mark<40;"));
        System.out.println(sendCommandToServer("SELECT * FROM marks;"));
        System.out.println(sendCommandToServer("ALTER TABLE marks ADD age;"));
        System.out.println(sendCommandToServer("SELECT * FROM marks;"));
        System.out.println(sendCommandToServer("UPDATE marks SET age = 35 WHERE name == 'Simon';"));
        System.out.println(sendCommandToServer("SELECT * FROM marks;"));
        System.out.println(sendCommandToServer("ALTER TABLE marks DROP pass;"));
        System.out.println(sendCommandToServer("SELECT * FROM marks;"));
        System.out.println(sendCommandToServer("SELECT * FROM marks"));
        System.out.println(sendCommandToServer("SELECT * FROM crew;"));
        System.out.println(sendCommandToServer("SELECT height FROM marks WHERE name == 'Chris';"));
        System.out.println(sendCommandToServer("DROP TABLE coursework;"));
        System.out.println(sendCommandToServer("DROP TABLE marks;"));
        System.out.println(sendCommandToServer("DROP DATABASE markbook;"));
    }

    @Test
    public void testBasicCreateAndQuery() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (name, mark, pass);");
        sendCommandToServer("INSERT INTO marks VALUES ('Simon', 65, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Sion', 55, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Rob', 35, FALSE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Chris', 20, FALSE);");
        String response = sendCommandToServer("SELECT * FROM marks;");
        assertTrue(response.contains("[OK]"), "A valid query was made, however an [OK] tag was not returned");
        assertFalse(response.contains("[ERROR]"), "A valid query was made, however an [ERROR] tag was returned");
        assertTrue(response.contains("Simon"), "An attempt was made to add Simon to the table, but they were not returned by SELECT *");
        assertTrue(response.contains("Chris"), "An attempt was made to add Chris to the table, but they were not returned by SELECT *");
    }

    // A test to make sure that querying returns a valid ID (this test also implicitly checks the "==" condition)
    // (these IDs are used to create relations between tables, so it is essential that suitable IDs are being generated and returned !)
    @Test
    public void testQueryID() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (name, mark, pass);");
        sendCommandToServer("INSERT INTO marks VALUES ('Simon', 65, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Simon', 67, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Sion', 55, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Rob', 35, FALSE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Chris', 20, FALSE);");
        String response = sendCommandToServer("SELECT id FROM marks WHERE name == 'Simon';");
        System.out.println(response);
        // Convert multi-lined responses into just a single line
        String singleLine = response.replace("\n"," ").trim();
        // Split the line on the space character
        String[] tokens = singleLine.split(" ");
        // Check that the very last token is a number (which should be the ID of the entry)
        String lastToken = tokens[tokens.length-1];
        try {
            Integer.parseInt(lastToken);
        } catch (NumberFormatException nfe) {
            fail("The last token returned by `SELECT id FROM marks WHERE name == 'Simon';` should have been an integer ID, but was " + lastToken);
        }
    }

    // A test to make sure that databases can be reopened after server restart
    @Test
    public void testTablePersistsAfterRestart() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (name, mark, pass);");
        sendCommandToServer("INSERT INTO marks VALUES ('Simon', 65, TRUE);");
        // Create a new server object
        server = new DBServer();
        sendCommandToServer("USE " + randomName + ";");
        String response = sendCommandToServer("SELECT * FROM marks;");
        assertTrue(response.contains("Simon"), "Simon was added to a table and the server restarted - but Simon was not returned by SELECT *");
    }

    // Test to make sure that the [ERROR] tag is returned in the case of an error (and NOT the [OK] tag)
    @Test
    public void testForErrorTag() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (name, mark, pass);");
        sendCommandToServer("INSERT INTO marks VALUES ('Simon', 65, TRUE);");
        String response = sendCommandToServer("SELECT * FROM libraryfines;");
        assertTrue(response.contains("[ERROR]"), "An attempt was made to access a non-existent table, however an [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An attempt was made to access a non-existent table, however an [OK] tag was returned");
    }

    @Test
    public void testIdPersistsAfterRestart() {
        System.out.println(sendCommandToServer("CREATE DATABASE markbook;"));
        System.out.println(sendCommandToServer("USE markbook;"));
        System.out.println(sendCommandToServer("CREATE TABLE marks (name, mark, pass);"));
        System.out.println(sendCommandToServer("INSERT INTO marks VALUES ('Simon', 65, TRUE);"));
        System.out.println(sendCommandToServer("INSERT INTO marks VALUES ('Sion', 55, TRUE);"));
        System.out.println(sendCommandToServer("INSERT INTO marks VALUES ('Rob', 35, FALSE);"));
        System.out.println(sendCommandToServer("INSERT INTO marks VALUES ('Chris', 20, FALSE);"));
        System.out.println(sendCommandToServer("DELETE FROM marks WHERE id == 2 && id == 4;"));
        System.out.println(sendCommandToServer("SELECT * FROM marks;"));
        server = new DBServer();
        System.out.println(sendCommandToServer("USE markbook;"));
        System.out.println(sendCommandToServer("INSERT INTO marks VALUES ('Barney', 70, TRUE);"));
        System.out.println(sendCommandToServer("SELECT * FROM marks;"));
    }
}
