/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import db.DatabaseManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;
import model.RespondentQuestion;

/**
 *
 * @author Maksims Plehanovs, Vadims Lavrenovs
 */
public class RespondentQuestionDAO {
        // Iegūst visas atbildes uz konkrētu jautājumu
    public List<RespondentQuestion> getAnswersByQuestionId(int questionId)
            throws SQLException {

        if (questionId <= 0) {
            throw new IllegalArgumentException(
                    "Nederīgs jautājuma ID."
            );
        }

        List<RespondentQuestion> answers = new ArrayList<>();

        String sql = "SELECT respondent_questionid, question_id, "
                + "user_id, answer FROM respondent_question "
                + "WHERE question_id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, questionId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    RespondentQuestion rq = new RespondentQuestion();

                    rq.setRespondentQuestionId(
                            rs.getInt("respondent_questionid")
                    );
                    rq.setQuestionId(rs.getInt("question_id"));
                    rq.setUserId(rs.getInt("user_id"));
                    rq.setAnswer(rs.getString("answer"));

                    answers.add(rq);
                }
            }
        }

        return answers;
    }

    // Pārbauda, vai atbilde ir derīga
    private boolean isValidAnswer(RespondentQuestion rq) {

        if (rq == null) {
            return false;
        }

        if (rq.getUserId() <= 0 || rq.getQuestionId() <= 0) {
            return false;
        }

        String answer = rq.getAnswer();

        if (answer == null || answer.trim().isEmpty()) {
            return false;
        }

        if (answer.trim().length() > 50) {
            return false;
        }

        return true;
    }

    // Saglabā respondenta atbildes datubāzē
    public void saveAnswers(List<RespondentQuestion> answers)
            throws SQLException {

        if (answers == null || answers.isEmpty()) {
            throw new IllegalArgumentException(
                    "Atbilžu saraksts nedrīkst būt tukšs."
            );
        }

        for (RespondentQuestion rq : answers) {
            if (!isValidAnswer(rq)) {
                throw new IllegalArgumentException(
                        "Atrasta nederīga atbilde."
                );
            }
        }

        String sql = "INSERT INTO respondent_question "
                + "(answer, user_id, question_id) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection()) {

            conn.setAutoCommit(false);

            try (PreparedStatement ps = conn.prepareStatement(sql)) {

                for (RespondentQuestion rq : answers) {
                    ps.setString(1, rq.getAnswer().trim());
                    ps.setInt(2, rq.getUserId());
                    ps.setInt(3, rq.getQuestionId());

                    ps.addBatch();
                }

                ps.executeBatch();
                conn.commit();

            } catch (SQLException e) {

                conn.rollback();
                throw e;
            }
        }
    }
}
