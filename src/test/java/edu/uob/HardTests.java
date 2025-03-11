package edu.uob;

import org.junit.jupiter.api.*;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class HardTests {

    private DBServer server;

    @BeforeEach
    public void setup() throws IOException {
        server = new DBServer();
        server.handleCommand("CREATE DATABASE test_db;");
        server.handleCommand("USE test_db;");
    }

    @Test
    public void testDatabaseCreationEdgeCases() throws IOException {
        // Test CREATE DATABASE with edge cases
        String createTestDbResponse = server.handleCommand("CREATE DATABASE test_db;");
        assertTrue(createTestDbResponse.contains("[ERROR]")); // Should fail as test_db already exists

        String createTestDb123Response = server.handleCommand("CREATE DATABASE test_db_123;");
        assertTrue(createTestDb123Response.contains("[OK]"));

        String createTestDollarDbResponse = server.handleCommand("CREATE DATABASE test$db;");
        assertTrue(createTestDollarDbResponse.contains("[OK]"));

        String createTestSpaceDbResponse = server.handleCommand("CREATE DATABASE \"test db\";");
        assertTrue(createTestSpaceDbResponse.contains("[OK]"));

        String createNullDbResponse = server.handleCommand("CREATE DATABASE NULL;");
        assertTrue(createNullDbResponse.contains("[ERROR]")); // NULL is not a valid database name
    }

    @Test
    public void testTableCreationEdgeCases() throws IOException, Exception {
        // Test CREATE TABLE with edge cases
        String createUsersTableResponse = server.handleCommand("CREATE TABLE users;");
        assertTrue(createUsersTableResponse.contains("[OK]"));

        String createEmployeesTableResponse = server.handleCommand("CREATE TABLE employees (id INT, name VARCHAR(100), age INT, salary FLOAT);");
        assertTrue(createEmployeesTableResponse.contains("[OK]"));

        String createTestTableResponse = server.handleCommand("CREATE TABLE test_table (col1 INT, col2 FLOAT, col3 TEXT, col4 BOOLEAN);");
        assertTrue(createTestTableResponse.contains("[OK]"));

        String createEmptyTableResponse = server.handleCommand("CREATE TABLE empty_table ();");
        assertTrue(createEmptyTableResponse.contains("[OK]"));

        String createSpecialCharsTableResponse = server.handleCommand("CREATE TABLE special_chars (col1 INT, col2 VARCHAR(50), col3 \"odd-column\");");
        assertTrue(createSpecialCharsTableResponse.contains("[OK]"));
    }

    @Test
    public void testTableDroppingEdgeCases() throws IOException, Exception {
        // Test dropping tables
        String dropUsersTableResponse = server.handleCommand("DROP TABLE users;");
        assertTrue(dropUsersTableResponse.contains("[OK]"));

        String dropEmployeesTableIfExistsResponse = server.handleCommand("DROP TABLE IF EXISTS employees;");
        assertTrue(dropEmployeesTableIfExistsResponse.contains("[OK]"));

        String dropNonExistentTableResponse = server.handleCommand("DROP TABLE non_existent_table;");
        assertTrue(dropNonExistentTableResponse.contains("[ERROR]"));
    }

    @Test
    public void testTableAlterationsEdgeCases() throws IOException, Exception {
        // Test ALTER TABLE with unexpected values
        String alterAddColumnResponse = server.handleCommand("ALTER TABLE employees ADD column_new INT;");
        assertTrue(alterAddColumnResponse.contains("[OK]"));

        String alterDropAgeColumnResponse = server.handleCommand("ALTER TABLE employees DROP age;");
        assertTrue(alterDropAgeColumnResponse.contains("[OK]"));

        String alterAddStrangeNameColumnResponse = server.handleCommand("ALTER TABLE test_table ADD \"strange name\" TEXT;");
        assertTrue(alterAddStrangeNameColumnResponse.contains("[OK]"));

        String alterDropNonExistentColumnResponse = server.handleCommand("ALTER TABLE test_table DROP column_that_does_not_exist;");
        assertTrue(alterDropNonExistentColumnResponse.contains("[ERROR]"));
    }

    @Test
    public void testInsertingTrickyValues() throws IOException, Exception {
        // Test INSERT with tricky values
        String insertJohnResponse = server.handleCommand("INSERT INTO employees VALUES (1, 'John Doe', 30, 55000.50);");
        assertTrue(insertJohnResponse.contains("[OK]"));

        String insertJaneResponse = server.handleCommand("INSERT INTO employees VALUES (2, 'Jane \"The Boss\" Doe', 40, NULL);");
        assertTrue(insertJaneResponse.contains("[OK]"));

        String insertOReillyResponse = server.handleCommand("INSERT INTO employees VALUES (3, 'O\'Reilly', 25, 60000.99);");
        assertTrue(insertOReillyResponse.contains("[OK]"));

        String insertZhangWeiResponse = server.handleCommand("INSERT INTO employees VALUES (4, '张伟', 35, 50000);");
        assertTrue(insertZhangWeiResponse.contains("[OK]"));

        String insertEmptyNameResponse = server.handleCommand("INSERT INTO employees VALUES (5, '', -1, -9999.99);");
        assertTrue(insertEmptyNameResponse.contains("[OK]"));
    }

    @Test
    public void testComplexSelectStatements() throws IOException, Exception {
        // Test SELECT queries with different conditions
        String selectAllEmployeesResponse = server.handleCommand("SELECT * FROM employees;");
        assertTrue(selectAllEmployeesResponse.contains("id\tname\tage\tsalary"));

        String selectEmployeesOlderThan30Response = server.handleCommand("SELECT name, salary FROM employees WHERE age > 30;");
        assertTrue(selectEmployeesOlderThan30Response.contains("name"));
        assertTrue(selectEmployeesOlderThan30Response.contains("salary"));

        String selectEmployeesWithDoeResponse = server.handleCommand("SELECT * FROM employees WHERE name LIKE '%Doe%';");
        assertTrue(selectEmployeesWithDoeResponse.contains("John Doe"));
        assertTrue(selectEmployeesWithDoeResponse.contains("Jane \"The Boss\" Doe"));

        String selectEmployeesWithSalaryAndAgeResponse = server.handleCommand("SELECT * FROM employees WHERE salary >= 50000 AND age < 40;");
        assertTrue(selectEmployeesWithSalaryAndAgeResponse.contains("id\tname\tage\tsalary"));

        String selectEmployeesWithComplexConditionResponse = server.handleCommand("SELECT * FROM employees WHERE (age > 20 OR salary < 60000) AND name != 'John';");
        assertTrue(selectEmployeesWithComplexConditionResponse.contains("id\tname\tage\tsalary"));

        String selectEmployeesWithNestedConditionResponse = server.handleCommand("SELECT * FROM employees WHERE age > 30 OR (salary < 40000 AND name LIKE 'J%');");
        assertTrue(selectEmployeesWithNestedConditionResponse.contains("id\tname\tage\tsalary"));
    }

    @Test
    public void testExtremeSelectConditions() throws IOException, Exception {
        // Test extreme conditions for SELECT
        String selectLargeAgeResponse = server.handleCommand("SELECT * FROM employees WHERE age > 9999999999;");
        assertTrue(selectLargeAgeResponse.contains("[ERROR]"));

        String selectNegativeSalaryResponse = server.handleCommand("SELECT * FROM employees WHERE salary < -9999999999;");
        assertTrue(selectNegativeSalaryResponse.contains("[ERROR]"));

        String selectNameWithManyAResponse = server.handleCommand("SELECT * FROM employees WHERE name LIKE 'a%a%a%a%a%a%a%a%a%a%a%';");
        assertTrue(selectNameWithManyAResponse.contains("[ERROR]"));

        String selectNullNameResponse = server.handleCommand("SELECT * FROM employees WHERE name == NULL;");
        assertTrue(selectNullNameResponse.contains("[ERROR]"));

        String selectInvalidAgeAndNameResponse = server.handleCommand("SELECT * FROM employees WHERE age > 30 AND name LIKE 'J%' OR salary < 0;");
        assertTrue(selectInvalidAgeAndNameResponse.contains("[ERROR]"));
    }

    @Test
    public void testUpdateWithEdgeCases() throws IOException, Exception {
        // Test UPDATE with edge cases
        String updateSalaryResponse = server.handleCommand("UPDATE employees SET salary = salary * 1.10 WHERE age > 30;");
        assertTrue(updateSalaryResponse.contains("[OK]"));

        String updateNullNameResponse = server.handleCommand("UPDATE employees SET name = 'Unknown' WHERE name IS NULL;");
        assertTrue(updateNullNameResponse.contains("[OK]"));

        String updateAgeToLargeValueResponse = server.handleCommand("UPDATE employees SET age = 999999999 WHERE name = 'John';");
        assertTrue(updateAgeToLargeValueResponse.contains("[OK]"));

        String updateInvalidSalaryResponse = server.handleCommand("UPDATE employees SET salary = -1 WHERE name = 'Jane \"The Boss\" Doe';");
        assertTrue(updateInvalidSalaryResponse.contains("[OK]"));
    }

    @Test
    public void testDeleteWithTrickyConditions() throws IOException, Exception {
        // Test DELETE with tricky conditions
        String deleteNegativeAgeResponse = server.handleCommand("DELETE FROM employees WHERE age < 0;");
        assertTrue(deleteNegativeAgeResponse.contains("[OK]"));

        String deleteNullSalaryResponse = server.handleCommand("DELETE FROM employees WHERE salary == NULL;");
        assertTrue(deleteNullSalaryResponse.contains("[OK]"));

        String deleteNameLikeJohnResponse = server.handleCommand("DELETE FROM employees WHERE name LIKE 'John%';");
        assertTrue(deleteNameLikeJohnResponse.contains("[OK]"));

        String deleteUnrealisticAgeResponse = server.handleCommand("DELETE FROM employees WHERE age > 1000;");
        assertTrue(deleteUnrealisticAgeResponse.contains("[ERROR]"));
    }
}