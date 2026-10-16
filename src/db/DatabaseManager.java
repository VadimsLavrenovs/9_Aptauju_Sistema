/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package db;

import java.sql.*;

/**
 *
 * @author Maksims Pļehanovs, Vadims Lavrenovs
 */
public class DatabaseManager {

    private static final String URL = "jdbc:derby:database/appdb;create=true";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    public static void init() {
        try (Connection conn = getConnection()) {
            DatabaseMetaData meta = conn.getMetaData();

            ResultSet user = meta.getTables(null, null, "APP_USER", null);
            if (!user.next()) {
                conn.createStatement().executeUpdate(
                        "CREATE TABLE app_user ("
                        + "userid INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY, "
                        + "username VARCHAR(50) NOT NULL UNIQUE, "
                        + "password VARCHAR(15) NOT NULL, "
                        + "role VARCHAR(11) NOT NULL)"
                );
                java.sql.Statement stmt = conn.createStatement();
                stmt.executeUpdate("INSERT INTO app_user (username, password, role) VALUES ('janis', 'Parole123', 'Interviewer')");
                stmt.executeUpdate("INSERT INTO app_user (username, password, role) VALUES ('liene', 'SafePass99', 'Interviewer')");
                stmt.executeUpdate("INSERT INTO app_user (username, password, role) VALUES ('andris', 'ManaParole1', 'Respondent')");
                stmt.executeUpdate("INSERT INTO app_user (username, password, role) VALUES ('ieva', 'Zemene2026', 'Respondent')");
                stmt.executeUpdate("INSERT INTO app_user (username, password, role) VALUES ('peteris', 'Koks12345', 'Respondent')");
                stmt.executeUpdate("INSERT INTO app_user (username, password, role) VALUES ('anna', 'Sveiks2026', 'Respondent')");
                stmt.executeUpdate("INSERT INTO app_user (username, password, role) VALUES ('maris', 'Rudens777', 'Respondent')");
                stmt.executeUpdate("INSERT INTO app_user (username, password, role) VALUES ('kristine', 'ZilaDebess', 'Respondent')");
                stmt.executeUpdate("INSERT INTO app_user (username, password, role) VALUES ('edgars', 'ParoleX', 'Respondent')");
                stmt.executeUpdate("INSERT INTO app_user (username, password, role) VALUES ('santa', 'Laimigs12', 'Respondent')");
                System.out.println("Tabula 'app_user' veiksmīgi izveidota!");
            }

            ResultSet survey = meta.getTables(null, null, "SURVEY", null);
            if (!survey.next()) {
                conn.createStatement().executeUpdate(
                        "CREATE TABLE survey ("
                        + "surveyid INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY, "
                        + "title VARCHAR(50) NOT NULL, "
                        + "user_id INT NOT NULL, "
                        + "FOREIGN KEY (user_id) REFERENCES app_user(userid))"
                );
                java.sql.Statement stmt = conn.createStatement();
                stmt.executeUpdate("INSERT INTO survey (title, user_id) VALUES ('Kafijas lietošanas paradumi', 1)");
                stmt.executeUpdate("INSERT INTO survey (title, user_id) VALUES ('Sporta aktivitātes ikdienā', 1)");
                stmt.executeUpdate("INSERT INTO survey (title, user_id) VALUES ('Attālinātā darba efektivitāte', 2)");
                stmt.executeUpdate("INSERT INTO survey (title, user_id) VALUES ('Mājdzīvnieku aprūpe', 2)");
                stmt.executeUpdate("INSERT INTO survey (title, user_id) VALUES ('Iepirkšanās internetā', 1)");
                stmt.executeUpdate("INSERT INTO survey (title, user_id) VALUES ('Ceļošanas galamērķi', 2)");
                stmt.executeUpdate("INSERT INTO survey (title, user_id) VALUES ('Veselīgs uzturs', 1)");
                stmt.executeUpdate("INSERT INTO survey (title, user_id) VALUES ('Automašīnu izvēles kritēriji', 2)");
                stmt.executeUpdate("INSERT INTO survey (title, user_id) VALUES ('Viedierīču lietošanas laiks', 1)");
                stmt.executeUpdate("INSERT INTO survey (title, user_id) VALUES ('Kultūras pasākumu apmeklētība', 2)");
                System.out.println("Tabula 'survey' veiksmīgi izveidota!");
            }

            ResultSet question = meta.getTables(null, null, "QUESTION", null);
            if (!question.next()) {
                conn.createStatement().executeUpdate(
                        "CREATE TABLE question ("
                        + "questionid INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY, "
                        + "question VARCHAR(100) NOT NULL UNIQUE, "
                        + "answer_1 VARCHAR(50) NOT NULL, "
                        + "answer_2 VARCHAR(50) NOT NULL, "
                        + "answer_3 VARCHAR(50) NOT NULL, "
                        + "survey_id INT NOT NULL, "
                        + "FOREIGN KEY (survey_id) REFERENCES survey(surveyid))"
                );
                java.sql.Statement stmt = conn.createStatement();
                stmt.executeUpdate("INSERT INTO question (question, answer_1, answer_2, answer_3, survey_id) VALUES ('Cik bieži Jūs dzerat kafiju?', 'Katru dienu', 'Reizi nedēļā', 'Nedzeru vispār', 1)");
                stmt.executeUpdate("INSERT INTO question (question, answer_1, answer_2, answer_3, survey_id) VALUES ('Kāda ir Jūsu mīļākā kafija?', 'Melnā kafija', 'Kapučīno', 'Late', 1)");
                stmt.executeUpdate("INSERT INTO question (question, answer_1, answer_2, answer_3, survey_id) VALUES ('Kur parasti pērkat kafiju?', 'DUS', 'Kafejnīcā', 'Gatavoju mājās', 1)");
                stmt.executeUpdate("INSERT INTO question (question, answer_1, answer_2, answer_3, survey_id) VALUES ('Cik bieži Jūs sportojat?', 'Katru dienu', '2-3 reizes nedēļā', 'Nesportoju', 2)");
                stmt.executeUpdate("INSERT INTO question (question, answer_1, answer_2, answer_3, survey_id) VALUES ('Kāds ir Jūsu mīļākais sports?', 'Skriešana', 'Peldēšana', 'Basketbols', 2)");
                stmt.executeUpdate("INSERT INTO question (question, answer_1, answer_2, answer_3, survey_id) VALUES ('Kur Jūs sportojat?', 'Sporta zālē', 'Brīvā dabā', 'Mājās', 2)");
                stmt.executeUpdate("INSERT INTO question (question, answer_1, answer_2, answer_3, survey_id) VALUES ('Vai lietojat cukuru pie kafijas?', 'Jā, vienu karoti', 'Jā, vairāk', 'Nē, nelietoju', 1)");
                stmt.executeUpdate("INSERT INTO question (question, answer_1, answer_2, answer_3, survey_id) VALUES ('Kurā dienas laikā sportojat?', 'No rīta', 'Pa dienu', 'Vakarā', 2)");
                stmt.executeUpdate("INSERT INTO question (question, answer_1, answer_2, answer_3, survey_id) VALUES ('Cik tērējat kafijai mēnesī?', 'Līdz 10 EUR', '10-30 EUR', 'Vairāk', 1)");
                stmt.executeUpdate("INSERT INTO question (question, answer_1, answer_2, answer_3, survey_id) VALUES ('Vai sportojat vienatnē?', 'Jā, vienatnē', 'Kopā ar draugiem', 'Grupā', 2)");
                System.out.println("Tabula 'question' veiksmīgi izveidota!");
            }

            ResultSet respondent_question = meta.getTables(null, null, "RESPONDENT_QUESTION", null);
            if (!respondent_question.next()) {
                conn.createStatement().executeUpdate(
                        "CREATE TABLE respondent_question ("
                        + "respondent_questionid INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY, "
                        + "answer VARCHAR(50), "
                        + "user_id INT NOT NULL, "
                        + "FOREIGN KEY (user_id) REFERENCES app_user(userid), "
                        + "question_id INT NOT NULL, "
                        + "FOREIGN KEY (question_id) REFERENCES question(questionid))"
                );
                java.sql.Statement stmt = conn.createStatement();
                stmt.executeUpdate("INSERT INTO respondent_question (answer, user_id, question_id) VALUES ('Katru dienu', 3, 1)");
                stmt.executeUpdate("INSERT INTO respondent_question (answer, user_id, question_id) VALUES ('Kapučīno', 4, 2)");
                stmt.executeUpdate("INSERT INTO respondent_question (answer, user_id, question_id) VALUES ('Gatavoju mājās', 5, 3)");
                stmt.executeUpdate("INSERT INTO respondent_question (answer, user_id, question_id) VALUES ('2-3 reizes nedēļā', 6, 4)");
                stmt.executeUpdate("INSERT INTO respondent_question (answer, user_id, question_id) VALUES ('Skriešana', 7, 5)");
                stmt.executeUpdate("INSERT INTO respondent_question (answer, user_id, question_id) VALUES ('Brīvā dabā', 8, 6)");
                stmt.executeUpdate("INSERT INTO respondent_question (answer, user_id, question_id) VALUES ('Nē, nelietoju', 9, 7)");
                stmt.executeUpdate("INSERT INTO respondent_question (answer, user_id, question_id) VALUES ('Vakarā', 10, 8)");
                stmt.executeUpdate("INSERT INTO respondent_question (answer, user_id, question_id) VALUES ('10-30 EUR', 3, 9)");
                stmt.executeUpdate("INSERT INTO respondent_question (answer, user_id, question_id) VALUES ('Jā, vienatnē', 4, 10)");
                System.out.println("Tabula 'respondent_question' veiksmīgi izveidota!");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
