package edu.uob;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.Duration;

public class SqlGrammarTest {

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
    public void testDatabaseCreation() {
        String randomName = generateRandomName();
        String response = sendCommandToServer("CREATE DATABASE " + randomName + ";");
        assertTrue(response.contains("[OK]"), "Database creation should return [OK]");
        assertFalse(response.contains("[ERROR]"), "Database creation should not return [ERROR]");
    }

    @Test
    public void testDatabaseSelection() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        String response = sendCommandToServer("USE " + randomName + ";");
        assertTrue(response.contains("[OK]"), "Database selection should return [OK]");
        assertFalse(response.contains("[ERROR]"), "Database selection should not return [ERROR]");
    }

    @Test
    public void testTableCreation() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        String response = sendCommandToServer("CREATE TABLE students (id, firstName, lastName, age, gpa, major, credits, enrolled, grade, startDate);");
        assertTrue(response.contains("[OK]"), "Table creation should return [OK]");
        assertFalse(response.contains("[ERROR]"), "Table creation should not return [ERROR]");
    }

    @Test
    public void testDataInsertion() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE students (id, firstName, lastName, age, gpa, major, credits, enrolled, grade, startDate);");
        sendCommandToServer("INSERT INTO students VALUES ('John', 'Smith', 20, 3.5, 'CS', 60, TRUE, 'A', '2022-09-01');");
        sendCommandToServer("INSERT INTO students VALUES ('Emma', 'Johnson', 19, 3.8, '!/\"/:}P{}@}{}{:()()(=£$%^&*()_+', 45, TRUE, 'A', '2022-09-01');");

        String response = sendCommandToServer("SELECT * FROM students;");
        assertTrue(response.contains("[OK]"), "Data query should return [OK]");
        assertFalse(response.contains("[ERROR]"), "Data query should not return [ERROR]");
        assertTrue(response.contains("John"), "Query result should contain John");
        assertTrue(response.contains("Emma"), "Query result should contain Emma");
    }

    @Test
    public void testDeeplyNestedConditions() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE students (id, firstName, lastName, age, gpa, major, credits, enrolled, grade, startDate);");
        sendCommandToServer("INSERT INTO students VALUES ('John', 'Smith', 20, 3.5, 'CS', 60, TRUE, 'A', '2022-09-01');");
        sendCommandToServer("INSERT INTO students VALUES ('Emma', 'Johnson', 19, 3.8, 'Physics', 45, TRUE, 'A', '2022-09-01');");
        sendCommandToServer("INSERT INTO students VALUES ('Michael', 'Williams', 17, 3.2, 'Math', 30, TRUE, 'B', '2022-09-01');");
        sendCommandToServer("INSERT INTO students VALUES ('Sarah', 'Jones', 21, 3.9, 'CS', 75, TRUE, 'A', '2021-09-01');");

        String response = sendCommandToServer("SELECT * FROM students WHERE (((age > 18) AND (gpa >= 3.5)) OR ((major == 'CS') AND (credits >= 60)));");
        assertTrue(response.contains("[OK]"), "Complex query should return [OK]");
        assertTrue(response.contains("John"), "Complex query should return John");
        assertTrue(response.contains("Emma"), "Complex query should return Emma");
        assertTrue(response.contains("Sarah"), "Complex query should return Sarah");
        assertFalse(response.contains("Michael"), "Complex query should not return Michael");
    }

    @Test
    public void testComplexInsert() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE mixedData (intValue, negInt, posInt, floatValue, negFloat, posFloat, boolTrue, boolFalse, quotedString, specialChars, nullValue);");

        String response = sendCommandToServer("INSERT INTO mixedData VALUES (42, -42, 42, 3.14, -3.14, 3.14, TRUE, FALSE, \"Single quote inside string\", 'Special @#$%^&*() chars', NULL);");
        assertTrue(response.contains("[OK]"), "Complex insert should return [OK]");

        response = sendCommandToServer("SELECT * FROM mixedData;");
        assertTrue(response.contains("42"), "Query result should contain 42");
        assertTrue(response.contains("-42"), "Query result should contain -42");
        assertTrue(response.contains("3.14"), "Query result should contain 3.14");
        assertTrue(response.contains("-3.14"), "Query result should contain -3.14");
        assertTrue(response.contains("TRUE"), "Query result should contain TRUE");
        assertTrue(response.contains("FALSE"), "Query result should contain FALSE");
        assertTrue(response.contains("Single quote inside string"), "Query result should contain string with quote");
        assertTrue(response.contains("Special @#$%^&*() chars"), "Query result should contain special characters");
    }

    @Test
    public void testMaximumWhitespace() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");

        String response = sendCommandToServer("CREATE       TABLE       spaceTest       (   column1    ,    column2     ,      column3      );");
        assertTrue(response.contains("[OK]"), "Table creation with excess whitespace should return [OK]");

        response = sendCommandToServer("INSERT      INTO      spaceTest      VALUES      (  1  ,  'test1'  ,  'description1'  );");
        assertTrue(response.contains("[OK]"), "Insert with excess whitespace should return [OK]");

        response = sendCommandToServer("SELECT      *      FROM      spaceTest;");
        assertTrue(response.contains("test1"), "Query result should contain test1");
        assertTrue(response.contains("description1"), "Query result should contain description1");
    }

    @Test
    public void testCreateTableWithNoAttributes() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");

        String response = sendCommandToServer("CREATE TABLE emptyTable ();");
        assertTrue(response.contains("[OK]"), "Empty table creation should return [OK]");

        // Try to insert into the empty table - this might be an error depending on your implementation
        response = sendCommandToServer("INSERT INTO emptyTable VALUES ();");

        // Try to select from the empty table
        response = sendCommandToServer("SELECT * FROM emptyTable;");
        assertTrue(response.contains("[OK]"), "Query on empty table should return [OK]");
    }

    @Test
    public void testNullValuesInDifferentPositions() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE nullTest (value1, value2, value3, value4, value5);");

        String response = sendCommandToServer("INSERT INTO nullTest VALUES (NULL, 'not null', NULL, 42, NULL);");
        assertTrue(response.contains("[OK]"), "Insert with NULL values should return [OK]");

        response = sendCommandToServer("SELECT * FROM nullTest;");
        assertTrue(response.contains("[OK]"), "Query should return [OK]");
        assertTrue(response.contains("NULL"), "Query result should contain NULL");
        assertTrue(response.contains("not null"), "Query result should contain 'not null'");
        assertTrue(response.contains("42"), "Query result should contain 42");
    }

    @Test
    public void testJoinWithConditions() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");

        // Create users table
        sendCommandToServer("CREATE TABLE users (id, name, email);");
        sendCommandToServer("INSERT INTO users VALUES ('Alice', 'alice@example.com');");
        sendCommandToServer("INSERT INTO users VALUES ('Bob', 'bob@example.com');");
        sendCommandToServer("INSERT INTO users VALUES ('Charlie', 'charlie@example.com');");

        // Create roles table
        sendCommandToServer("CREATE TABLE roles (id, userId, roleName, permissions);");
        sendCommandToServer("INSERT INTO roles VALUES (1, 'Admin', 'all');");
        sendCommandToServer("INSERT INTO roles VALUES (2, 'Editor', 'read,write');");
        sendCommandToServer("INSERT INTO roles VALUES (3, 'Viewer', 'read');");

        // Test join with conditions (syntax might need adjustment based on your implementation)
        String response = sendCommandToServer("SELECT * FROM users JOIN roles ON users.id == roles.userId WHERE roles.permissions LIKE '%read%';");
        assertTrue(response.contains("Bob"), "Join result should contain Bob");
        assertTrue(response.contains("Charlie"), "Join result should contain Charlie");
    }

    @Test
    public void testFloatingPointValues() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE floatingPoints (value1, value2, value3, value4, value5);");

        String response = sendCommandToServer("INSERT INTO floatingPoints VALUES (1.0, 0.5, -0.25, 3.14159, 0.0);");
        assertTrue(response.contains("[OK]"), "Insert with floating points should return [OK]");

        response = sendCommandToServer("SELECT * FROM floatingPoints WHERE value4 > 3.0 AND value3 < 0.0;");
        assertTrue(response.contains("3.14159"), "Query result should contain 3.14159");
        assertTrue(response.contains("-0.25"), "Query result should contain -0.25");
    }

    @Test
    public void testAlterTable() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE modifiedTable (id, existingColumn, oldColumn);");

        sendCommandToServer("INSERT INTO modifiedTable VALUES ('existing value', 'to be dropped');");

        // Test adding a column (syntax might need adjustment based on your implementation)
        String response = sendCommandToServer("ALTER TABLE modifiedTable ADD newColumn;");
        assertTrue(response.contains("[OK]"), "Alter table add column should return [OK]");

        // Test dropping a column (syntax might need adjustment based on your implementation)
        response = sendCommandToServer("ALTER TABLE modifiedTable DROP oldColumn;");
        assertTrue(response.contains("[OK]"), "Alter table drop column should return [OK]");

        // Verify the changes
        response = sendCommandToServer("SELECT * FROM modifiedTable;");
        assertTrue(response.contains("existing value"), "Query result should contain 'existing value'");
        assertFalse(response.contains("to be dropped"), "Query result should not contain 'to be dropped'");
    }

    @Test
    public void testBooleanOperators() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE boolTest (a, b, c, d, e, f, g);");

        sendCommandToServer("INSERT INTO boolTest VALUES (TRUE, FALSE, TRUE, FALSE, FALSE, FALSE, FALSE);");

        // Test complex boolean expressions
        String response = sendCommandToServer("SELECT * FROM boolTest WHERE (a AND b == FALSE) OR (c AND d == FALSE);");
        assertTrue(response.contains("[OK]"), "Boolean query should return [OK]");
        assertTrue(response.contains("TRUE"), "Query result should contain TRUE");
        assertTrue(response.contains("FALSE"), "Query result should contain FALSE");
    }
}