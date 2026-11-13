/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import model.Question;
import model.Survey;
import org.junit.Test;

/**
 *
 * @author Maksims Pļehanovs, Vadims Lavrenovs
 */
public class SurveyDAOTest {

    private SurveyDAO dao = new SurveyDAO();

    // Izveido aptauju
    private Survey createSurvey(String title, int userId) {
        Survey survey = new Survey();
        survey.setTitle(title);
        survey.setUserId(userId);
        return survey;
    }

    // Izveido jautājumu
    private Question createQuestion(
            String question, String a1, String a2, String a3) {

        Question q = new Question();
        q.setQuestion(question);
        q.setAnswer_1(a1);
        q.setAnswer_2(a2);
        q.setAnswer_3(a3);

        return q;
    }

    // Izveido tekstu ar noteiktu garumu
    private String createLongText(int length) {
        char[] chars = new char[length];
        Arrays.fill(chars, 'A');
        return new String(chars);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRejectsNullSurvey() throws Exception {
        List<Question> questions = new ArrayList<>();
        questions.add(createQuestion(
                "Jautājums?", "A", "B", "C"));

        dao.createSurveyWithQuestions(null, questions);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRejectsNullQuestionList() throws Exception {
        dao.createSurveyWithQuestions(
                createSurvey("Testa aptauja", 1), null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRejectsEmptyQuestionList() throws Exception {
        dao.createSurveyWithQuestions(
                createSurvey("Testa aptauja", 1),
                new ArrayList<Question>());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRejectsEmptySurveyTitle() throws Exception {
        Survey survey = createSurvey("   ", 1);

        List<Question> questions = new ArrayList<>();
        questions.add(createQuestion(
                "Jautājums?", "A", "B", "C"));

        dao.createSurveyWithQuestions(survey, questions);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRejectsSurveyTitleLongerThan50Characters()
            throws Exception {

        Survey survey = createSurvey(createLongText(51), 1);

        List<Question> questions = new ArrayList<>();
        questions.add(createQuestion(
                "Jautājums?", "A", "B", "C"));

        dao.createSurveyWithQuestions(survey, questions);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRejectsInvalidUserId() throws Exception {
        Survey survey = createSurvey("Testa aptauja", 0);

        List<Question> questions = new ArrayList<>();
        questions.add(createQuestion(
                "Jautājums?", "A", "B", "C"));

        dao.createSurveyWithQuestions(survey, questions);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRejectsNullQuestion() throws Exception {
        Survey survey = createSurvey("Testa aptauja", 1);

        List<Question> questions = new ArrayList<>();
        questions.add(null);

        dao.createSurveyWithQuestions(survey, questions);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRejectsEmptyQuestionText() throws Exception {
        Survey survey = createSurvey("Testa aptauja", 1);

        List<Question> questions = new ArrayList<>();
        questions.add(createQuestion("   ", "A", "B", "C"));

        dao.createSurveyWithQuestions(survey, questions);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRejectsQuestionLongerThan100Characters()
            throws Exception {

        Survey survey = createSurvey("Testa aptauja", 1);

        List<Question> questions = new ArrayList<>();
        questions.add(createQuestion(
                createLongText(101), "A", "B", "C"));

        dao.createSurveyWithQuestions(survey, questions);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRejectsEmptyAnswerOption() throws Exception {
        Survey survey = createSurvey("Testa aptauja", 1);

        List<Question> questions = new ArrayList<>();
        questions.add(createQuestion(
                "Jautājums?", "A", "   ", "C"));

        dao.createSurveyWithQuestions(survey, questions);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRejectsAnswerLongerThan50Characters()
            throws Exception {

        Survey survey = createSurvey("Testa aptauja", 1);

        List<Question> questions = new ArrayList<>();
        questions.add(createQuestion(
                "Jautājums?", createLongText(51), "B", "C"));

        dao.createSurveyWithQuestions(survey, questions);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRejectsDuplicateQuestions() throws Exception {
        Survey survey = createSurvey("Testa aptauja", 1);

        List<Question> questions = new ArrayList<>();
        questions.add(createQuestion(
                "Kāds ir tavs vecums?", "A", "B", "C"));
        questions.add(createQuestion(
                "kāds ir tavs vecums?", "D", "E", "F"));

        dao.createSurveyWithQuestions(survey, questions);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpdateQuestionRejectsNullQuestion()
            throws Exception {

        dao.updateQuestion(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUpdateQuestionRejectsInvalidQuestionId()
            throws Exception {

        Question q = createQuestion(
                "Jautājums?", "A", "B", "C");
        q.setQuestionId(0);

        dao.updateQuestion(q);
    }

}
