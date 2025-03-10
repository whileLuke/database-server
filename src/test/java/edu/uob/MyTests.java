package edu.uob;

import org.junit.jupiter.api.*;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class MyTests {

    private DBServer server;

    @BeforeEach
    public void setup() throws IOException, Exception {
        server = new DBServer();
        server.handleCommand("CREATE DATABASE markbook;");
        server.handleCommand("USE markbook;");
    }

    @Test
    public void testBasicCRUDOperations() throws IOException, Exception {
        // Test CREATE TABLE command
        String createTableResponse = server.handleCommand("CREATE TABLE marks (name, mark, pass);");
        assertTrue(createTableResponse.contains("[OK]"));

        // Test INSERT command
        String insertResponse1 = server.handleCommand("INSERT INTO marks VALUES ('Simon', 65, TRUE);");
        String insertResponse2 = server.handleCommand("INSERT INTO marks VALUES ('Sion', 55, TRUE);");
        String insertResponse3 = server.handleCommand("INSERT INTO marks VALUES ('Rob', 35, FALSE);");
        String insertResponse4 = server.handleCommand("INSERT INTO marks VALUES ('Chris', 20, FALSE);");
        assertTrue(insertResponse1.contains("[OK]"));
        assertTrue(insertResponse2.contains("[OK]"));
        assertTrue(insertResponse3.contains("[OK]"));
        assertTrue(insertResponse4.contains("[OK]"));

        // Test SELECT * query
        String selectResponse = server.handleCommand("SELECT * FROM marks;");
        assertTrue(selectResponse.contains("id\tname\tmark\tpass"));
        assertTrue(selectResponse.contains("1\tSimon\t65\tTRUE"));
        assertTrue(selectResponse.contains("4\tChris\t20\tFALSE"));

        // Test SELECT with WHERE name != 'Sion'
        String whereNotEqualsResponse = server.handleCommand("SELECT * FROM marks WHERE name != 'Sion';");
        assertTrue(whereNotEqualsResponse.contains("id\tname\tmark\tpass"));
        assertFalse(whereNotEqualsResponse.contains("2\tSion\t55\tTRUE"));

        // Test SELECT with WHERE pass == TRUE
        String wherePassTrueResponse = server.handleCommand("SELECT * FROM marks WHERE pass == TRUE;");
        assertTrue(wherePassTrueResponse.contains("id\tname\tmark\tpass"));
        assertTrue(wherePassTrueResponse.contains("1\tSimon\t65\tTRUE"));
        assertFalse(wherePassTrueResponse.contains("3\tRob\t35\tFALSE"));

        // Test UPDATE command
        String updateResponse = server.handleCommand("UPDATE marks SET mark = 38 WHERE name == 'Chris';");
        assertTrue(updateResponse.contains("[OK]"));
        String verifyUpdate = server.handleCommand("SELECT * FROM marks WHERE name == 'Chris';");
        assertTrue(verifyUpdate.contains("4\tChris\t38\tFALSE"));

        // Test DELETE command
        String deleteResponse = server.handleCommand("DELETE FROM marks WHERE name == 'Sion';");
        assertTrue(deleteResponse.contains("[OK]"));
        String verifyDelete = server.handleCommand("SELECT * FROM marks;");
        assertFalse(verifyDelete.contains("Sion"));

        // Test complex SELECT query
        String complexSelectResponse = server.handleCommand("SELECT * FROM marks WHERE (pass == FALSE) AND (mark > 35);");
        assertTrue(complexSelectResponse.contains("id\tname\tmark\tpass"));
        assertTrue(complexSelectResponse.contains("4\tChris\t38\tFALSE"));
        assertFalse(complexSelectResponse.contains("1\tSimon\t65\tTRUE"));
    }

    @Test
    public void testLIKEAndOtherSelectors() throws IOException, Exception {
        // Setup table and data
        server.handleCommand("CREATE TABLE marks (name, mark, pass);");
        server.handleCommand("INSERT INTO marks VALUES ('Simon', 65, TRUE);");
        server.handleCommand("INSERT INTO marks VALUES ('Sion', 55, TRUE);");
        server.handleCommand("INSERT INTO marks VALUES ('Chris', 38, FALSE);");

        // Test SELECT with LIKE
        String likeResponse = server.handleCommand("SELECT * FROM marks WHERE name LIKE 'i';");
        assertTrue(likeResponse.contains("1\tSimon\t65\tTRUE"));
        assertTrue(likeResponse.contains("4\tChris\t38\tFALSE"));
        assertFalse(likeResponse.contains("2\tSion\t55\tTRUE"));

        // Test SELECT single column
        String selectIdResponse = server.handleCommand("SELECT id FROM marks WHERE pass == FALSE;");
        assertTrue(selectIdResponse.contains("id"));
        assertTrue(selectIdResponse.contains("3")); // Assuming id 3 and 4 correspond to failed entries

        String selectNameResponse = server.handleCommand("SELECT name FROM marks WHERE mark>60;");
        assertTrue(selectNameResponse.contains("name"));
        assertTrue(selectNameResponse.contains("Simon"));
    }

    @Test
    public void testTableAlterations() throws IOException, Exception {
        // Setup table and data
        server.handleCommand("CREATE TABLE marks (name, mark, pass);");
        server.handleCommand("INSERT INTO marks VALUES ('Simon', 65, TRUE);");

        // Test ALTER TABLE ADD
        String alterAddResponse = server.handleCommand("ALTER TABLE marks ADD age;");
        assertTrue(alterAddResponse.contains("[OK]"));
        String selectWithAgeResponse = server.handleCommand("SELECT * FROM marks;");
        assertTrue(selectWithAgeResponse.contains("age"));

        // Update newly added column
        String updateAgeResponse = server.handleCommand("UPDATE marks SET age = 35 WHERE name == 'Simon';");
        assertTrue(updateAgeResponse.contains("[OK]"));
        String selectUpdatedAge = server.handleCommand("SELECT * FROM marks WHERE name == 'Simon';");
        assertTrue(selectUpdatedAge.contains("35"));

        // Test ALTER TABLE DROP
        String alterDropResponse = server.handleCommand("ALTER TABLE marks DROP pass;");
        assertTrue(alterDropResponse.contains("[OK]"));
        String selectWithoutPass = server.handleCommand("SELECT * FROM marks;");
        assertFalse(selectWithoutPass.contains("pass"));
    }

    @Test
    public void testErrorsAndInvalidQueries() throws IOException, Exception {
        // Test missing semicolon
        String missingSemicolonResponse = server.handleCommand("SELECT * FROM marks");
        assertTrue(missingSemicolonResponse.contains("[ERROR]"));

        // Test selecting non-existent table
        String nonExistentTableResponse = server.handleCommand("SELECT * FROM crew;");
        assertTrue(nonExistentTableResponse.contains("[ERROR]"));

        // Test selecting non-existent attribute
        String nonExistentAttrResponse = server.handleCommand("SELECT height FROM marks WHERE name == 'Chris';");
        assertTrue(nonExistentAttrResponse.contains("[ERROR]"));

        // Test invalid DELETE query
        String invalidDeleteResponse = server.handleCommand("DELETE FROM non_existing_table WHERE id == 1;");
        assertTrue(invalidDeleteResponse.contains("[ERROR]"));
    }

    @Test
    public void testComplexJoins() throws IOException, Exception {
        // Setup tables and data
        server.handleCommand("CREATE TABLE marks (name, mark, pass);");
        server.handleCommand("INSERT INTO marks VALUES ('Simon', 65, TRUE);");
        server.handleCommand("INSERT INTO marks VALUES ('Rob', 35, FALSE);");
        server.handleCommand("CREATE TABLE coursework (task, submission);");
        server.handleCommand("INSERT INTO coursework VALUES ('OXO', 1);");
        server.handleCommand("INSERT INTO coursework VALUES ('STAG', 2);");

        // Test JOIN command
        String joinResponse = server.handleCommand("JOIN coursework AND marks ON submission AND id;");
        assertTrue(joinResponse.contains("coursework.task"));
        assertTrue(joinResponse.contains("marks.name"));
        assertTrue(joinResponse.contains("Simon"));
        assertTrue(joinResponse.contains("OXO"));
    }

    @Test
    public void testDropCommands() throws IOException, Exception {
        // Drop individual table
        String dropTableResponse = server.handleCommand("DROP TABLE marks;");
        assertTrue(dropTableResponse.contains("[OK]"));

        // Drop database
        String dropDatabaseResponse = server.handleCommand("DROP DATABASE markbook;");
        assertTrue(dropDatabaseResponse.contains("[OK]"));
    }
}
