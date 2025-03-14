package edu.uob;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.Duration;

public class MyTests {

    private DBServer server;

    @BeforeEach
    public void setup() {
        server = new DBServer();
    }

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
    public void testOperations() throws IOException {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        String createTableResponse = sendCommandToServer("CREATE TABLE marks (name, mark, pass);");
        assertTrue(createTableResponse.contains("[OK]"));

        String insertResponse1 = sendCommandToServer("INSERT INTO marks VALUES ('Simon', 65, TRUE);");
        String insertResponse2 = sendCommandToServer("INSERT INTO marks VALUES ('Sion', 55, TRUE);");
        String insertResponse3 = sendCommandToServer("INSERT INTO marks VALUES ('Rob', 35, FALSE);");
        String insertResponse4 = sendCommandToServer("INSERT INTO marks VALUES ('Chris', 20, FALSE);");
        assertTrue(insertResponse1.contains("[OK]"));
        assertTrue(insertResponse2.contains("[OK]"));
        assertTrue(insertResponse3.contains("[OK]"));
        assertTrue(insertResponse4.contains("[OK]"));

        String selectResponse = sendCommandToServer("SELECT * FROM marks;");
        assertTrue(selectResponse.contains("id\tname\tmark\tpass"));
        assertTrue(selectResponse.contains("1\tSimon\t65\tTRUE"));
        assertTrue(selectResponse.contains("4\tChris\t20\tFALSE"));

        String whereNotEqualsResponse = sendCommandToServer("SELECT * FROM marks WHERE name != 'Sion';");
        assertTrue(whereNotEqualsResponse.contains("id\tname\tmark\tpass"));
        assertFalse(whereNotEqualsResponse.contains("2\tSion\t55\tTRUE"));

        String wherePassTrueResponse = sendCommandToServer("SELECT * FROM marks WHERE pass == TRUE;");
        assertTrue(wherePassTrueResponse.contains("id\tname\tmark\tpass"));
        assertTrue(wherePassTrueResponse.contains("1\tSimon\t65\tTRUE"));
        assertFalse(wherePassTrueResponse.contains("3\tRob\t35\tFALSE"));

        String updateResponse = sendCommandToServer("UPDATE marks SET mark = 38 WHERE name == 'Chris';");
        assertTrue(updateResponse.contains("[OK]"));
        String verifyUpdate = sendCommandToServer("SELECT * FROM marks WHERE name == 'Chris';");
        assertTrue(verifyUpdate.contains("4\tChris\t38\tFALSE"));

        String deleteResponse = sendCommandToServer("DELETE FROM marks WHERE name == 'Sion';");
        assertTrue(deleteResponse.contains("[OK]"));
        String verifyDelete = sendCommandToServer("SELECT * FROM marks;");
        assertFalse(verifyDelete.contains("Sion"));

        String complexSelectResponse = sendCommandToServer("SELECT * FROM marks WHERE (pass == FALSE) AND (mark > 35);");
        assertTrue(complexSelectResponse.contains("id\tname\tmark\tpass"));
        assertTrue(complexSelectResponse.contains("4\tChris\t38\tFALSE"));
        assertFalse(complexSelectResponse.contains("1\tSimon\t65\tTRUE"));
    }

    @Test
    public void testSelects() throws IOException {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (name, mark, pass);");
        sendCommandToServer("INSERT INTO marks VALUES ('Simon', 65, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Sion', 55, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Rob', 35, FALSE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Chris', 38, FALSE);");

        String alterAddResponse = sendCommandToServer("ALTER TABLE marks ADD age;");
        assertTrue(alterAddResponse.contains("[OK]"));

        String updateAgeResponse = sendCommandToServer("UPDATE marks SET age = 35 WHERE name == 'Simon';");
        assertTrue(updateAgeResponse.contains("[OK]"));

        String verifyUpdate = sendCommandToServer("SELECT * FROM marks WHERE name == 'Chris';");
        assertTrue(verifyUpdate.contains("4\tChris\t38\tFALSE"));

        String deleteResponse = sendCommandToServer("DELETE FROM marks WHERE name == 'Sion';");
        assertTrue(deleteResponse.contains("[OK]"));

        String verifyDelete = sendCommandToServer("SELECT * FROM marks;");
        assertFalse(verifyDelete.contains("Sion"));

        String likeResponse = sendCommandToServer("SELECT * FROM marks WHERE name LIKE 'i';");
        assertTrue(likeResponse.contains("1\tSimon\t65\tTRUE"));
        assertTrue(likeResponse.contains("4\tChris\t38\tFALSE"));
        assertFalse(likeResponse.contains("2\tSion\t55\tTRUE"));

        String selectMultiResponse = sendCommandToServer("SELECT * FROM marks WHERE (pass == FALSE) AND (mark > 35);");
        assertTrue(selectMultiResponse.contains("4\tChris\t38\tFALSE"));
        String selectIdResponse = sendCommandToServer("SELECT id FROM marks WHERE pass == FALSE;");
        assertTrue(selectIdResponse.contains("id"));
        assertTrue(selectIdResponse.contains("3")); // Assuming id 3 and 4 correspond to failed entries
        assertTrue(selectIdResponse.contains("4"));

        String selectNameResponse = sendCommandToServer("SELECT name FROM marks WHERE mark>60;");
        assertTrue(selectNameResponse.contains("name"));
        assertTrue(selectNameResponse.contains("Simon"));
    }

    @Test
    public void testTableAlterations() throws IOException, Exception {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (name, mark, pass);");
        sendCommandToServer("INSERT INTO marks VALUES ('Simon', 65, TRUE);");

        String alterAddResponse = sendCommandToServer("ALTER TABLE marks ADD age;");
        assertTrue(alterAddResponse.contains("[OK]"));
        String selectWithAgeResponse = sendCommandToServer("SELECT * FROM marks;");
        assertTrue(selectWithAgeResponse.contains("age"));

        String updateAgeResponse = sendCommandToServer("UPDATE marks SET age = 35 WHERE name == 'Simon';");
        assertTrue(updateAgeResponse.contains("[OK]"));
        String selectUpdatedAge = sendCommandToServer("SELECT * FROM marks WHERE name == 'Simon';");
        assertTrue(selectUpdatedAge.contains("35"));

        String alterDropResponse = sendCommandToServer("ALTER TABLE marks DROP pass;");
        assertTrue(alterDropResponse.contains("[OK]"));
        String selectWithoutPass = sendCommandToServer("SELECT * FROM marks;");
        assertFalse(selectWithoutPass.contains("pass"));
    }

    @Test
    public void testInvalidQueries() throws IOException, Exception {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        String missingSemicolonResponse = sendCommandToServer("SELECT * FROM marks");
        assertTrue(missingSemicolonResponse.contains("[ERROR]"));

        String nonExistentTableResponse = sendCommandToServer("SELECT * FROM crew;");
        assertTrue(nonExistentTableResponse.contains("[ERROR]"));

        String nonExistentAttrResponse = sendCommandToServer("SELECT height FROM marks WHERE name == 'Chris';");
        assertTrue(nonExistentAttrResponse.contains("[ERROR]"));

        String invalidDeleteResponse = sendCommandToServer("DELETE FROM non_existing_table WHERE id == 1;");
        assertTrue(invalidDeleteResponse.contains("[ERROR]"));
    }

    @Test
    public void testComplexJoins() throws IOException, Exception {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (name, mark, pass);");
        sendCommandToServer("INSERT INTO marks VALUES ('Simon', 65, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Rob', 35, FALSE);");
        sendCommandToServer("CREATE TABLE coursework (task, submission);");
        sendCommandToServer("INSERT INTO coursework VALUES ('OXO', 1);");
        sendCommandToServer("INSERT INTO coursework VALUES ('STAG', 2);");

        String joinResponse = sendCommandToServer("JOIN coursework AND marks ON submission AND id;");
        assertTrue(joinResponse.contains("coursework.task"));
        assertTrue(joinResponse.contains("marks.name"));
        assertTrue(joinResponse.contains("Simon"));
        assertTrue(joinResponse.contains("OXO"));
    }

    @Test
    public void testDropCommands() throws IOException, Exception {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE markbook;");
        sendCommandToServer("USE markbook");
        sendCommandToServer("CREATE TABLE marks;");
        String dropTableResponse = sendCommandToServer("DROP TABLE marks;");
        assertTrue(dropTableResponse.contains("[OK]"));

        String dropDatabaseResponse = sendCommandToServer("DROP DATABASE markbook;");
        assertTrue(dropDatabaseResponse.contains("[OK]"));
    }
}
