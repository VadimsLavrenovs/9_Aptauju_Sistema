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
            createTables(conn);
            seedUsers(conn);
            seedSurveys(conn);
            seedQuestions(conn);
            seedAnswers(conn);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Pārbauda, vai tabula eksistē
    private static boolean tableExists(Connection conn, String tableName)
            throws SQLException {

        DatabaseMetaData meta = conn.getMetaData();

        try (ResultSet rs = meta.getTables(
                null, null, tableName.toUpperCase(), new String[]{"TABLE"})) {
            return rs.next();
        }
    }

    // Izveido tabulas, ja tās vēl neeksistē
    private static void createTables(Connection conn) throws SQLException {

        if (!tableExists(conn, "APP_USER")) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate(
                        "CREATE TABLE app_user ("
                        + "userid INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY, "
                        + "username VARCHAR(50) NOT NULL UNIQUE, "
                        + "password VARCHAR(15) NOT NULL, "
                        + "role VARCHAR(11) NOT NULL)"
                );
            }
        }

        if (!tableExists(conn, "SURVEY")) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate(
                        "CREATE TABLE survey ("
                        + "surveyid INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY, "
                        + "title VARCHAR(50) NOT NULL, "
                        + "user_id INT NOT NULL, "
                        + "FOREIGN KEY (user_id) REFERENCES app_user(userid))"
                );
            }
        }

        if (!tableExists(conn, "QUESTION")) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate(
                        "CREATE TABLE question ("
                        + "questionid INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY, "
                        + "question VARCHAR(100) NOT NULL UNIQUE, "
                        + "answer_1 VARCHAR(50) NOT NULL, "
                        + "answer_2 VARCHAR(50) NOT NULL, "
                        + "answer_3 VARCHAR(50) NOT NULL, "
                        + "survey_id INT NOT NULL, "
                        + "FOREIGN KEY (survey_id) REFERENCES survey(surveyid))"
                );
            }
        }

        if (!tableExists(conn, "RESPONDENT_QUESTION")) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate(
                        "CREATE TABLE respondent_question ("
                        + "respondent_questionid INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY, "
                        + "answer VARCHAR(50), "
                        + "user_id INT NOT NULL, "
                        + "FOREIGN KEY (user_id) REFERENCES app_user(userid), "
                        + "question_id INT NOT NULL, "
                        + "FOREIGN KEY (question_id) REFERENCES question(questionid))"
                );
            }
        }
    }

    // Pievieno visus lietotājus, ja tie vēl neeksistē
    private static void seedUsers(Connection conn) throws SQLException {
        addUser(conn, "janis", "Parole123", "Interviewer");
        addUser(conn, "liene", "SafePass99", "Interviewer");
        addUser(conn, "andris", "parole", "Respondent");
        addUser(conn, "ieva", "Zemene2026", "Respondent");
        addUser(conn, "peteris", "Koks12345", "Respondent");
        addUser(conn, "anna", "Sveiks2026", "Respondent");
        addUser(conn, "maris", "Rudens777", "Respondent");
        addUser(conn, "kristine", "ZilaDebess", "Respondent");
        addUser(conn, "edgars", "ParoleX", "Respondent");
        addUser(conn, "santa", "Laimigs12", "Respondent");
        addUser(conn, "admin", "parole", "Admin");
        addUser(conn, "NikitaShalkovski", "parole", "Interviewer");
        addUser(conn, "Vadimka", "parole", "Interviewer");
        addUser(conn, "inter", "parole", "Interviewer");
        addUser(conn, "i", "parole", "Interviewer");
        addUser(conn, "Maksimka", "parole", "Respondent");
        addUser(conn, "resp", "parole", "Respondent");
        addUser(conn, "andrejs", "parole", "Respondent");
        addUser(conn, "jevgenijs", "parole", "Respondent");
        addUser(conn, "Kirils", "parole", "Respondent");
        addUser(conn, "Danils", "parole", "Respondent");
        addUser(conn, "Ivans", "parole", "Respondent");
        addUser(conn, "Deniss", "parole", "Respondent");
        addUser(conn, "r", "parole", "Respondent");
    }

    private static void addUser(Connection conn, String username,
            String password, String role) throws SQLException {

        String check = "SELECT userid FROM app_user WHERE username = ?";

        try (PreparedStatement ps = conn.prepareStatement(check)) {
            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return;
                }
            }
        }

        String sql = "INSERT INTO app_user (username, password, role) "
                + "VALUES (?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            ps.setString(3, role);
            ps.executeUpdate();
        }
    }

    // Iegūst lietotāja ID pēc lietotājvārda
    private static int getUserId(Connection conn, String username)
            throws SQLException {

        String sql = "SELECT userid FROM app_user WHERE username = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("userid");
                }
            }
        }

        throw new SQLException("Nav atrasts lietotājs: " + username);
    }

    // Pievieno aptaujas, ja tās vēl neeksistē
    private static void seedSurveys(Connection conn) throws SQLException {

        // Sākotnējās aptaujas
        addSurvey(conn, "Kafijas lietošanas paradumi", "janis");
        addSurvey(conn, "Sporta aktivitātes ikdienā", "janis");
        addSurvey(conn, "Attālinātā darba efektivitāte", "liene");
        addSurvey(conn, "Mājdzīvnieku aprūpe", "liene");
        addSurvey(conn, "Iepirkšanās internetā", "janis");
        addSurvey(conn, "Ceļošanas galamērķi", "liene");
        addSurvey(conn, "Veselīgs uzturs", "janis");
        addSurvey(conn, "Automašīnu izvēles kritēriji", "liene");
        addSurvey(conn, "Viedierīču lietošanas laiks", "janis");
        addSurvey(conn, "Kultūras pasākumu apmeklētība", "liene");
        addSurvey(conn, "Mūzikas klausīšanās paradumi", "NikitaShalkovski");
        addSurvey(conn, "Brīvā laika pavadīšana", "NikitaShalkovski");
        addSurvey(conn, "Mācību paradumi", "Vadimka");
        addSurvey(conn, "Tehnoloģiju izmantošana", "Vadimka");
        addSurvey(conn, "Ikdienas pārvietošanās", "inter");
        addSurvey(conn, "Atpūtas paradumi", "inter");
        addSurvey(conn, "Ēdiena izvēles paradumi", "i");
        addSurvey(conn, "Sociālo tīklu izmantošana", "i");
    }

    private static void addSurvey(Connection conn, String title,
            String interviewer) throws SQLException {

        String check = "SELECT surveyid FROM survey WHERE title = ?";

        try (PreparedStatement ps = conn.prepareStatement(check)) {
            ps.setString(1, title);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return;
                }
            }
        }

        String sql = "INSERT INTO survey (title, user_id) VALUES (?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, title);
            ps.setInt(2, getUserId(conn, interviewer));
            ps.executeUpdate();
        }
    }

    // Iegūst aptaujas ID pēc nosaukuma
    private static int getSurveyId(Connection conn, String title)
            throws SQLException {

        String sql = "SELECT surveyid FROM survey WHERE title = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, title);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("surveyid");
                }
            }
        }

        throw new SQLException("Nav atrasta aptauja: " + title);
    }

    // Pievieno jautājumus visām aptaujām
    private static void seedQuestions(Connection conn) throws SQLException {

        String[][] data = {
            {"Kafijas lietošanas paradumi", "Cik bieži Jūs dzerat kafiju?", "Katru dienu", "Reizi nedēļā", "Nedzeru vispār"},
            {"Kafijas lietošanas paradumi", "Kāda ir Jūsu mīļākā kafija?", "Melnā kafija", "Kapučīno", "Late"},
            {"Sporta aktivitātes ikdienā", "Cik bieži Jūs sportojat?", "Katru dienu", "2-3 reizes nedēļā", "Nesportoju"},
            {"Sporta aktivitātes ikdienā", "Kāds ir Jūsu mīļākais sports?", "Skriešana", "Peldēšana", "Basketbols"},
            {"Attālinātā darba efektivitāte", "Vai Jums patīk strādāt attālināti?", "Jā", "Daļēji", "Nē"},
            {"Attālinātā darba efektivitāte", "Kur strādājat visproduktīvāk?", "Mājās", "Birojā", "Kafejnīcā"},
            {"Mājdzīvnieku aprūpe", "Vai Jums ir mājdzīvnieks?", "Jā", "Nē", "Plānoju iegādāties"},
            {"Mājdzīvnieku aprūpe", "Kādu mājdzīvnieku dodat priekšroku?", "Suni", "Kaķi", "Citu"},
            {"Iepirkšanās internetā", "Cik bieži iepērkaties internetā?", "Katru nedēļu", "Reizi mēnesī", "Reti"},
            {"Iepirkšanās internetā", "Kas ir svarīgākais, iepērkoties internetā?", "Cena", "Kvalitāte", "Piegāde"},
            {"Ceļošanas galamērķi", "Cik bieži Jūs ceļojat?", "Vairākas reizes gadā", "Reizi gadā", "Retāk"},
            {"Ceļošanas galamērķi", "Kāds ir Jūsu iecienītākais ceļošanas veids?", "Ar lidmašīnu", "Ar automašīnu", "Ar autobusu"},
            {"Veselīgs uzturs", "Cik bieži ēdat augļus un dārzeņus?", "Katru dienu", "Dažas reizes nedēļā", "Reti"},
            {"Veselīgs uzturs", "Vai pievēršat uzmanību uztura sastāvam?", "Vienmēr", "Dažreiz", "Nē"},
            {"Automašīnu izvēles kritēriji", "Kas ir svarīgākais, izvēloties automašīnu?", "Cena", "Degvielas patēriņš", "Drošība"},
            {"Automašīnu izvēles kritēriji", "Kādu automašīnu izvēlētos?", "Benzīna", "Dīzeļa", "Elektroauto"},
            {"Viedierīču lietošanas laiks", "Cik stundas dienā lietojat telefonu?", "Līdz 2 stundām", "2-5 stundas", "Vairāk par 5 stundām"},
            {"Viedierīču lietošanas laiks", "Kādam nolūkam visbiežāk lietojat telefonu?", "Saziņai", "Izklaidei", "Darbam"},
            {"Kultūras pasākumu apmeklētība", "Cik bieži apmeklējat kultūras pasākumus?", "Reizi mēnesī", "Dažas reizes gadā", "Neapmeklēju"},
            {"Kultūras pasākumu apmeklētība", "Kādus pasākumus apmeklējat visbiežāk?", "Koncertus", "Teātri", "Izstādes"},
            {"Mūzikas klausīšanās paradumi", "Cik bieži klausāties mūziku?", "Katru dienu", "Dažas reizes nedēļā", "Reti"},
            {"Mūzikas klausīšanās paradumi", "Kādu mūziku klausāties visbiežāk?", "Popmūziku", "Roku", "Klasisko mūziku"},
            {"Brīvā laika pavadīšana", "Kā pavadāt brīvo laiku?", "Ar draugiem", "Mājās", "Sportojot"},
            {"Brīvā laika pavadīšana", "Cik daudz brīvā laika Jums ir dienā?", "Līdz 2 stundām", "2-4 stundas", "Vairāk par 4 stundām"},
            {"Mācību paradumi", "Cik bieži mācāties ārpus nodarbībām?", "Katru dienu", "Dažas reizes nedēļā", "Reti"},
            {"Mācību paradumi", "Kā vislabāk apgūstat jaunu informāciju?", "Lasot", "Klausoties", "Praktiski darbojoties"},
            {"Tehnoloģiju izmantošana", "Cik bieži izmantojat datoru?", "Katru dienu", "Dažas reizes nedēļā", "Reti"},
            {"Tehnoloģiju izmantošana", "Kādam nolūkam visbiežāk izmantojat datoru?", "Darbam", "Mācībām", "Izklaidei"},
            {"Ikdienas pārvietošanās", "Kā visbiežāk pārvietojaties?", "Ar automašīnu", "Ar sabiedrisko transportu", "Kājām"},
            {"Ikdienas pārvietošanās", "Cik ilgi parasti ceļā pavadāt dienā?", "Līdz 30 minūtēm", "30-60 minūtes", "Vairāk par stundu"},
            {"Atpūtas paradumi", "Kā vislabprātāk atpūšaties?", "Skatoties filmas", "Lasot grāmatas", "Dodoties pastaigās"},
            {"Atpūtas paradumi", "Cik bieži dodaties atpūsties ārpus mājas?", "Katru nedēļu", "Reizi mēnesī", "Reti"},
            {"Ēdiena izvēles paradumi", "Cik bieži gatavojat ēdienu mājās?", "Katru dienu", "Dažas reizes nedēļā", "Reti"},
            {"Ēdiena izvēles paradumi", "Kas ir svarīgākais, izvēloties ēdienu?", "Garša", "Cena", "Veselīgums"},
            {"Sociālo tīklu izmantošana", "Cik bieži izmantojat sociālos tīklus?", "Katru dienu", "Dažas reizes nedēļā", "Reti"},
            {"Sociālo tīklu izmantošana", "Kuru sociālo tīklu izmantojat visbiežāk?", "Instagram", "Facebook", "TikTok"}
        };

        for (String[] row : data) {
            addQuestion(conn, row[0], row[1], row[2], row[3], row[4]);
        }
    }

    private static void addQuestion(Connection conn, String surveyTitle,
            String question, String answer1, String answer2, String answer3)
            throws SQLException {

        String check = "SELECT questionid FROM question WHERE question = ?";

        try (PreparedStatement ps = conn.prepareStatement(check)) {
            ps.setString(1, question);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return;
                }
            }
        }

        String sql = "INSERT INTO question "
                + "(question, answer_1, answer_2, answer_3, survey_id) "
                + "VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, question);
            ps.setString(2, answer1);
            ps.setString(3, answer2);
            ps.setString(4, answer3);
            ps.setInt(5, getSurveyId(conn, surveyTitle));
            ps.executeUpdate();
        }
    }

    // Pievieno testa atbildes visiem jautājumiem
    private static void seedAnswers(Connection conn) throws SQLException {

        String[] respondents = {
            "andris", "ieva", "peteris", "anna",
            "maris", "kristine", "edgars", "santa",
            "Maksimka", "resp", "andrejs", "jevgenijs",
            "Kirils", "Danils", "Ivans", "Deniss", "r"
        };

        String sql = "SELECT questionid, answer_1, answer_2, answer_3 "
                + "FROM question ORDER BY questionid";

        try (PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                int questionId = rs.getInt("questionid");

                String[] answers = {
                    rs.getString("answer_1"),
                    rs.getString("answer_2"),
                    rs.getString("answer_3")
                };

                for (int i = 0; i < respondents.length; i++) {

                    String answer = answers[i % 3];

                    addAnswer(
                            conn,
                            answer,
                            getUserId(conn, respondents[i]),
                            questionId
                    );
                }
            }
        }
    }

    // Pievieno atbildi, ja konkrētais respondents uz šo jautājumu neatbildēja
    private static void addAnswer(Connection conn, String answer,
            int userId, int questionId) throws SQLException {

        String check = "SELECT respondent_questionid "
                + "FROM respondent_question "
                + "WHERE user_id = ? AND question_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(check)) {
            ps.setInt(1, userId);
            ps.setInt(2, questionId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return;
                }
            }
        }

        String sql = "INSERT INTO respondent_question "
                + "(answer, user_id, question_id) VALUES (?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, answer);
            ps.setInt(2, userId);
            ps.setInt(3, questionId);
            ps.executeUpdate();
        }
    }
}
