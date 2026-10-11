/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

/**
 *
 * @author Maksims Plehanovs, Vadims Lavrenovs
 */
public class RespondentQuestion {

    private int respondentQuestionId;
    private int questionId;
    private int userId;
    private String answer;

    public int getRespondentQuestionId() {
        return respondentQuestionId;
    }

    public void setRespondentQuestionId(int respondentQuestionId) {
        this.respondentQuestionId = respondentQuestionId;
    }

    public int getQuestionId() {
        return questionId;
    }

    public void setQuestionId(int questionId) {
        this.questionId = questionId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }
}
