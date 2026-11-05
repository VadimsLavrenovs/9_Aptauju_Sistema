/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import db.DatabaseManager;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import model.Question;
import model.Survey;

/**
 *
 * @author Maksims Plehanovs, Vadims Lavrenovs
 */
public class SurveyDAO {

    public void updateQuestion(Question q) throws SQLException {
        String sql = "UPDATE question SET question = ?, answer_1 = ?, answer_2 = ?, answer_3 = ? WHERE questionid = ?";

        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, q.getQuestion());
            ps.setString(2, q.getAnswer_1());
            ps.setString(3, q.getAnswer_2());
            ps.setString(4, q.getAnswer_3());
            ps.setInt(5, q.getQuestionId());
            ps.executeUpdate();
        }
    }

    public List<Survey> getSurveysByUserId(int userId) throws SQLException {
        List<Survey> surveys = new ArrayList<>();
        String sql = "SELECT surveyid, title, user_id FROM survey WHERE user_id = ? ORDER BY surveyid";

        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Survey survey = new Survey();
                    survey.setSurveyId(rs.getInt("surveyid"));
                    survey.setTitle(rs.getString("title"));
                    survey.setUserId(rs.getInt("user_id"));
                    surveys.add(survey);
                }
            }
        }
        return surveys;
    }

    public List<Question> getQuestionsBySurveyId(int surveyId) throws SQLException {
        List<Question> questions = new ArrayList<>();
        String sql = "SELECT questionid, question, answer_1, answer_2, answer_3, survey_id "
                + "FROM question WHERE survey_id = ? ORDER BY questionid";

        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, surveyId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Question q = new Question();
                    q.setQuestionId(rs.getInt("questionid"));
                    q.setQuestion(rs.getString("question"));
                    q.setAnswer_1(rs.getString("answer_1"));
                    q.setAnswer_2(rs.getString("answer_2"));
                    q.setAnswer_3(rs.getString("answer_3"));
                    q.setSurveyId(rs.getInt("survey_id"));
                    questions.add(q);
                }
            }
        }
        return questions;
    }

    public List<Survey> getSurveysAvailableForRespondent(int userId) throws SQLException {
        List<Survey> surveys = new ArrayList<>();

        String sql = "SELECT s.surveyid, s.title, s.user_id FROM survey s "
                + "WHERE EXISTS (SELECT 1 FROM question q WHERE q.survey_id = s.surveyid) "
                + "ORDER BY s.surveyid";

        try (Connection conn = DatabaseManager.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Survey survey = new Survey();
                    survey.setSurveyId(rs.getInt("surveyid"));
                    survey.setTitle(rs.getString("title"));
                    survey.setUserId(rs.getInt("user_id"));
                    surveys.add(survey);
                }
            }
        }

        return surveys;
    }

    public int createSurveyWithQuestions(Survey survey, List<Question> questions) throws SQLException {
        String surveySql = "INSERT INTO survey (title, user_id) VALUES (?, ?)";
        String questionSql = "INSERT INTO question (question, answer_1, answer_2, answer_3, survey_id) "
                + "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false);
            try {
                int surveyId;
                try (PreparedStatement ps = conn.prepareStatement(surveySql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, survey.getTitle());
                    ps.setInt(2, survey.getUserId());
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        keys.next();
                        surveyId = keys.getInt(1);
                    }
                }
                try (PreparedStatement ps = conn.prepareStatement(questionSql)) {
                    for (Question q : questions) {
                        ps.setString(1, q.getQuestion());
                        ps.setString(2, q.getAnswer_1());
                        ps.setString(3, q.getAnswer_2());
                        ps.setString(4, q.getAnswer_3());
                        ps.setInt(5, surveyId);
                        ps.executeUpdate();
                    }
                }
                conn.commit();
                return surveyId;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }
}
