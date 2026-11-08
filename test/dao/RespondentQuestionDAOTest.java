/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import model.RespondentQuestion;
import org.junit.Test;

/**
 *
 * @author Maksims Pļehanovs, Vadims Lavrenovs
 */
public class RespondentQuestionDAOTest {

    private RespondentQuestionDAO dao = new RespondentQuestionDAO();

    // Izveido testa atbildi ar norādītajiem datiem
    private RespondentQuestion createAnswer(
            int userId, int questionId, String answer) {

        RespondentQuestion rq = new RespondentQuestion();
        rq.setUserId(userId);
        rq.setQuestionId(questionId);
        rq.setAnswer(answer);

        return rq;
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetAnswersRejectsInvalidQuestionId()
            throws Exception {

        dao.getAnswersByQuestionId(0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSaveAnswersRejectsNullList()
            throws Exception {

        dao.saveAnswers(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSaveAnswersRejectsEmptyList()
            throws Exception {

        dao.saveAnswers(new ArrayList<RespondentQuestion>());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSaveAnswersRejectsNullAnswer()
            throws Exception {

        List<RespondentQuestion> answers = new ArrayList<>();
        answers.add(null);

        dao.saveAnswers(answers);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSaveAnswersRejectsInvalidUserId()
            throws Exception {

        List<RespondentQuestion> answers = new ArrayList<>();
        answers.add(createAnswer(0, 1, "Atbilde"));

        dao.saveAnswers(answers);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSaveAnswersRejectsInvalidQuestionId()
            throws Exception {

        List<RespondentQuestion> answers = new ArrayList<>();
        answers.add(createAnswer(1, 0, "Atbilde"));

        dao.saveAnswers(answers);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSaveAnswersRejectsEmptyAnswer()
            throws Exception {

        List<RespondentQuestion> answers = new ArrayList<>();
        answers.add(createAnswer(1, 1, "   "));

        dao.saveAnswers(answers);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSaveAnswersRejectsAnswerLongerThan50Characters()
            throws Exception {

        char[] chars = new char[51];
        Arrays.fill(chars, 'A');

        List<RespondentQuestion> answers = new ArrayList<>();
        answers.add(createAnswer(1, 1, new String(chars)));

        dao.saveAnswers(answers);
    }
}
