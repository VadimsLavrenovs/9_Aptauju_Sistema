/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import java.sql.*;
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
}
