package edu.uob;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import java.io.File;
import java.time.Duration;

@DisplayName("DBServer Comprehensive School Database Test Suite")
public class CustomTranscriptTest {

    private DBServer server;

    @BeforeEach
    public void setup() {
        // Create a fresh DBServer instance before each top-level test
        server = new DBServer();
    }

    /**
     * Helper method to send a command to the server.
     * Uses a timeout to catch potential infinite loops.
     */
    private String sendCommand(String command) {
        return org.junit.jupiter.api.Assertions.assertTimeoutPreemptively(
                Duration.ofMillis(1000),
                () -> server.handleCommand(command),
                "Server timed out (possibly stuck in an infinite loop)."
        );
    }

    @Nested
    @DisplayName("Database and Table Creation & Insertion")
    class CreationInsertionTests {
        @Test
        @DisplayName("Create Database, Use It, Create Table and Insert Rows")
        public void testCreateAndInsert() {
            String response = sendCommand("CREATE DATABASE schoolCreation;");
            assertTrue(response.contains("[OK]"), "CREATE DATABASE schoolCreation failed.");

            response = sendCommand("USE schoolCreation;");
            assertTrue(response.contains("[OK]"), "USE schoolCreation failed.");

            response = sendCommand("CREATE TABLE students (name, score, passed);");
            assertTrue(response.contains("[OK]"), "CREATE TABLE students failed.");

            response = sendCommand("INSERT INTO students VALUES ('Tom', 85, TRUE);");
            assertTrue(response.contains("[OK]"), "INSERT for Tom failed.");
            response = sendCommand("INSERT INTO students VALUES ('Jerry', 65, TRUE);");
            assertTrue(response.contains("[OK]"), "INSERT for Jerry failed.");
            response = sendCommand("INSERT INTO students VALUES ('Megan', 55, FALSE);");
            assertTrue(response.contains("[OK]"), "INSERT for Megan failed.");
            response = sendCommand("INSERT INTO students VALUES ('Lily', 40, FALSE);");
            assertTrue(response.contains("[OK]"), "INSERT for Lily failed.");
        }
    }

    @Nested
    @DisplayName("SELECT Queries")
    class SelectQueriesTests {
        @BeforeEach
        public void setupSelect() {
            sendCommand("DROP DATABASE schoolSelect;");
            sendCommand("CREATE DATABASE schoolSelect;");
            sendCommand("USE schoolSelect;");
            sendCommand("CREATE TABLE students (name, score, passed);");
            sendCommand("INSERT INTO students VALUES ('Tom', 85, TRUE);");
            sendCommand("INSERT INTO students VALUES ('Jerry', 65, TRUE);");
            sendCommand("INSERT INTO students VALUES ('Megan', 55, FALSE);");
            sendCommand("INSERT INTO students VALUES ('Lily', 40, FALSE);");
        }

        @Test
        @DisplayName("SELECT * from students")
        public void testSelectAll() {
            String response = sendCommand("SELECT * FROM students;");
            assertTrue(response.contains("[OK]"), "SELECT * from students failed.");
            assertTrue(response.contains("Tom"), "Expected 'Tom' in SELECT * output.");
            assertTrue(response.contains("Jerry"), "Expected 'Jerry' in SELECT * output.");
        }

        @Test
        @DisplayName("SELECT with condition: score > 80")
        public void testSelectConditionScore() {
            String response = sendCommand("SELECT * FROM students WHERE score > 80;");
            assertTrue(response.contains("[OK]"), "SELECT with condition (score > 80) failed.");
            assertTrue(response.contains("Tom"), "Expected 'Tom' for score > 80.");
            assertFalse(response.contains("Jerry"), "Jerry should not appear for score > 80.");
        }

        @Test
        @DisplayName("SELECT with condition: passed == FALSE")
        public void testSelectConditionPassed() {
            String response = sendCommand("SELECT * FROM students WHERE passed == FALSE;");
            assertTrue(response.contains("[OK]"), "SELECT with condition (passed == FALSE) failed.");
            assertTrue(response.contains("Megan"), "Expected 'Megan' when passed==FALSE.");
            assertTrue(response.contains("Lily"), "Expected 'Lily' when passed==FALSE.");
            assertFalse(response.contains("Tom"), "Tom should not appear when passed==FALSE.");
        }
    }

    @Nested
    @DisplayName("JOIN Command")
    class JoinTests {
        @BeforeEach
        public void setupJoin() {
            sendCommand("CREATE DATABASE joinSchool;");
            sendCommand("USE joinSchool;");
            // Create two tables: courses and enrollments
            sendCommand("CREATE TABLE courses (courseName, courseId);");
            sendCommand("CREATE TABLE enrollments (studentName, courseId);");
            // Insert data into courses
            sendCommand("INSERT INTO courses VALUES ('Biology', 201);");
            sendCommand("INSERT INTO courses VALUES ('Chemistry', 202);");
            // Insert data into enrollments
            sendCommand("INSERT INTO enrollments VALUES ('Tom', 201);");
            sendCommand("INSERT INTO enrollments VALUES ('Jerry', 202);");
        }

        @Test
        @DisplayName("Test JOIN courses and enrollments")
        public void testJoin() {
            String response = sendCommand("JOIN courses AND enrollments ON courseId AND courseId;");
            assertTrue(response.contains("[OK]"), "JOIN command failed.");
            assertTrue(response.contains("courses.courseName"), "Expected header 'courses.courseName' in join output.");
            assertTrue(response.contains("enrollments.studentName"), "Expected header 'enrollments.studentName' in join output.");
            assertTrue(response.contains("Biology"), "Expected 'Biology' in join result.");
            assertTrue(response.contains("Tom"), "Expected 'Tom' in join result.");
        }
    }

    @Nested
    @DisplayName("UPDATE and DELETE Commands")
    class UpdateDeleteTests {
        @BeforeEach
        public void setupUpdateDelete() {
            sendCommand("DROP DATABASE modSchool;");
            sendCommand("CREATE DATABASE modSchool;");
            sendCommand("USE modSchool;");
            sendCommand("CREATE TABLE students (name, score, passed);");
            sendCommand("INSERT INTO students VALUES ('Alice', 90, TRUE);");
            sendCommand("INSERT INTO students VALUES ('Bob', 75, TRUE);");
            sendCommand("INSERT INTO students VALUES ('Cathy', 50, FALSE);");
            sendCommand("INSERT INTO students VALUES ('Donna', 40, FALSE);");
        }

        @Test
        @DisplayName("Test UPDATE Command")
        public void testUpdate() {
            String response = sendCommand("UPDATE students SET score = 65 WHERE name == 'Donna';");
            assertTrue(response.contains("[OK]"), "UPDATE command failed.");
            response = sendCommand("SELECT * FROM students WHERE name == 'Donna';");
            assertTrue(response.contains("65"), "Expected updated score (65) for Donna.");
        }

        @Test
        @DisplayName("Test DELETE Command")
        public void testDelete() {
            String response = sendCommand("DELETE FROM students WHERE name == 'Cathy';");
            assertTrue(response.contains("[OK]"), "DELETE command failed.");
            response = sendCommand("SELECT * FROM students;");
            assertFalse(response.contains("Cathy"), "Expected 'Cathy' to be deleted.");
        }
    }

    @Nested
    @DisplayName("ALTER Commands")
    class AlterCommandsTests {
        @BeforeEach
        public void setupAlter() {
            sendCommand("DROP DATABASE alterSchool;");
            sendCommand("CREATE DATABASE alterSchool;");
            sendCommand("USE alterSchool;");
            sendCommand("CREATE TABLE students (name, score, passed);");
            sendCommand("INSERT INTO students VALUES ('Alice', 90, TRUE);");
        }

        @Test
        @DisplayName("Test ALTER TABLE ADD")
        public void testAlterAdd() {
            String response = sendCommand("ALTER TABLE students ADD grade;");
            assertTrue(response.contains("[OK]"), "ALTER TABLE ADD grade failed.");
            response = sendCommand("SELECT * FROM students;");
            assertTrue(response.contains("grade"), "Expected column 'grade' in students table.");
        }

        @Test
        @DisplayName("Test ALTER TABLE DROP")
        public void testAlterDrop() {
            String response = sendCommand("ALTER TABLE students DROP passed;");
            assertTrue(response.contains("[OK]"), "ALTER TABLE DROP passed failed.");
            response = sendCommand("SELECT * FROM students;");
            assertFalse(response.contains("passed"), "Expected column 'passed' to be dropped.");
        }
    }

    @Nested
    @DisplayName("DROP Commands")
    class DropCommandsTests {
        @BeforeEach
        public void setupDrop() {
            // Create a new database and table to test drop commands.
            sendCommand("CREATE DATABASE dropSchool;");
            sendCommand("USE dropSchool;");
            sendCommand("CREATE TABLE students (name, score, passed);");
            sendCommand("INSERT INTO students VALUES ('Eve', 88, TRUE);");
            sendCommand("INSERT INTO students VALUES ('Frank', 70, TRUE);");
        }

        @Test
        @DisplayName("Test DROP TABLE and DROP DATABASE")
        public void testDrop() {
            String response = sendCommand("DROP TABLE students;");
            if (response.contains("[OK]")) {
                System.out.println("DROP TABLE students: Success");
            } else {
                fail("DROP TABLE students failed. Response: " + response);
            }

            response = sendCommand("DROP DATABASE dropSchool;");
            if (response.contains("[OK]")) {
                System.out.println("DROP DATABASE dropSchool: Success");
            } else {
                fail("DROP DATABASE dropSchool failed. Response: " + response);
            }
        }
    }

    @Nested
    @DisplayName("Error Handling and Invalid Syntax Tests")
    class ErrorHandlingTests {
        @BeforeEach
        public void setupErrorTests() {
            sendCommand("DROP DATABASE errorSchool;");
            sendCommand("CREATE DATABASE errorSchool;");
            sendCommand("USE errorSchool;");
            sendCommand("CREATE TABLE test (col1, col2);");
            sendCommand("INSERT INTO test VALUES ('X', 1);");
        }

        @Test
        @DisplayName("Test Missing Semicolon Error")
        public void testMissingSemicolon() {
            String response = sendCommand("SELECT * FROM test");
            assertTrue(response.contains("[ERROR]"), "Missing semicolon should produce an error.");
        }

        @Test
        @DisplayName("Test Non-existent Table Error")
        public void testNonExistentTable() {
            String response = sendCommand("SELECT * FROM nonExistentTable;");
            assertTrue(response.contains("[ERROR]"), "Querying a non-existent table should produce an error.");
        }

        @Test
        @DisplayName("Test Non-existent Attribute Error")
        public void testNonExistentAttribute() {
            String response = sendCommand("SELECT nonExistentAttr FROM test;");
            assertTrue(response.contains("[ERROR]"), "Querying a non-existent attribute should produce an error.");
        }

        @Test
        @DisplayName("Test Invalid CREATE command (typo in keyword)")
        public void testInvalidCreate() {
            String response = sendCommand("CREA DATABASE invalidDB;");
            assertTrue(response.contains("[ERROR]"), "A typo in the CREATE command should produce an error.");
        }

        @Test
        @DisplayName("Test Invalid attribute list in CREATE TABLE (missing commas)")
        public void testInvalidAttributeList() {
            String response = sendCommand("CREATE TABLE invalid (col1 col2);");
            assertTrue(response.contains("[ERROR]"), "Missing commas in the attribute list should produce an error.");
        }

        @Test
        @DisplayName("Test Invalid INSERT command (missing closing parenthesis)")
        public void testInvalidInsert() {
            String response = sendCommand("INSERT INTO test VALUES ('Alice', 90, TRUE;");
            assertTrue(response.contains("[ERROR]"), "An INSERT command missing a closing parenthesis should produce an error.");
        }

        @Test
        @DisplayName("Test Invalid condition in SELECT (missing value after comparator)")
        public void testInvalidSelectCondition() {
            String response = sendCommand("SELECT * FROM test WHERE col1 == ;");
            assertTrue(response.contains("[ERROR]"), "An incomplete condition should produce an error.");
        }

        @Test
        @DisplayName("Test Invalid JOIN command syntax (missing keywords)")
        public void testInvalidJoinSyntax() {
            String response = sendCommand("JOIN test AND test col1 AND col2;");
            assertTrue(response.contains("[ERROR]"), "An incorrectly formatted JOIN command should produce an error.");
        }

        @Test
        @DisplayName("Test Invalid command with extra whitespace inside tokens")
        public void testInvalidExtraWhitespaceInsideTokens() {
            String response = sendCommand("C R E A T E DATABASE invalidDB;");
            assertTrue(response.contains("[ERROR]"), "Extra whitespace within a token should produce an error.");
        }
    }

    @Nested
    @DisplayName("Complex Conditions Tests")
    class ComplexConditionsTests {
        @BeforeEach
        public void setupComplex() {
            // Create a database and table with sample data for condition tests.
            sendCommand("DROP DATABASE complexDB;");
            sendCommand("CREATE DATABASE complexDB;");
            sendCommand("USE complexDB;");
            sendCommand("CREATE TABLE numbers (num, flag);");
            // Insert rows: (5, TRUE), (10, FALSE), (15, TRUE), (20, FALSE)
            sendCommand("INSERT INTO numbers VALUES (5, TRUE);");
            sendCommand("INSERT INTO numbers VALUES (10, FALSE);");
            sendCommand("INSERT INTO numbers VALUES (15, TRUE);");
            sendCommand("INSERT INTO numbers VALUES (20, FALSE);");
        }

        @Test
        @DisplayName("Test complex nested condition with multiple embedded brackets")
        public void testComplexNestedConditions() {
            // Query: SELECT * FROM numbers WHERE ((num < 15) AND (flag == TRUE)) OR (num == 20);
            // Expected rows: row with num=5 (5<15 and TRUE) and row with num=20.
            String response = sendCommand("SELECT * FROM numbers WHERE ((num < 15) AND (flag == TRUE)) OR (num == 20);");
            assertTrue(response.contains("[OK]"), "Complex nested condition should return [OK].");
            // Check that the row with num 5 and the row with num 20 appear.
            assertTrue(response.contains("5"), "Expected row with num=5 in result.");
            assertTrue(response.contains("20"), "Expected row with num=20 in result.");
            // Ensure that rows with num=10 and num=15 do not appear.
            assertFalse(response.contains("10"), "Row with num=10 should not be returned.");
            assertFalse(response.contains("15"), "Row with num=15 should not be returned.");
        }

        @Test
        @DisplayName("Test condition spacing variations in complex conditions")
        public void testConditionSpacingVariationsComplex() {
            // Test a query with conditions that have minimal spacing.
            // For instance: SELECT * FROM numbers WHERE num>10 AND flag==FALSE;
            // Expected row: row with num=20 (since 20 > 10 and flag is FALSE)
            String response = sendCommand("SELECT * FROM numbers WHERE num>10 AND flag==FALSE;");
            assertTrue(response.contains("[OK]"), "Complex condition with minimal spacing should return [OK].");
            assertTrue(response.contains("20"), "Expected row with num=20 in result.");
            // Ensure other rows are not returned.
            assertFalse(response.contains("10"), "Row with num=10 should not be returned.");
            assertFalse(response.contains("15"), "Row with num=15 should not be returned.");
            assertFalse(response.contains("5"), "Row with num=5 should not be returned.");
        }
    }

    @Nested
    @DisplayName("Duplicate Creation Tests")
    class DuplicateCreationTests {
        @Test
        @DisplayName("Test duplicate database creation")
        public void testDuplicateDatabaseCreation() {
            // First creation should succeed.
            String response = sendCommand("CREATE DATABASE dupDB;");
            assertTrue(response.contains("[OK]"), "First creation of dupDB should succeed.");

            // Attempt to create the same database again.
            response = sendCommand("CREATE DATABASE dupDB;");
            assertTrue(response.contains("[ERROR]"), "Duplicate creation of dupDB should produce an error.");
        }

        @Test
        @DisplayName("Test duplicate table creation in the same database")
        public void testDuplicateTableCreation() {
            String response = sendCommand("CREATE DATABASE dupDB2;");
            assertTrue(response.contains("[OK]"), "Creation of dupDB2 should succeed.");

            response = sendCommand("USE dupDB2;");
            assertTrue(response.contains("[OK]"), "USE dupDB2 should succeed.");

            response = sendCommand("CREATE TABLE dupTable (col1, col2);");
            assertTrue(response.contains("[OK]"), "First creation of dupTable should succeed.");

            response = sendCommand("CREATE TABLE dupTable (col1, col2);");
            assertTrue(response.contains("[ERROR]"), "Duplicate creation of dupTable should produce an error.");
        }

        @Test
        @DisplayName("Test duplicate column names within the same table")
        public void testDuplicateColumnNamesWithinSameTable() {
            String response = sendCommand("CREATE TABLE dupColumns (col1, col1);");
            assertTrue(response.contains("[ERROR]"), "Creating a table with duplicate column names should produce an error.");
        }

        @Test
        @DisplayName("Test same column names across different tables are allowed")
        public void testSameColumnNamesAcrossDifferentTables() {
            String response = sendCommand("CREATE DATABASE dupColumnsDB;");
            assertTrue(response.contains("[OK]"), "Creation of dupColumnsDB should succeed.");

            response = sendCommand("USE dupColumnsDB;");
            assertTrue(response.contains("[OK]"), "USE dupColumnsDB should succeed.");

            response = sendCommand("CREATE TABLE table1 (col1, col2);");
            assertTrue(response.contains("[OK]"), "Creation of table1 should succeed.");

            response = sendCommand("CREATE TABLE table2 (col1, col2);");
            assertTrue(response.contains("[OK]"), "Creation of table2 should succeed, even if column names duplicate across tables.");
        }
    }

    @Nested
    @DisplayName("Complex Conditions Tests")
    class MoreComplexConditionTests {
        @BeforeEach
        public void setupComplex() {
            // Create a database and table for complex condition tests.
            sendCommand("DROP DATABASE moreComplexDB;");
            sendCommand("CREATE DATABASE moreComplexDB;");
            sendCommand("USE moreComplexDB;");
            sendCommand("CREATE TABLE numbers (num, flag, category);");
            // Insert sample data:
            // Row 1: num=5, flag=TRUE, category='A'
            // Row 2: num=10, flag=FALSE, category='B'
            // Row 3: num=15, flag=TRUE, category='B'
            // Row 4: num=20, flag=FALSE, category='A'
            // Row 5: num=25, flag=TRUE, category='C'
            sendCommand("INSERT INTO numbers VALUES (5, TRUE, 'A');");
            sendCommand("INSERT INTO numbers VALUES (10, FALSE, 'B');");
            sendCommand("INSERT INTO numbers VALUES (15, TRUE, 'B');");
            sendCommand("INSERT INTO numbers VALUES (20, FALSE, 'A');");
            sendCommand("INSERT INTO numbers VALUES (25, TRUE, 'C');");

        }

        @Test
        @DisplayName("Test simple nested condition: (num < 15 AND flag == TRUE)")
        public void testSimpleNestedCondition() {
            // Expected: Only row 1 (num=5) qualifies.
            String response = sendCommand("SELECT * FROM numbers WHERE (num < 15 AND flag == TRUE);");
            assertTrue(response.contains("[OK]"), "Nested condition query should return [OK].");
            assertTrue(response.contains("5"), "Expected row with num=5 in result.");
            assertFalse(response.contains("10"), "Row with num=10 should not be returned.");
            assertFalse(response.contains("15"), "Row with num=15 should not be returned.");

            response = sendCommand("SELECT * FROM numbers WHERE ((num < 15 AND flag == TRUE) OR (num > 20 AND category == 'C'));");
            assertTrue(response.contains("[OK]"), "Double nested condition query should return [OK].");
            assertTrue(response.contains("5"), "Expected row with num=5 in result.");
            assertTrue(response.contains("25"), "Expected row with num=25 in result.");
            // Ensure rows with num=10, 15, and 20 are not returned.
            assertFalse(response.contains("10"), "Row with num=10 should not be returned.");
            assertFalse(response.contains("15"), "Row with num=15 should not be returned.");
            assertFalse(response.contains("20"), "Row with num=20 should not be returned.");

            response = sendCommand("SELECT * FROM numbers WHERE (((num >= 5 AND num <= 25) AND (flag == TRUE)) OR (category == 'B'));");
            assertTrue(response.contains("[OK]"), "Deeply nested condition query should return [OK].");
            assertTrue(response.contains("5"), "Expected row with num=5 in result.");
            assertTrue(response.contains("10"), "Expected row with num=10 in result (via category 'B').");
            assertTrue(response.contains("15"), "Expected row with num=15 in result.");
            assertTrue(response.contains("25"), "Expected row with num=25 in result.");

            response = sendCommand("SELECT * FROM numbers WHERE num>10ANDflag==FALSE;");
            assertFalse(response.contains("[OK]"), "Complex condition without spaces between conditions should not be accepted.");
            assertFalse(response.contains("20"), "No expected row with num=20 in result.");
            // Ensure other rows (num=5, 15, 25) are not returned.
            assertFalse(response.contains("5"), "Row with num=5 should not be returned.");
            assertFalse(response.contains("15"), "Row with num=15 should not be returned.");
            assertFalse(response.contains("25"), "Row with num=25 should not be returned.");
        }

    }
}
