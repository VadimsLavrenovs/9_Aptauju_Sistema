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

    private Connection connection;

    public boolean saveAnswer(RespondentQuestion answer) {
        return false;
    }

    public List<RespondentQuestion> getAnswersByQuestionId(int questionId) throws SQLException {
        List<RespondentQuestion> answers = new ArrayList<>();
        String sql = "SELECT respondent_questionid, question_id, user_id, answer "
                + "FROM respondent_question WHERE question_id = ?";

        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, questionId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    RespondentQuestion rq = new RespondentQuestion();
                    rq.setRespondentQuestionId(rs.getInt("respondent_questionid"));
                    rq.setQuestionId(rs.getInt("question_id"));
                    rq.setUserId(rs.getInt("user_id"));
                    rq.setAnswer(rs.getString("answer"));
                    answers.add(rq);
                }
            }
        }
        return answers;
    }
}
