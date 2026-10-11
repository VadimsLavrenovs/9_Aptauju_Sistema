/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import java.sql.Connection;
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

    public List<RespondentQuestion> getAnswersByQuestionId(int questionId) {
        return new ArrayList<>();
    }
}
