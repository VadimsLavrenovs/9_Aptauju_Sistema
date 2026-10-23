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

    private Connection connection;

    public boolean createSurvey(Survey survey) {
        return false;
    }

    public boolean deleteSurvey(int surveyId) {
        return false;
    }

    public Survey getSurveyById(int surveyId) {
        return null;
    }

    public boolean updateSurvey(Survey survey) {
        return false;
    }

    public boolean addQuestionToSurvey(Question question) {
        return false;
    }

    public boolean updateQuestion(Question question) {
        return false;
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
}
