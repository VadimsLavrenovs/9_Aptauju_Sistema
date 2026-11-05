/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package dbsystem;

import dao.RespondentQuestionDAO;
import dao.SurveyDAO;
import db.DatabaseManager;
import java.util.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import model.Question;
import model.RespondentQuestion;
import model.Survey;
import dao.UserDAO;
import model.User;

/**
 *
 * @author Maksims Plehanovs, Vadims Lavrenovs PR-31
 */
public class SurveySystem extends javax.swing.JFrame {

    /**
     * Creates new form Aptauja
     */
    private java.util.List<Survey> currentSurveys = new ArrayList<>();
    private java.util.List<Question> currentQuestions = new ArrayList<>();
    private int currentQuestionIndex = 0;
    private String newSurveyTitle = "";
    private java.util.List<Question> newQuestions = new ArrayList<>();
    private boolean respondentMode = false;
    private java.util.List<Question> fillQuestions = new ArrayList<>();
    private java.util.List<RespondentQuestion> givenAnswers = new ArrayList<>();
    private int fillIndex = 0;
    private ButtonGroup answerGroup = new ButtonGroup();
    private boolean passwordFromRespondent = false;
    private boolean passwordFromAdmin = false;
    private java.util.List<Survey> editSurveys = new ArrayList<>();
    private java.util.List<Question> editQuestions = new ArrayList<>();
    private int editIndex = 0;
    private boolean editComboLoading = false;
    private final java.awt.Color gaisiVioleta = new java.awt.Color(232, 220, 247);

    public SurveySystem() {
        initComponents();
        iestatitLoguKrasa();
        DatabaseManager.init();
        prepareResultsTable();
        prepareAnswerDialog();
        System.setOut(new java.io.PrintStream(System.out, true, java.nio.charset.StandardCharsets.UTF_8));
    }

    private void iestatitLoguKrasa() {
        getContentPane().setBackground(gaisiVioleta);

        javax.swing.JDialog[] logi = {
            LietRegistracija,
            AptaujuIzveide,
            AtbilzuIevade,
            RezultatuApkAtt,
            AptaujasRedigesana,
            ParolesMainisana,
            NavigacijasLogsRespondent,
            NavigacijasLogsIntervetajs,
            LoginLogs,
            AptaujasIzveleLogs,
            AptaujuIzveideNosaukums,
            AdminLietotajiDialog,
            AdminLogsNavigacija
        };

        for (javax.swing.JDialog logs : logi) {
            if (logs != null) {
                logs.getContentPane().setBackground(gaisiVioleta);
            }
        }
    }

    public boolean register(String username, String password, String role) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setRole(role);

        try {
            return new UserDAO().saveUser(user);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public String login(String username, String password) {
        try {
            User user = new UserDAO().getUserByUsername(username);

            if (user != null && user.getPassword().equals(password)) {
                util.Session.startSession(user.getUserId(), user.getRole());
                return user.getRole();
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(
                    this, "Datubāzes kļūda autorizācijas laikā"
            );
            e.printStackTrace();
        }

        return null;
    }

    private void prepareResultsTable() {
        jTable1.setModel(new DefaultTableModel(new Object[][]{}, new String[]{"Jautājums", "Atbilde", "Skaits", "%"}) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        });
        jTable1.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        jTable1.getColumnModel().getColumn(0).setPreferredWidth(170);
        jTable1.getColumnModel().getColumn(1).setPreferredWidth(90);
        jTable1.getColumnModel().getColumn(2).setPreferredWidth(50);
        jTable1.getColumnModel().getColumn(3).setPreferredWidth(50);
    }

    private void showQuestionHeader() {
        RezultatuApkAttJautKartiba.setText(String.valueOf(currentQuestionIndex + 1));
        RezultatuApkAttJautSkaits.setText(String.valueOf(currentQuestions.size()));
        ((DefaultTableModel) jTable1.getModel()).setRowCount(0);
    }

    private void showCurrentQuestionResults() {
        Question q = currentQuestions.get(currentQuestionIndex);

        java.util.List<RespondentQuestion> answers;
        try {
            answers = new RespondentQuestionDAO().getAnswersByQuestionId(q.getQuestionId());
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Datubāzes kļūda, ielādējot rezultātus.");
            e.printStackTrace();
            return;
        }

        String[] options = {q.getAnswer_1(), q.getAnswer_2(), q.getAnswer_3()};
        int[] counts = new int[options.length];
        for (RespondentQuestion rq : answers) {
            for (int i = 0; i < options.length; i++) {
                if (options[i].equals(rq.getAnswer())) {
                    counts[i]++;
                }
            }
        }

        int total = answers.size();
        DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
        model.setRowCount(0);
        for (int i = 0; i < options.length; i++) {
            long percent = total == 0 ? 0 : Math.round(counts[i] * 100.0 / total);
            String jautajums = (i == 0) ? q.getQuestion() : "";
            model.addRow(new Object[]{jautajums, options[i], counts[i], percent + "%"});
        }
    }

    private boolean newQuestionFieldsEmpty() {
        return AptaujuIzveideJautajumsLogs.getText().trim().isEmpty()
                && jTextField9.getText().trim().isEmpty()
                && jTextField7.getText().trim().isEmpty()
                && jTextField8.getText().trim().isEmpty();
    }

    private void clearNewQuestionFields() {
        AptaujuIzveideJautajumsLogs.setText("");
        jTextField9.setText("");
        jTextField7.setText("");
        jTextField8.setText("");
    }

    private void showNewQuestionNumber() {
        jLabel9.setText(String.valueOf(newQuestions.size() + 1));
    }

    private boolean addCurrentQuestion() {
        String text = AptaujuIzveideJautajumsLogs.getText().trim();
        String a1 = jTextField9.getText().trim();
        String a2 = jTextField7.getText().trim();
        String a3 = jTextField8.getText().trim();

        if (text.isEmpty() || a1.isEmpty() || a2.isEmpty() || a3.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Aizpildiet jautājumu un visas trīs atbildes!");
            return false;
        }
        if (text.length() > 100) {
            JOptionPane.showMessageDialog(this, "Jautājums ir pārāk garš (maksimums 100 simboli).");
            return false;
        }
        if (a1.length() > 50 || a2.length() > 50 || a3.length() > 50) {
            JOptionPane.showMessageDialog(this, "Atbilde ir pārāk gara (maksimums 50 simboli).");
            return false;
        }
        for (Question existing : newQuestions) {
            if (existing.getQuestion().equalsIgnoreCase(text)) {
                JOptionPane.showMessageDialog(this, "Šāds jautājums jau ir pievienots!");
                return false;
            }
        }

        Question q = new Question();
        q.setQuestion(text);
        q.setAnswer_1(a1);
        q.setAnswer_2(a2);
        q.setAnswer_3(a3);
        newQuestions.add(q);
        return true;
    }

    private void prepareAnswerDialog() {
        answerGroup.add(AtbilzuIevade1AtbildeBox);
        answerGroup.add(AtbilzuIevade2AtbildeBox);
        answerGroup.add(AtbilzuIevade3AtbildeBox);
        AtbilzuIevade.setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
    }

    private void openSurveyChooser(java.util.List<Survey> surveys, boolean forFilling) {
        currentSurveys = surveys;
        respondentMode = forFilling;

        if (forFilling) {
            jLabel1.setText("Kuru aptauju Jūs gribat aizpildīt?");
            RezultatuIzveleLogsApskatitPoga.setText("Pildīt");
        } else {
            jLabel1.setText("Kādas aptaujas rezultātu Jūs gribat redzēt?");
            RezultatuIzveleLogsApskatitPoga.setText("Apskatīt");
        }

        RezultatuIzveleLogsAptaujasRezultati.removeAllItems();
        for (Survey survey : currentSurveys) {
            RezultatuIzveleLogsAptaujasRezultati.addItem(survey.getTitle());
        }

        NavigacijasLogsIntervetajs.setVisible(false);
        NavigacijasLogsRespondent.setVisible(false);
        AptaujasIzveleLogs.setVisible(true);
        String role = util.Session.getRole();

        if (role != null) {
            if ("respondent".equalsIgnoreCase(role)) {
                AptaujasIzveleLogs.setSize(360, 320);
                AptaujasIzveleLogs.setLocationRelativeTo(null);
            } else {
                AptaujasIzveleLogs.setSize(470, 320);
                AptaujasIzveleLogs.setLocationRelativeTo(null);
            }
        }
    }

    private void backFromSurveyChooser() {
        AptaujasIzveleLogs.setVisible(false);
        String role = util.Session.getRole();

        if (role != null) {
            if ("respondent".equalsIgnoreCase(role)) {
                NavigacijasLogsRespondent.setVisible(true);
                NavigacijasLogsRespondent.setSize(458, 330);
                NavigacijasLogsRespondent.setLocationRelativeTo(null);
            } else {
                NavigacijasLogsIntervetajs.setVisible(true);
                NavigacijasLogsIntervetajs.setSize(458, 390);
                NavigacijasLogsIntervetajs.setLocationRelativeTo(null);
            }
        }
    }

    private void showFillQuestion() {
        Question q = fillQuestions.get(fillIndex);
        jLabel13.setText("Jautājums " + (fillIndex + 1) + " no " + fillQuestions.size() + ":");
        AtbilzuIevadeJautajums.setText(q.getQuestion());
        AtbilzuIevade1Atbilde.setText(q.getAnswer_1());
        AtbilzuIevade2Atbilde.setText(q.getAnswer_2());
        AtbilzuIevade3Atbilde.setText(q.getAnswer_3());
        answerGroup.clearSelection();
        boolean last = fillIndex == fillQuestions.size() - 1;
        AtbilzuIevadeTalakPoga.setText(last ? "Pabeigt" : "Talāk");
    }

    private void finishFilling() {
        AtbilzuIevade.setVisible(false);
        NavigacijasLogsRespondent.setVisible(true);
        NavigacijasLogsRespondent.setSize(458, 330);
        NavigacijasLogsRespondent.setLocationRelativeTo(null);
    }

    private void backFromPasswordChange() {
        ParolesMainisana.setVisible(false);
        JDialog target;
        if (passwordFromAdmin) {
            target = AdminLogsNavigacija;
            target.setSize(408, 330);
        } else if (passwordFromRespondent) {
            target = NavigacijasLogsRespondent;
            target.setSize(458, 330);
        } else {
            target = NavigacijasLogsIntervetajs;
            target.setSize(458, 390);
        }
        target.setLocationRelativeTo(null);
        target.setVisible(true);
        passwordFromAdmin = false;
        passwordFromRespondent = false;
    }

    private void loadEditSurvey(int surveyIndex) {
        try {
            editQuestions = new SurveyDAO().getQuestionsBySurveyId(editSurveys.get(surveyIndex).getSurveyId());
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Datubāzes kļūda, ielādējot jautājumus.");
            e.printStackTrace();
            editQuestions = new ArrayList<>();
        }
        editIndex = 0;
        showEditQuestion();
        if (editQuestions.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Šai aptaujai vēl nav jautājumu.");
        }
    }

    private void showEditQuestion() {
        if (editQuestions.isEmpty()) {
            AptaujasRedigesanaJautKartiba.setText("0");
            AptaujasRedigesanaJautSkaits.setText("0");
            AptaujasRedigesanaJautLogs.setText("");
            AptaujasRedigesana1AtbildeLogs.setText("");
            AptaujasRedigesana2AtbildeLogs.setText("");
            AptaujasRedigesana3AtbildeLogs.setText("");
            return;
        }
        Question q = editQuestions.get(editIndex);
        AptaujasRedigesanaJautKartiba.setText(String.valueOf(editIndex + 1));
        AptaujasRedigesanaJautSkaits.setText(String.valueOf(editQuestions.size()));
        AptaujasRedigesanaJautLogs.setText(q.getQuestion());
        AptaujasRedigesana1AtbildeLogs.setText(q.getAnswer_1());
        AptaujasRedigesana2AtbildeLogs.setText(q.getAnswer_2());
        AptaujasRedigesana3AtbildeLogs.setText(q.getAnswer_3());
    }

    public boolean changePassword(
            int userId, String jaunaParole, String atkaroti) {

        if (jaunaParole.isEmpty() || atkaroti.isEmpty()) {
            return false;
        }

        if (!jaunaParole.equals(atkaroti)) {
            return false;
        }

        try {
            return new UserDAO().updatePassword(userId, jaunaParole);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        LietRegistracija = new javax.swing.JDialog();
        LietRegistracijaHelpPoga = new javax.swing.JButton();
        LietRegistracijaRegistretiesPoga = new javax.swing.JButton();
        LietRegistracijaAtpakalPoga = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jTextField3 = new javax.swing.JTextField();
        jTextField4 = new javax.swing.JTextField();
        jTextField5 = new javax.swing.JTextField();
        LietRegistracijaIntervetajs = new javax.swing.JCheckBox();
        AptaujuIzveide = new javax.swing.JDialog();
        panel1 = new java.awt.Panel();
        jLabel7 = new javax.swing.JLabel();
        AptaujuIzveideDzestPoga = new javax.swing.JButton();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        AptaujuIzveideJautajumsLogs = new javax.swing.JTextField();
        jTextField7 = new javax.swing.JTextField();
        jTextField8 = new javax.swing.JTextField();
        jTextField9 = new javax.swing.JTextField();
        AptaujuIzveideIzveidotAptauju = new javax.swing.JButton();
        AptaujuIzveidePievienotJaut = new javax.swing.JButton();
        AtbilzuIevade = new javax.swing.JDialog();
        jLabel13 = new javax.swing.JLabel();
        AtbilzuIevadeJautajums = new javax.swing.JLabel();
        AtbilzuIevade1Atbilde = new javax.swing.JLabel();
        AtbilzuIevade2Atbilde = new javax.swing.JLabel();
        AtbilzuIevade3Atbilde = new javax.swing.JLabel();
        AtbilzuIevadeTalakPoga = new javax.swing.JButton();
        AtbilzuIevade2AtbildeBox = new javax.swing.JCheckBox();
        AtbilzuIevade1AtbildeBox = new javax.swing.JCheckBox();
        AtbilzuIevade3AtbildeBox = new javax.swing.JCheckBox();
        RezultatuApkAtt = new javax.swing.JDialog();
        RezultatuApkAttParaditRezultatu = new javax.swing.JButton();
        RezultatuApkAttRezultati = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        RezultatuApkAttNakamaisJautPoga = new javax.swing.JButton();
        RezultatuApkAttIeprieksejaisJautPoga = new javax.swing.JButton();
        jLabel18 = new javax.swing.JLabel();
        RezultatuApkAttJautKartiba = new javax.swing.JLabel();
        jLabel20 = new javax.swing.JLabel();
        RezultatuApkAttJautSkaits = new javax.swing.JLabel();
        AptaujasRedigesana = new javax.swing.JDialog();
        AptaujasRedigesanaSaglabatPoga = new javax.swing.JButton();
        jLabel22 = new javax.swing.JLabel();
        jLabel23 = new javax.swing.JLabel();
        jLabel24 = new javax.swing.JLabel();
        jLabel25 = new javax.swing.JLabel();
        jLabel26 = new javax.swing.JLabel();
        AptaujasRedigesanaAptaujuIzvele = new javax.swing.JComboBox<>();
        AptaujasRedigesanaPagajusaisJaut = new javax.swing.JButton();
        AptaujasRedigesanaNakamaisJaut = new javax.swing.JButton();
        AptaujasRedigesanaJautKartiba = new javax.swing.JLabel();
        jLabel28 = new javax.swing.JLabel();
        AptaujasRedigesanaJautSkaits = new javax.swing.JLabel();
        AptaujasRedigesana1AtbildeLogs = new javax.swing.JTextField();
        AptaujasRedigesana2AtbildeLogs = new javax.swing.JTextField();
        AptaujasRedigesana3AtbildeLogs = new javax.swing.JTextField();
        AptaujasRedigesanaJautLogs = new javax.swing.JTextField();
        AptaujasRedigesanaIzietPoga = new javax.swing.JButton();
        ParolesMainisana = new javax.swing.JDialog();
        ParolesMainisanaMainitParoliPoga = new javax.swing.JButton();
        jLabel30 = new javax.swing.JLabel();
        jLabel31 = new javax.swing.JLabel();
        jLabel32 = new javax.swing.JLabel();
        ParolesMainisanaPasreizejaParole = new javax.swing.JTextField();
        ParolesMainisanaAtkartotiParole = new javax.swing.JTextField();
        ParolesMainisanaJaunaParole = new javax.swing.JTextField();
        NavigacijasLogsRespondent = new javax.swing.JDialog();
        jLabel33 = new javax.swing.JLabel();
        RespondentsLogsPilditAptaujasPoga = new javax.swing.JButton();
        RespondentsLogsMainitParoliPoga = new javax.swing.JButton();
        NavigacijasLogsIntervetajs = new javax.swing.JDialog();
        IntervetajsLogsMainitParoliPoga = new javax.swing.JButton();
        jLabel35 = new javax.swing.JLabel();
        IntervetajsLogsApskatitiesRezultatusPoga = new javax.swing.JButton();
        IntervetajsLogsVeidotAptaujuPoga = new javax.swing.JButton();
        IntervetajsLogsRedigetAptaujuPoga = new javax.swing.JButton();
        LoginLogs = new javax.swing.JDialog();
        jLabel34 = new javax.swing.JLabel();
        jLabel36 = new javax.swing.JLabel();
        LoginLogsIeietPoga = new javax.swing.JButton();
        LoginLogsLietotajvards = new javax.swing.JTextField();
        LoginLogsParole = new javax.swing.JTextField();
        AptaujasIzveleLogs = new javax.swing.JDialog();
        RezultatuIzveleLogsAptaujasRezultati = new javax.swing.JComboBox<>();
        jLabel1 = new javax.swing.JLabel();
        RezultatuIzveleLogsApskatitPoga = new javax.swing.JButton();
        RezultatuIzveleLogsAtpakalPoga = new javax.swing.JButton();
        AptaujuIzveideNosaukums = new javax.swing.JDialog();
        AptaujuIzveideNosaukumsAtpakalPoga = new javax.swing.JButton();
        AptaujuIzveideNosaukumsTalaklPoga = new javax.swing.JButton();
        AptaujuIzveideNosaukumsAptaujasNosaukums = new javax.swing.JTextField();
        AptaujasNos = new javax.swing.JLabel();
        AdminLietotajiDialog = new javax.swing.JDialog();
        jLabel2 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        lietotajiTable = new javax.swing.JTable();
        AdminLogsNavigacija = new javax.swing.JDialog();
        jLabel37 = new javax.swing.JLabel();
        AdminSkatitDatusPoga = new javax.swing.JButton();
        AdminLogsMainitParoli = new javax.swing.JButton();
        GalvenaisLogsIeietPoga = new javax.swing.JButton();
        GalvenaisLogsRegistretiesPoga = new javax.swing.JButton();

        LietRegistracija.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                LietRegistracijaWindowClosing(evt);
            }
        });

        LietRegistracijaHelpPoga.setBackground(new java.awt.Color(204, 204, 204));
        LietRegistracijaHelpPoga.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        LietRegistracijaHelpPoga.setText("?");
        LietRegistracijaHelpPoga.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                LietRegistracijaHelpPogaMouseClicked(evt);
            }
        });

        LietRegistracijaRegistretiesPoga.setBackground(new java.awt.Color(204, 102, 255));
        LietRegistracijaRegistretiesPoga.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        LietRegistracijaRegistretiesPoga.setForeground(new java.awt.Color(255, 255, 255));
        LietRegistracijaRegistretiesPoga.setText("Reģistrēties");
        LietRegistracijaRegistretiesPoga.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                LietRegistracijaRegistretiesPogaMouseClicked(evt);
            }
        });

        LietRegistracijaAtpakalPoga.setBackground(new java.awt.Color(204, 102, 255));
        LietRegistracijaAtpakalPoga.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        LietRegistracijaAtpakalPoga.setForeground(new java.awt.Color(255, 255, 255));
        LietRegistracijaAtpakalPoga.setText("Atpakaļ");
        LietRegistracijaAtpakalPoga.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                LietRegistracijaAtpakalPogaMouseClicked(evt);
            }
        });

        jLabel3.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel3.setText("Lietotājvārds:");

        jLabel4.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel4.setText("Parole:");

        jLabel5.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel5.setText("Parole(atkārtoti):");

        jLabel6.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel6.setText("Intervētājs?:");

        jTextField3.setBackground(new java.awt.Color(204, 204, 204));

        jTextField4.setBackground(new java.awt.Color(204, 204, 204));

        jTextField5.setBackground(new java.awt.Color(204, 204, 204));

        LietRegistracijaIntervetajs.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        LietRegistracijaIntervetajs.setAlignmentX(1.0F);
        LietRegistracijaIntervetajs.setMargin(new java.awt.Insets(5, 5, 5, 5));

        javax.swing.GroupLayout LietRegistracijaLayout = new javax.swing.GroupLayout(LietRegistracija.getContentPane());
        LietRegistracija.getContentPane().setLayout(LietRegistracijaLayout);
        LietRegistracijaLayout.setHorizontalGroup(
            LietRegistracijaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(LietRegistracijaLayout.createSequentialGroup()
                .addGap(91, 91, 91)
                .addGroup(LietRegistracijaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(LietRegistracijaLayout.createSequentialGroup()
                        .addComponent(LietRegistracijaRegistretiesPoga)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(LietRegistracijaAtpakalPoga, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(41, 41, 41))
                    .addGroup(LietRegistracijaLayout.createSequentialGroup()
                        .addGroup(LietRegistracijaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel6)
                            .addComponent(jLabel5)
                            .addComponent(jLabel4)
                            .addComponent(jLabel3))
                        .addGroup(LietRegistracijaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(LietRegistracijaLayout.createSequentialGroup()
                                .addGap(97, 97, 97)
                                .addComponent(jTextField3, javax.swing.GroupLayout.DEFAULT_SIZE, 180, Short.MAX_VALUE))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, LietRegistracijaLayout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGroup(LietRegistracijaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jTextField5, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jTextField4, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(LietRegistracijaIntervetajs, javax.swing.GroupLayout.Alignment.TRAILING))))))
                .addGap(9, 9, 9)
                .addComponent(LietRegistracijaHelpPoga, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        LietRegistracijaLayout.setVerticalGroup(
            LietRegistracijaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(LietRegistracijaLayout.createSequentialGroup()
                .addGap(39, 39, 39)
                .addGroup(LietRegistracijaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(jTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(24, 24, 24)
                .addGroup(LietRegistracijaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(jTextField4, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(23, 23, 23)
                .addGroup(LietRegistracijaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(jTextField5, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, Short.MAX_VALUE)
                .addGroup(LietRegistracijaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel6)
                    .addComponent(LietRegistracijaIntervetajs))
                .addGap(47, 47, 47)
                .addGroup(LietRegistracijaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(LietRegistracijaRegistretiesPoga, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(LietRegistracijaAtpakalPoga, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(33, 33, 33))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, LietRegistracijaLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(LietRegistracijaHelpPoga, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        AptaujuIzveide.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                AptaujuIzveideWindowClosing(evt);
            }
        });

        jLabel7.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel7.setText("Jautājums Nr.");

        AptaujuIzveideDzestPoga.setBackground(new java.awt.Color(255, 0, 0));
        AptaujuIzveideDzestPoga.setForeground(new java.awt.Color(255, 255, 255));
        AptaujuIzveideDzestPoga.setText("Dzēst");
        AptaujuIzveideDzestPoga.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                AptaujuIzveideDzestPogaMouseClicked(evt);
            }
        });

        jLabel8.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel8.setText("Jautājums:");

        jLabel9.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel9.setText("1");

        jLabel10.setText("Atbilde 1:");

        jLabel11.setText("Atbilde 2:");

        jLabel12.setText("Atbilde 3:");

        AptaujuIzveideJautajumsLogs.setBackground(new java.awt.Color(204, 204, 204));

        jTextField7.setBackground(new java.awt.Color(204, 204, 204));

        jTextField8.setBackground(new java.awt.Color(204, 204, 204));

        jTextField9.setBackground(new java.awt.Color(204, 204, 204));

        javax.swing.GroupLayout panel1Layout = new javax.swing.GroupLayout(panel1);
        panel1.setLayout(panel1Layout);
        panel1Layout.setHorizontalGroup(
            panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel7)
                    .addComponent(jLabel8)
                    .addComponent(jLabel10)
                    .addComponent(jLabel11)
                    .addComponent(jLabel12))
                .addGroup(panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panel1Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(panel1Layout.createSequentialGroup()
                                .addComponent(jLabel9)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(AptaujuIzveideDzestPoga))
                            .addGroup(panel1Layout.createSequentialGroup()
                                .addGroup(panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jTextField7, javax.swing.GroupLayout.PREFERRED_SIZE, 242, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jTextField8, javax.swing.GroupLayout.PREFERRED_SIZE, 242, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jTextField9, javax.swing.GroupLayout.PREFERRED_SIZE, 242, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(0, 14, Short.MAX_VALUE)))
                        .addContainerGap())
                    .addGroup(panel1Layout.createSequentialGroup()
                        .addGap(48, 48, 48)
                        .addComponent(AptaujuIzveideJautajumsLogs, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
        );
        panel1Layout.setVerticalGroup(
            panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(AptaujuIzveideDzestPoga)
                    .addComponent(jLabel9))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8)
                    .addComponent(AptaujuIzveideJautajumsLogs, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel10)
                    .addComponent(jTextField9, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel11)
                    .addComponent(jTextField7, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(23, 23, 23)
                .addGroup(panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel12)
                    .addComponent(jTextField8, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(17, Short.MAX_VALUE))
        );

        AptaujuIzveideIzveidotAptauju.setBackground(new java.awt.Color(204, 102, 255));
        AptaujuIzveideIzveidotAptauju.setForeground(new java.awt.Color(255, 255, 255));
        AptaujuIzveideIzveidotAptauju.setText("Izveidot aptauju");
        AptaujuIzveideIzveidotAptauju.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                AptaujuIzveideIzveidotAptaujuMouseClicked(evt);
            }
        });

        AptaujuIzveidePievienotJaut.setBackground(new java.awt.Color(204, 204, 204));
        AptaujuIzveidePievienotJaut.setText("+ Pievienot jautājumu");
        AptaujuIzveidePievienotJaut.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                AptaujuIzveidePievienotJautMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout AptaujuIzveideLayout = new javax.swing.GroupLayout(AptaujuIzveide.getContentPane());
        AptaujuIzveide.getContentPane().setLayout(AptaujuIzveideLayout);
        AptaujuIzveideLayout.setHorizontalGroup(
            AptaujuIzveideLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(AptaujuIzveideLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(AptaujuIzveideLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(AptaujuIzveideLayout.createSequentialGroup()
                        .addComponent(panel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addContainerGap())
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, AptaujuIzveideLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(AptaujuIzveidePievienotJaut, javax.swing.GroupLayout.PREFERRED_SIZE, 164, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(113, 113, 113))))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, AptaujuIzveideLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(AptaujuIzveideIzveidotAptauju, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(133, 133, 133))
        );
        AptaujuIzveideLayout.setVerticalGroup(
            AptaujuIzveideLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(AptaujuIzveideLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(panel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(AptaujuIzveidePievienotJaut, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(AptaujuIzveideIzveidotAptauju, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12))
        );

        AtbilzuIevade.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                AtbilzuIevadeWindowClosing(evt);
            }
        });

        jLabel13.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel13.setText("Jautājums");

        AtbilzuIevadeJautajums.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        AtbilzuIevadeJautajums.setText("Jautājums :)");

        AtbilzuIevade1Atbilde.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        AtbilzuIevade1Atbilde.setText("Atbilde 1");

        AtbilzuIevade2Atbilde.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        AtbilzuIevade2Atbilde.setText("Atbilde 2");

        AtbilzuIevade3Atbilde.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        AtbilzuIevade3Atbilde.setText("Atbilde 3");

        AtbilzuIevadeTalakPoga.setBackground(new java.awt.Color(204, 102, 255));
        AtbilzuIevadeTalakPoga.setForeground(new java.awt.Color(255, 255, 255));
        AtbilzuIevadeTalakPoga.setText("Talāk");
        AtbilzuIevadeTalakPoga.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                AtbilzuIevadeTalakPogaMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout AtbilzuIevadeLayout = new javax.swing.GroupLayout(AtbilzuIevade.getContentPane());
        AtbilzuIevade.getContentPane().setLayout(AtbilzuIevadeLayout);
        AtbilzuIevadeLayout.setHorizontalGroup(
            AtbilzuIevadeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(AtbilzuIevadeLayout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addGroup(AtbilzuIevadeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(AtbilzuIevadeLayout.createSequentialGroup()
                        .addComponent(AtbilzuIevadeJautajums)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(AtbilzuIevadeLayout.createSequentialGroup()
                        .addGroup(AtbilzuIevadeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(AtbilzuIevade2AtbildeBox)
                            .addComponent(AtbilzuIevade1AtbildeBox)
                            .addComponent(AtbilzuIevade3AtbildeBox))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(AtbilzuIevadeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(AtbilzuIevade2Atbilde)
                            .addComponent(AtbilzuIevade1Atbilde)
                            .addComponent(AtbilzuIevade3Atbilde))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
            .addGroup(AtbilzuIevadeLayout.createSequentialGroup()
                .addGroup(AtbilzuIevadeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(AtbilzuIevadeLayout.createSequentialGroup()
                        .addGap(153, 153, 153)
                        .addComponent(AtbilzuIevadeTalakPoga, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(AtbilzuIevadeLayout.createSequentialGroup()
                        .addGap(128, 128, 128)
                        .addComponent(jLabel13)))
                .addGap(0, 157, Short.MAX_VALUE))
        );
        AtbilzuIevadeLayout.setVerticalGroup(
            AtbilzuIevadeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(AtbilzuIevadeLayout.createSequentialGroup()
                .addContainerGap(10, Short.MAX_VALUE)
                .addComponent(jLabel13)
                .addGap(18, 18, 18)
                .addComponent(AtbilzuIevadeJautajums)
                .addGap(28, 28, 28)
                .addGroup(AtbilzuIevadeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(AtbilzuIevade1Atbilde)
                    .addComponent(AtbilzuIevade1AtbildeBox))
                .addGap(18, 18, 18)
                .addGroup(AtbilzuIevadeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(AtbilzuIevadeLayout.createSequentialGroup()
                        .addComponent(AtbilzuIevade2Atbilde)
                        .addGap(18, 18, 18)
                        .addGroup(AtbilzuIevadeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(AtbilzuIevade3Atbilde)
                            .addComponent(AtbilzuIevade3AtbildeBox)))
                    .addComponent(AtbilzuIevade2AtbildeBox))
                .addGap(22, 22, 22)
                .addComponent(AtbilzuIevadeTalakPoga, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(62, 62, 62))
        );

        RezultatuApkAtt.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                RezultatuApkAttWindowClosing(evt);
            }
        });

        RezultatuApkAttParaditRezultatu.setBackground(new java.awt.Color(204, 102, 255));
        RezultatuApkAttParaditRezultatu.setForeground(new java.awt.Color(255, 255, 255));
        RezultatuApkAttParaditRezultatu.setText("Parādīt rezultātu");
        RezultatuApkAttParaditRezultatu.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                RezultatuApkAttParaditRezultatuMouseClicked(evt);
            }
        });

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        RezultatuApkAttRezultati.setViewportView(jTable1);

        RezultatuApkAttNakamaisJautPoga.setBackground(new java.awt.Color(204, 102, 255));
        RezultatuApkAttNakamaisJautPoga.setForeground(new java.awt.Color(255, 255, 255));
        RezultatuApkAttNakamaisJautPoga.setText(">");
        RezultatuApkAttNakamaisJautPoga.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                RezultatuApkAttNakamaisJautPogaMouseClicked(evt);
            }
        });

        RezultatuApkAttIeprieksejaisJautPoga.setBackground(new java.awt.Color(204, 102, 255));
        RezultatuApkAttIeprieksejaisJautPoga.setForeground(new java.awt.Color(255, 255, 255));
        RezultatuApkAttIeprieksejaisJautPoga.setText("<");
        RezultatuApkAttIeprieksejaisJautPoga.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                RezultatuApkAttIeprieksejaisJautPogaMouseClicked(evt);
            }
        });

        jLabel18.setText("Jautājums");

        RezultatuApkAttJautKartiba.setText("...");

        jLabel20.setText("no");

        RezultatuApkAttJautSkaits.setText("...");

        javax.swing.GroupLayout RezultatuApkAttLayout = new javax.swing.GroupLayout(RezultatuApkAtt.getContentPane());
        RezultatuApkAtt.getContentPane().setLayout(RezultatuApkAttLayout);
        RezultatuApkAttLayout.setHorizontalGroup(
            RezultatuApkAttLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
            .addGroup(RezultatuApkAttLayout.createSequentialGroup()
                .addGroup(RezultatuApkAttLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, RezultatuApkAttLayout.createSequentialGroup()
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(RezultatuApkAttRezultati, javax.swing.GroupLayout.PREFERRED_SIZE, 375, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(RezultatuApkAttLayout.createSequentialGroup()
                        .addGap(141, 141, 141)
                        .addComponent(RezultatuApkAttParaditRezultatu)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
            .addGroup(RezultatuApkAttLayout.createSequentialGroup()
                .addGap(30, 30, 30)
                .addComponent(RezultatuApkAttIeprieksejaisJautPoga, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(63, 63, 63)
                .addComponent(jLabel18)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(RezultatuApkAttJautKartiba, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel20)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(RezultatuApkAttJautSkaits, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(RezultatuApkAttNakamaisJautPoga, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(26, 26, 26))
        );
        RezultatuApkAttLayout.setVerticalGroup(
            RezultatuApkAttLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, RezultatuApkAttLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(RezultatuApkAttLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, RezultatuApkAttLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel18)
                        .addComponent(RezultatuApkAttJautKartiba)
                        .addComponent(jLabel20)
                        .addComponent(RezultatuApkAttJautSkaits))
                    .addComponent(RezultatuApkAttIeprieksejaisJautPoga, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 40, Short.MAX_VALUE)
                    .addComponent(RezultatuApkAttNakamaisJautPoga, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addComponent(RezultatuApkAttRezultati, javax.swing.GroupLayout.DEFAULT_SIZE, 207, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(RezultatuApkAttParaditRezultatu, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(11, 11, 11))
        );

        AptaujasRedigesana.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                AptaujasRedigesanaWindowClosing(evt);
            }
        });

        AptaujasRedigesanaSaglabatPoga.setBackground(new java.awt.Color(204, 102, 255));
        AptaujasRedigesanaSaglabatPoga.setForeground(new java.awt.Color(255, 255, 255));
        AptaujasRedigesanaSaglabatPoga.setText("Saglabāt");
        AptaujasRedigesanaSaglabatPoga.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                AptaujasRedigesanaSaglabatPogaMouseClicked(evt);
            }
        });

        jLabel22.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel22.setText("Atbilde 1:");

        jLabel23.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel23.setText("Atbilde 2:");

        jLabel24.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel24.setText("Atbilde 3:");

        jLabel25.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel25.setText("Jautājums:");

        jLabel26.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel26.setText("Jautājums");

        AptaujasRedigesanaAptaujuIzvele.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        AptaujasRedigesanaAptaujuIzvele.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                AptaujasRedigesanaAptaujuIzveleActionPerformed(evt);
            }
        });

        AptaujasRedigesanaPagajusaisJaut.setBackground(new java.awt.Color(204, 102, 255));
        AptaujasRedigesanaPagajusaisJaut.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        AptaujasRedigesanaPagajusaisJaut.setForeground(new java.awt.Color(255, 255, 255));
        AptaujasRedigesanaPagajusaisJaut.setText("<");
        AptaujasRedigesanaPagajusaisJaut.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                AptaujasRedigesanaPagajusaisJautMouseClicked(evt);
            }
        });

        AptaujasRedigesanaNakamaisJaut.setBackground(new java.awt.Color(204, 102, 255));
        AptaujasRedigesanaNakamaisJaut.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        AptaujasRedigesanaNakamaisJaut.setForeground(new java.awt.Color(255, 255, 255));
        AptaujasRedigesanaNakamaisJaut.setText(">");
        AptaujasRedigesanaNakamaisJaut.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                AptaujasRedigesanaNakamaisJautMouseClicked(evt);
            }
        });

        AptaujasRedigesanaJautKartiba.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        AptaujasRedigesanaJautKartiba.setText("...");

        jLabel28.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel28.setText("no");

        AptaujasRedigesanaJautSkaits.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        AptaujasRedigesanaJautSkaits.setText("...");

        AptaujasRedigesana1AtbildeLogs.setBackground(new java.awt.Color(204, 204, 204));

        AptaujasRedigesana2AtbildeLogs.setBackground(new java.awt.Color(204, 204, 204));

        AptaujasRedigesana3AtbildeLogs.setBackground(new java.awt.Color(204, 204, 204));

        AptaujasRedigesanaJautLogs.setBackground(new java.awt.Color(204, 204, 204));

        AptaujasRedigesanaIzietPoga.setBackground(new java.awt.Color(204, 102, 255));
        AptaujasRedigesanaIzietPoga.setForeground(new java.awt.Color(255, 255, 255));
        AptaujasRedigesanaIzietPoga.setText("Iziet");
        AptaujasRedigesanaIzietPoga.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                AptaujasRedigesanaIzietPogaMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout AptaujasRedigesanaLayout = new javax.swing.GroupLayout(AptaujasRedigesana.getContentPane());
        AptaujasRedigesana.getContentPane().setLayout(AptaujasRedigesanaLayout);
        AptaujasRedigesanaLayout.setHorizontalGroup(
            AptaujasRedigesanaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(AptaujasRedigesanaLayout.createSequentialGroup()
                .addGap(0, 23, Short.MAX_VALUE)
                .addGroup(AptaujasRedigesanaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(AptaujasRedigesanaLayout.createSequentialGroup()
                        .addGroup(AptaujasRedigesanaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel25)
                            .addGroup(AptaujasRedigesanaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jLabel23)
                                .addComponent(jLabel22)
                                .addComponent(jLabel24)))
                        .addGroup(AptaujasRedigesanaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(AptaujasRedigesanaLayout.createSequentialGroup()
                                .addGap(96, 96, 96)
                                .addComponent(AptaujasRedigesanaJautLogs, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(AptaujasRedigesanaLayout.createSequentialGroup()
                                .addGap(36, 36, 36)
                                .addGroup(AptaujasRedigesanaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(AptaujasRedigesana2AtbildeLogs, javax.swing.GroupLayout.PREFERRED_SIZE, 260, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(AptaujasRedigesana1AtbildeLogs, javax.swing.GroupLayout.PREFERRED_SIZE, 260, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(AptaujasRedigesana3AtbildeLogs, javax.swing.GroupLayout.PREFERRED_SIZE, 260, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                    .addComponent(AptaujasRedigesanaAptaujuIzvele, javax.swing.GroupLayout.PREFERRED_SIZE, 390, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(24, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, AptaujasRedigesanaLayout.createSequentialGroup()
                .addGap(39, 39, 39)
                .addComponent(AptaujasRedigesanaPagajusaisJaut)
                .addGap(84, 84, 84)
                .addComponent(jLabel26)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(AptaujasRedigesanaJautKartiba)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel28)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(AptaujasRedigesanaJautSkaits)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(AptaujasRedigesanaNakamaisJaut)
                .addGap(54, 54, 54))
            .addGroup(AptaujasRedigesanaLayout.createSequentialGroup()
                .addGap(95, 95, 95)
                .addComponent(AptaujasRedigesanaSaglabatPoga, javax.swing.GroupLayout.PREFERRED_SIZE, 102, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(44, 44, 44)
                .addComponent(AptaujasRedigesanaIzietPoga, javax.swing.GroupLayout.PREFERRED_SIZE, 102, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        AptaujasRedigesanaLayout.setVerticalGroup(
            AptaujasRedigesanaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, AptaujasRedigesanaLayout.createSequentialGroup()
                .addGroup(AptaujasRedigesanaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(AptaujasRedigesanaLayout.createSequentialGroup()
                        .addGap(14, 14, 14)
                        .addComponent(AptaujasRedigesanaAptaujuIzvele, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(AptaujasRedigesanaLayout.createSequentialGroup()
                        .addGap(73, 73, 73)
                        .addGroup(AptaujasRedigesanaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel26)
                            .addComponent(AptaujasRedigesanaPagajusaisJaut)
                            .addComponent(AptaujasRedigesanaNakamaisJaut)
                            .addComponent(AptaujasRedigesanaJautKartiba)
                            .addComponent(jLabel28)
                            .addComponent(AptaujasRedigesanaJautSkaits))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(AptaujasRedigesanaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel25)
                    .addComponent(AptaujasRedigesanaJautLogs, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(25, 25, 25)
                .addGroup(AptaujasRedigesanaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel22)
                    .addComponent(AptaujasRedigesana1AtbildeLogs, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(27, 27, 27)
                .addGroup(AptaujasRedigesanaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel23)
                    .addComponent(AptaujasRedigesana2AtbildeLogs, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(30, 30, 30)
                .addGroup(AptaujasRedigesanaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel24)
                    .addComponent(AptaujasRedigesana3AtbildeLogs, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(AptaujasRedigesanaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(AptaujasRedigesanaSaglabatPoga, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(AptaujasRedigesanaIzietPoga, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(24, 24, 24))
        );

        ParolesMainisana.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                ParolesMainisanaWindowClosing(evt);
            }
        });

        ParolesMainisanaMainitParoliPoga.setBackground(new java.awt.Color(204, 102, 255));
        ParolesMainisanaMainitParoliPoga.setForeground(new java.awt.Color(255, 255, 255));
        ParolesMainisanaMainitParoliPoga.setText("Mainīt paroli");
        ParolesMainisanaMainitParoliPoga.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                ParolesMainisanaMainitParoliPogaMouseClicked(evt);
            }
        });

        jLabel30.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel30.setText("Pašreizējā parole:");

        jLabel31.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel31.setText("Jaunā parole:");

        jLabel32.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel32.setText("Jaunā parole(atkārtoti):");

        ParolesMainisanaPasreizejaParole.setBackground(new java.awt.Color(204, 204, 204));

        ParolesMainisanaAtkartotiParole.setBackground(new java.awt.Color(204, 204, 204));

        ParolesMainisanaJaunaParole.setBackground(new java.awt.Color(204, 204, 204));

        javax.swing.GroupLayout ParolesMainisanaLayout = new javax.swing.GroupLayout(ParolesMainisana.getContentPane());
        ParolesMainisana.getContentPane().setLayout(ParolesMainisanaLayout);
        ParolesMainisanaLayout.setHorizontalGroup(
            ParolesMainisanaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, ParolesMainisanaLayout.createSequentialGroup()
                .addGap(17, 17, 17)
                .addGroup(ParolesMainisanaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel32)
                    .addComponent(jLabel31, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel30, javax.swing.GroupLayout.Alignment.LEADING))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 46, Short.MAX_VALUE)
                .addGroup(ParolesMainisanaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(ParolesMainisanaAtkartotiParole, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(ParolesMainisanaPasreizejaParole, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(ParolesMainisanaJaunaParole, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(29, 29, 29))
            .addGroup(ParolesMainisanaLayout.createSequentialGroup()
                .addGap(169, 169, 169)
                .addComponent(ParolesMainisanaMainitParoliPoga, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        ParolesMainisanaLayout.setVerticalGroup(
            ParolesMainisanaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, ParolesMainisanaLayout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addGroup(ParolesMainisanaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel30)
                    .addComponent(ParolesMainisanaPasreizejaParole, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(16, 16, 16)
                .addGroup(ParolesMainisanaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel31)
                    .addComponent(ParolesMainisanaJaunaParole, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 22, Short.MAX_VALUE)
                .addGroup(ParolesMainisanaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(ParolesMainisanaAtkartotiParole, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel32))
                .addGap(18, 18, 18)
                .addComponent(ParolesMainisanaMainitParoliPoga, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(16, 16, 16))
        );

        NavigacijasLogsRespondent.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                NavigacijasLogsRespondentWindowClosing(evt);
            }
        });

        jLabel33.setFont(new java.awt.Font("Segoe UI", 0, 30)); // NOI18N
        jLabel33.setText("Sveiki lietotajs!");

        RespondentsLogsPilditAptaujasPoga.setBackground(new java.awt.Color(204, 102, 255));
        RespondentsLogsPilditAptaujasPoga.setForeground(new java.awt.Color(255, 255, 255));
        RespondentsLogsPilditAptaujasPoga.setText("Pildīt aptaujas");
        RespondentsLogsPilditAptaujasPoga.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                RespondentsLogsPilditAptaujasPogaMouseClicked(evt);
            }
        });

        RespondentsLogsMainitParoliPoga.setBackground(new java.awt.Color(204, 102, 255));
        RespondentsLogsMainitParoliPoga.setForeground(new java.awt.Color(255, 255, 255));
        RespondentsLogsMainitParoliPoga.setText("Mainīt paroli");
        RespondentsLogsMainitParoliPoga.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                RespondentsLogsMainitParoliPogaMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout NavigacijasLogsRespondentLayout = new javax.swing.GroupLayout(NavigacijasLogsRespondent.getContentPane());
        NavigacijasLogsRespondent.getContentPane().setLayout(NavigacijasLogsRespondentLayout);
        NavigacijasLogsRespondentLayout.setHorizontalGroup(
            NavigacijasLogsRespondentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, NavigacijasLogsRespondentLayout.createSequentialGroup()
                .addContainerGap(122, Short.MAX_VALUE)
                .addComponent(jLabel33)
                .addGap(117, 117, 117))
            .addGroup(NavigacijasLogsRespondentLayout.createSequentialGroup()
                .addGap(151, 151, 151)
                .addGroup(NavigacijasLogsRespondentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(RespondentsLogsMainitParoliPoga, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(RespondentsLogsPilditAptaujasPoga, javax.swing.GroupLayout.DEFAULT_SIZE, 128, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        NavigacijasLogsRespondentLayout.setVerticalGroup(
            NavigacijasLogsRespondentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(NavigacijasLogsRespondentLayout.createSequentialGroup()
                .addGap(23, 23, 23)
                .addComponent(jLabel33)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 106, Short.MAX_VALUE)
                .addComponent(RespondentsLogsPilditAptaujasPoga, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(35, 35, 35)
                .addComponent(RespondentsLogsMainitParoliPoga, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(55, 55, 55))
        );

        NavigacijasLogsIntervetajs.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                NavigacijasLogsIntervetajsWindowClosing(evt);
            }
        });

        IntervetajsLogsMainitParoliPoga.setBackground(new java.awt.Color(204, 102, 255));
        IntervetajsLogsMainitParoliPoga.setForeground(new java.awt.Color(255, 255, 255));
        IntervetajsLogsMainitParoliPoga.setText("Mainīt paroli");
        IntervetajsLogsMainitParoliPoga.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                IntervetajsLogsMainitParoliPogaMouseClicked(evt);
            }
        });

        jLabel35.setFont(new java.awt.Font("Segoe UI", 0, 30)); // NOI18N
        jLabel35.setText("Sveiki lietotajs!");

        IntervetajsLogsApskatitiesRezultatusPoga.setBackground(new java.awt.Color(204, 102, 255));
        IntervetajsLogsApskatitiesRezultatusPoga.setForeground(new java.awt.Color(255, 255, 255));
        IntervetajsLogsApskatitiesRezultatusPoga.setText("Apskatīties rezultātus");
        IntervetajsLogsApskatitiesRezultatusPoga.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                IntervetajsLogsApskatitiesRezultatusPogaMouseClicked(evt);
            }
        });

        IntervetajsLogsVeidotAptaujuPoga.setBackground(new java.awt.Color(204, 102, 255));
        IntervetajsLogsVeidotAptaujuPoga.setForeground(new java.awt.Color(255, 255, 255));
        IntervetajsLogsVeidotAptaujuPoga.setText("Veidot aptauju");
        IntervetajsLogsVeidotAptaujuPoga.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                IntervetajsLogsVeidotAptaujuPogaMouseClicked(evt);
            }
        });

        IntervetajsLogsRedigetAptaujuPoga.setBackground(new java.awt.Color(204, 102, 255));
        IntervetajsLogsRedigetAptaujuPoga.setForeground(new java.awt.Color(255, 255, 255));
        IntervetajsLogsRedigetAptaujuPoga.setText("Rediģēt aptauju");
        IntervetajsLogsRedigetAptaujuPoga.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                IntervetajsLogsRedigetAptaujuPogaMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout NavigacijasLogsIntervetajsLayout = new javax.swing.GroupLayout(NavigacijasLogsIntervetajs.getContentPane());
        NavigacijasLogsIntervetajs.getContentPane().setLayout(NavigacijasLogsIntervetajsLayout);
        NavigacijasLogsIntervetajsLayout.setHorizontalGroup(
            NavigacijasLogsIntervetajsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, NavigacijasLogsIntervetajsLayout.createSequentialGroup()
                .addContainerGap(122, Short.MAX_VALUE)
                .addComponent(jLabel35)
                .addGap(117, 117, 117))
            .addGroup(NavigacijasLogsIntervetajsLayout.createSequentialGroup()
                .addGap(151, 151, 151)
                .addGroup(NavigacijasLogsIntervetajsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(IntervetajsLogsMainitParoliPoga, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(IntervetajsLogsApskatitiesRezultatusPoga, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(IntervetajsLogsVeidotAptaujuPoga, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(IntervetajsLogsRedigetAptaujuPoga, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        NavigacijasLogsIntervetajsLayout.setVerticalGroup(
            NavigacijasLogsIntervetajsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(NavigacijasLogsIntervetajsLayout.createSequentialGroup()
                .addGap(23, 23, 23)
                .addComponent(jLabel35)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 24, Short.MAX_VALUE)
                .addComponent(IntervetajsLogsApskatitiesRezultatusPoga, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(IntervetajsLogsVeidotAptaujuPoga, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(IntervetajsLogsRedigetAptaujuPoga, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(IntervetajsLogsMainitParoliPoga, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(40, 40, 40))
        );

        LoginLogs.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                LoginLogsWindowClosing(evt);
            }
        });

        jLabel34.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel34.setText("Lietotājvārds:");

        jLabel36.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel36.setText("Parole:");

        LoginLogsIeietPoga.setBackground(new java.awt.Color(204, 102, 255));
        LoginLogsIeietPoga.setForeground(new java.awt.Color(255, 255, 255));
        LoginLogsIeietPoga.setText("Ieiet");
        LoginLogsIeietPoga.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                LoginLogsIeietPogaMouseClicked(evt);
            }
        });

        LoginLogsLietotajvards.setBackground(new java.awt.Color(204, 204, 204));

        LoginLogsParole.setBackground(new java.awt.Color(204, 204, 204));
        LoginLogsParole.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                LoginLogsParoleKeyPressed(evt);
            }
        });

        javax.swing.GroupLayout LoginLogsLayout = new javax.swing.GroupLayout(LoginLogs.getContentPane());
        LoginLogs.getContentPane().setLayout(LoginLogsLayout);
        LoginLogsLayout.setHorizontalGroup(
            LoginLogsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(LoginLogsLayout.createSequentialGroup()
                .addGap(82, 82, 82)
                .addGroup(LoginLogsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel36)
                    .addComponent(jLabel34))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 21, Short.MAX_VALUE)
                .addGroup(LoginLogsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(LoginLogsParole, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 133, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(LoginLogsLietotajvards, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 133, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(61, 61, 61))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, LoginLogsLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(LoginLogsIeietPoga, javax.swing.GroupLayout.PREFERRED_SIZE, 82, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(158, 158, 158))
        );
        LoginLogsLayout.setVerticalGroup(
            LoginLogsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(LoginLogsLayout.createSequentialGroup()
                .addGap(62, 62, 62)
                .addGroup(LoginLogsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel34)
                    .addComponent(LoginLogsLietotajvards, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(47, 47, 47)
                .addGroup(LoginLogsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel36)
                    .addComponent(LoginLogsParole, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 42, Short.MAX_VALUE)
                .addComponent(LoginLogsIeietPoga, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(27, 27, 27))
        );

        AptaujasIzveleLogs.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                AptaujasIzveleLogsWindowClosing(evt);
            }
        });

        jLabel1.setFont(new java.awt.Font("Segoe UI", 0, 22)); // NOI18N
        jLabel1.setText("Kādas aptaujas rezultātu Jūs gribat redzēt?");

        RezultatuIzveleLogsApskatitPoga.setBackground(new java.awt.Color(204, 102, 255));
        RezultatuIzveleLogsApskatitPoga.setForeground(new java.awt.Color(255, 255, 255));
        RezultatuIzveleLogsApskatitPoga.setText("Apskatīt");
        RezultatuIzveleLogsApskatitPoga.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                RezultatuIzveleLogsApskatitPogaMouseClicked(evt);
            }
        });

        RezultatuIzveleLogsAtpakalPoga.setBackground(new java.awt.Color(204, 102, 255));
        RezultatuIzveleLogsAtpakalPoga.setForeground(new java.awt.Color(255, 255, 255));
        RezultatuIzveleLogsAtpakalPoga.setText("Atpakaļ");
        RezultatuIzveleLogsAtpakalPoga.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                RezultatuIzveleLogsAtpakalPogaMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout AptaujasIzveleLogsLayout = new javax.swing.GroupLayout(AptaujasIzveleLogs.getContentPane());
        AptaujasIzveleLogs.getContentPane().setLayout(AptaujasIzveleLogsLayout);
        AptaujasIzveleLogsLayout.setHorizontalGroup(
            AptaujasIzveleLogsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(AptaujasIzveleLogsLayout.createSequentialGroup()
                .addGap(19, 19, 19)
                .addGroup(AptaujasIzveleLogsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(RezultatuIzveleLogsAptaujasRezultati, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(20, Short.MAX_VALUE))
            .addGroup(AptaujasIzveleLogsLayout.createSequentialGroup()
                .addGap(61, 61, 61)
                .addComponent(RezultatuIzveleLogsApskatitPoga, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(RezultatuIzveleLogsAtpakalPoga, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(49, 49, 49))
        );
        AptaujasIzveleLogsLayout.setVerticalGroup(
            AptaujasIzveleLogsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(AptaujasIzveleLogsLayout.createSequentialGroup()
                .addGap(30, 30, 30)
                .addComponent(jLabel1)
                .addGap(47, 47, 47)
                .addComponent(RezultatuIzveleLogsAptaujasRezultati, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(50, 50, 50)
                .addGroup(AptaujasIzveleLogsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(RezultatuIzveleLogsApskatitPoga, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(RezultatuIzveleLogsAtpakalPoga, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(43, Short.MAX_VALUE))
        );

        AptaujuIzveideNosaukums.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                AptaujuIzveideNosaukumsWindowClosing(evt);
            }
        });

        AptaujuIzveideNosaukumsAtpakalPoga.setBackground(new java.awt.Color(204, 102, 255));
        AptaujuIzveideNosaukumsAtpakalPoga.setForeground(new java.awt.Color(255, 255, 255));
        AptaujuIzveideNosaukumsAtpakalPoga.setText("Atpakaļ");
        AptaujuIzveideNosaukumsAtpakalPoga.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                AptaujuIzveideNosaukumsAtpakalPogaMouseClicked(evt);
            }
        });

        AptaujuIzveideNosaukumsTalaklPoga.setBackground(new java.awt.Color(204, 102, 255));
        AptaujuIzveideNosaukumsTalaklPoga.setForeground(new java.awt.Color(255, 255, 255));
        AptaujuIzveideNosaukumsTalaklPoga.setText("Tālāk");
        AptaujuIzveideNosaukumsTalaklPoga.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                AptaujuIzveideNosaukumsTalaklPogaMouseClicked(evt);
            }
        });

        AptaujasNos.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        AptaujasNos.setText("Aptaujas nosaukums:");

        javax.swing.GroupLayout AptaujuIzveideNosaukumsLayout = new javax.swing.GroupLayout(AptaujuIzveideNosaukums.getContentPane());
        AptaujuIzveideNosaukums.getContentPane().setLayout(AptaujuIzveideNosaukumsLayout);
        AptaujuIzveideNosaukumsLayout.setHorizontalGroup(
            AptaujuIzveideNosaukumsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(AptaujuIzveideNosaukumsLayout.createSequentialGroup()
                .addGap(30, 30, 30)
                .addGroup(AptaujuIzveideNosaukumsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(AptaujuIzveideNosaukumsLayout.createSequentialGroup()
                        .addComponent(AptaujasNos)
                        .addGap(18, 18, 18)
                        .addComponent(AptaujuIzveideNosaukumsAptaujasNosaukums, javax.swing.GroupLayout.PREFERRED_SIZE, 186, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(AptaujuIzveideNosaukumsLayout.createSequentialGroup()
                        .addComponent(AptaujuIzveideNosaukumsTalaklPoga, javax.swing.GroupLayout.PREFERRED_SIZE, 141, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(AptaujuIzveideNosaukumsAtpakalPoga, javax.swing.GroupLayout.PREFERRED_SIZE, 141, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)))
                .addGap(21, 21, 21))
        );
        AptaujuIzveideNosaukumsLayout.setVerticalGroup(
            AptaujuIzveideNosaukumsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, AptaujuIzveideNosaukumsLayout.createSequentialGroup()
                .addContainerGap(61, Short.MAX_VALUE)
                .addGroup(AptaujuIzveideNosaukumsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(AptaujuIzveideNosaukumsAptaujasNosaukums, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(AptaujasNos))
                .addGap(40, 40, 40)
                .addGroup(AptaujuIzveideNosaukumsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(AptaujuIzveideNosaukumsTalaklPoga, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(AptaujuIzveideNosaukumsAtpakalPoga, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(49, 49, 49))
        );

        AdminLietotajiDialog.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                AdminLietotajiDialogWindowClosing(evt);
            }
        });

        jLabel2.setFont(new java.awt.Font("Segoe UI", 0, 24)); // NOI18N
        jLabel2.setText("Administratora panelis");

        lietotajiTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null}
            },
            new String [] {
                "ID", "Lietotājvārds", "Loma"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.String.class, java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        jScrollPane1.setViewportView(lietotajiTable);
        if (lietotajiTable.getColumnModel().getColumnCount() > 0) {
            lietotajiTable.getColumnModel().getColumn(1).setResizable(false);
        }

        javax.swing.GroupLayout AdminLietotajiDialogLayout = new javax.swing.GroupLayout(AdminLietotajiDialog.getContentPane());
        AdminLietotajiDialog.getContentPane().setLayout(AdminLietotajiDialogLayout);
        AdminLietotajiDialogLayout.setHorizontalGroup(
            AdminLietotajiDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, AdminLietotajiDialogLayout.createSequentialGroup()
                .addContainerGap(85, Short.MAX_VALUE)
                .addComponent(jLabel2)
                .addGap(77, 77, 77))
            .addGroup(AdminLietotajiDialogLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                .addContainerGap())
        );
        AdminLietotajiDialogLayout.setVerticalGroup(
            AdminLietotajiDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(AdminLietotajiDialogLayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 185, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(20, Short.MAX_VALUE))
        );

        AdminLogsNavigacija.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                AdminLogsNavigacijaWindowClosing(evt);
            }
        });

        jLabel37.setFont(new java.awt.Font("Segoe UI", 0, 30)); // NOI18N
        jLabel37.setText("Sveiki admin!");

        AdminSkatitDatusPoga.setBackground(new java.awt.Color(204, 102, 255));
        AdminSkatitDatusPoga.setForeground(new java.awt.Color(255, 255, 255));
        AdminSkatitDatusPoga.setText("Apskatīties datus");
        AdminSkatitDatusPoga.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                AdminSkatitDatusPogaMouseClicked(evt);
            }
        });

        AdminLogsMainitParoli.setBackground(new java.awt.Color(204, 102, 255));
        AdminLogsMainitParoli.setForeground(new java.awt.Color(255, 255, 255));
        AdminLogsMainitParoli.setText("Mainīt paroli");
        AdminLogsMainitParoli.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                AdminLogsMainitParoliMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout AdminLogsNavigacijaLayout = new javax.swing.GroupLayout(AdminLogsNavigacija.getContentPane());
        AdminLogsNavigacija.getContentPane().setLayout(AdminLogsNavigacijaLayout);
        AdminLogsNavigacijaLayout.setHorizontalGroup(
            AdminLogsNavigacijaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, AdminLogsNavigacijaLayout.createSequentialGroup()
                .addContainerGap(108, Short.MAX_VALUE)
                .addComponent(jLabel37)
                .addGap(117, 117, 117))
            .addGroup(AdminLogsNavigacijaLayout.createSequentialGroup()
                .addGap(125, 125, 125)
                .addGroup(AdminLogsNavigacijaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(AdminLogsMainitParoli, javax.swing.GroupLayout.PREFERRED_SIZE, 128, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(AdminSkatitDatusPoga, javax.swing.GroupLayout.PREFERRED_SIZE, 128, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        AdminLogsNavigacijaLayout.setVerticalGroup(
            AdminLogsNavigacijaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(AdminLogsNavigacijaLayout.createSequentialGroup()
                .addGap(23, 23, 23)
                .addComponent(jLabel37)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 61, Short.MAX_VALUE)
                .addComponent(AdminSkatitDatusPoga, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(31, 31, 31)
                .addComponent(AdminLogsMainitParoli, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(65, 65, 65))
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setBackground(new java.awt.Color(204, 153, 255));
        addWindowFocusListener(new java.awt.event.WindowFocusListener() {
            public void windowGainedFocus(java.awt.event.WindowEvent evt) {
                formWindowGainedFocus(evt);
            }
            public void windowLostFocus(java.awt.event.WindowEvent evt) {
            }
        });
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowOpened(java.awt.event.WindowEvent evt) {
                formWindowOpened(evt);
            }
        });

        GalvenaisLogsIeietPoga.setBackground(new java.awt.Color(204, 102, 255));
        GalvenaisLogsIeietPoga.setForeground(new java.awt.Color(255, 255, 255));
        GalvenaisLogsIeietPoga.setText("Ieiet");
        GalvenaisLogsIeietPoga.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                GalvenaisLogsIeietPogaMouseClicked(evt);
            }
        });

        GalvenaisLogsRegistretiesPoga.setBackground(new java.awt.Color(204, 102, 255));
        GalvenaisLogsRegistretiesPoga.setForeground(new java.awt.Color(255, 255, 255));
        GalvenaisLogsRegistretiesPoga.setText("Reģistrēties");
        GalvenaisLogsRegistretiesPoga.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                GalvenaisLogsRegistretiesPogaMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(141, 141, 141)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(GalvenaisLogsRegistretiesPoga, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(GalvenaisLogsIeietPoga, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(147, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(52, 52, 52)
                .addComponent(GalvenaisLogsIeietPoga, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(37, 37, 37)
                .addComponent(GalvenaisLogsRegistretiesPoga, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(56, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void formWindowGainedFocus(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowGainedFocus
        getContentPane().setBackground(new java.awt.Color(232, 220, 247));
    }//GEN-LAST:event_formWindowGainedFocus

    private void GalvenaisLogsRegistretiesPogaMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_GalvenaisLogsRegistretiesPogaMouseClicked
        this.setVisible(false);
        LietRegistracija.setVisible(true);
        LietRegistracija.setSize(538, 400);
        LietRegistracija.setLocationRelativeTo(null);
    }//GEN-LAST:event_GalvenaisLogsRegistretiesPogaMouseClicked

    private void LietRegistracijaAtpakalPogaMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_LietRegistracijaAtpakalPogaMouseClicked
        LietRegistracija.setVisible(false);
        this.setVisible(true);
        this.setSize(400, 260);
        this.setLocationRelativeTo(null);
    }//GEN-LAST:event_LietRegistracijaAtpakalPogaMouseClicked

    private void LietRegistracijaWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_LietRegistracijaWindowClosing
        LietRegistracija.setVisible(false);
        this.setVisible(true);
        this.setSize(400, 260);
        this.setLocationRelativeTo(null);
    }//GEN-LAST:event_LietRegistracijaWindowClosing

    private void formWindowOpened(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowOpened
        this.setSize(400, 260);
        this.setLocationRelativeTo(null);
    }//GEN-LAST:event_formWindowOpened

    private void IntervetajsLogsMainitParoliPogaMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_IntervetajsLogsMainitParoliPogaMouseClicked
        if (!util.Session.isLoggedIn()) {
            JOptionPane.showMessageDialog(this, "Kļūda!");
            return;
        }
        ParolesMainisanaPasreizejaParole.setText("");
        ParolesMainisanaAtkartotiParole.setText("");
        ParolesMainisanaJaunaParole.setText("");
        passwordFromAdmin = false;
        passwordFromRespondent = false;
        passwordFromRespondent = false;
        NavigacijasLogsIntervetajs.setVisible(false);
        ParolesMainisana.setVisible(true);
        ParolesMainisana.setSize(455, 295);
        ParolesMainisana.setLocationRelativeTo(null);
    }//GEN-LAST:event_IntervetajsLogsMainitParoliPogaMouseClicked

    private void RespondentsLogsMainitParoliPogaMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_RespondentsLogsMainitParoliPogaMouseClicked
        if (!util.Session.isLoggedIn()) {
            JOptionPane.showMessageDialog(this, "Kļūda!");
            return;
        }
        ParolesMainisanaPasreizejaParole.setText("");
        ParolesMainisanaAtkartotiParole.setText("");
        ParolesMainisanaJaunaParole.setText("");
        passwordFromRespondent = true;
        NavigacijasLogsRespondent.setVisible(false);
        ParolesMainisana.setVisible(true);
        ParolesMainisana.setSize(455, 295);
        ParolesMainisana.setLocationRelativeTo(null);
    }//GEN-LAST:event_RespondentsLogsMainitParoliPogaMouseClicked

    private void LoginLogsIeietPogaMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_LoginLogsIeietPogaMouseClicked
        String username = LoginLogsLietotajvards.getText().trim();
        String password = LoginLogsParole.getText();

        String role = login(username, password);

        if (role != null) {
            LoginLogs.setVisible(false);

            if ("Admin".equalsIgnoreCase(role)) {
                AdminLogsNavigacija.setSize(408, 330);
                AdminLogsNavigacija.setLocationRelativeTo(null);
                AdminLogsNavigacija.setVisible(true);

            } else if ("Interviewer".equalsIgnoreCase(role)) {
                NavigacijasLogsIntervetajs.setSize(458, 390);
                NavigacijasLogsIntervetajs.setLocationRelativeTo(null);
                NavigacijasLogsIntervetajs.setVisible(true);

            } else if ("Respondent".equalsIgnoreCase(role)) {
                NavigacijasLogsRespondent.setSize(458, 330);
                NavigacijasLogsRespondent.setLocationRelativeTo(null);
                NavigacijasLogsRespondent.setVisible(true);

            } else {
                LoginLogs.setVisible(true);
                JOptionPane.showMessageDialog(
                        this, "Neatpazīta lietotāja loma: " + role
                );
            }

        } else {
            JOptionPane.showMessageDialog(
                    this, "Nepareizs lietotājvārds vai parole."
            );
        }
    }//GEN-LAST:event_LoginLogsIeietPogaMouseClicked

    private void GalvenaisLogsIeietPogaMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_GalvenaisLogsIeietPogaMouseClicked
        this.setVisible(false);
        LoginLogs.setVisible(true);
        LoginLogs.setSize(400, 320);
        LoginLogs.setLocationRelativeTo(null);
        LoginLogsParole.setText("");
        LoginLogsLietotajvards.setText("");
    }//GEN-LAST:event_GalvenaisLogsIeietPogaMouseClicked

    private void RezultatuIzveleLogsApskatitPogaMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_RezultatuIzveleLogsApskatitPogaMouseClicked
        int index = RezultatuIzveleLogsAptaujasRezultati.getSelectedIndex();

        if (index < 0) {
            JOptionPane.showMessageDialog(this, "Lūdzu, izvēlieties aptauju!");
            return;
        }

        Survey survey = currentSurveys.get(index);

        if (respondentMode) {
            try {
                fillQuestions = new SurveyDAO().getQuestionsBySurveyId(survey.getSurveyId());
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Datubāzes kļūda, ielādējot jautājumus.");
                e.printStackTrace();
                return;
            }

            if (fillQuestions.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Šai aptaujai vēl nav jautājumu.");
                return;
            }

            fillIndex = 0;
            givenAnswers = new ArrayList<>();
            AtbilzuIevade.setTitle(survey.getTitle());
            showFillQuestion();

            AptaujasIzveleLogs.setVisible(false);
            AtbilzuIevade.setVisible(true);
            AtbilzuIevade.setSize(420, 300);
            AtbilzuIevade.setLocationRelativeTo(null);

        } else {
            RezultatuApkAtt.setTitle(survey.getTitle());

            try {
                currentQuestions = new SurveyDAO().getQuestionsBySurveyId(survey.getSurveyId());
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Datubāzes kļūda, ielādējot jautājumus");
                e.printStackTrace();
                return;
            }

            if (currentQuestions.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Šai aptaujai vēl nav jautājumu.");
                return;
            }

            currentQuestionIndex = 0;
            showQuestionHeader();

            AptaujasIzveleLogs.setVisible(false);
            RezultatuApkAtt.setVisible(true);
            RezultatuApkAtt.setSize(403, 350);
            RezultatuApkAtt.setLocationRelativeTo(null);
        }
    }//GEN-LAST:event_RezultatuIzveleLogsApskatitPogaMouseClicked

    private void RezultatuIzveleLogsAtpakalPogaMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_RezultatuIzveleLogsAtpakalPogaMouseClicked
        backFromSurveyChooser();
    }//GEN-LAST:event_RezultatuIzveleLogsAtpakalPogaMouseClicked

    private void IntervetajsLogsVeidotAptaujuPogaMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_IntervetajsLogsVeidotAptaujuPogaMouseClicked
        NavigacijasLogsIntervetajs.setVisible(false);
        AptaujuIzveideNosaukums.setVisible(true);
        AptaujuIzveideNosaukums.setSize(440, 250);
        AptaujuIzveideNosaukums.setLocationRelativeTo(null);
    }//GEN-LAST:event_IntervetajsLogsVeidotAptaujuPogaMouseClicked

    private void AptaujuIzveideNosaukumsTalaklPogaMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_AptaujuIzveideNosaukumsTalaklPogaMouseClicked
        String title = AptaujuIzveideNosaukumsAptaujasNosaukums.getText().trim();
        if (title.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ievadiet aptaujas nosaukumu!");
            return;
        }
        if (title.length() > 50) {
            JOptionPane.showMessageDialog(this, "Nosaukums ir pārāk garš (maksimums 50 simboli).");
            return;
        }
        newSurveyTitle = title;
        newQuestions = new ArrayList<>();
        clearNewQuestionFields();
        showNewQuestionNumber();

        AptaujuIzveideNosaukums.setVisible(false);
        AptaujuIzveide.setVisible(true);
        AptaujuIzveide.setSize(425, 405);
        AptaujuIzveide.setLocationRelativeTo(null);
    }//GEN-LAST:event_AptaujuIzveideNosaukumsTalaklPogaMouseClicked

    private void AptaujuIzveideNosaukumsAtpakalPogaMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_AptaujuIzveideNosaukumsAtpakalPogaMouseClicked
        AptaujuIzveideNosaukums.setVisible(false);
        NavigacijasLogsIntervetajs.setVisible(true);
        NavigacijasLogsIntervetajs.setSize(458, 390);
        NavigacijasLogsIntervetajs.setLocationRelativeTo(null);
    }//GEN-LAST:event_AptaujuIzveideNosaukumsAtpakalPogaMouseClicked

    private void AptaujuIzveideWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_AptaujuIzveideWindowClosing
        AptaujuIzveide.setVisible(false);
        NavigacijasLogsIntervetajs.setVisible(true);
        NavigacijasLogsIntervetajs.setSize(458, 390);
        NavigacijasLogsIntervetajs.setLocationRelativeTo(null);
    }//GEN-LAST:event_AptaujuIzveideWindowClosing

    private void ParolesMainisanaWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_ParolesMainisanaWindowClosing
        backFromPasswordChange();
    }//GEN-LAST:event_ParolesMainisanaWindowClosing

    private void IntervetajsLogsApskatitiesRezultatusPogaMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_IntervetajsLogsApskatitiesRezultatusPogaMouseClicked
        try {
            java.util.List<Survey> mySurveys = new SurveyDAO().getSurveysByUserId(util.Session.getUserId());
            if (mySurveys.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Jums vēl nav izveidotu aptauju.");
                return;
            }
            openSurveyChooser(mySurveys, false);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Datubāzes kļūda, ielādējot aptaujas.");
            e.printStackTrace();
        }
    }//GEN-LAST:event_IntervetajsLogsApskatitiesRezultatusPogaMouseClicked

    private void IntervetajsLogsRedigetAptaujuPogaMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_IntervetajsLogsRedigetAptaujuPogaMouseClicked
        try {
            editSurveys = new SurveyDAO().getSurveysByUserId(util.Session.getUserId());
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Datubāzes kļūda, ielādējot aptaujas.");
            e.printStackTrace();
            return;
        }
        if (editSurveys.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Jums vēl nav izveidotu aptauju.");
            return;
        }

        editComboLoading = true;
        AptaujasRedigesanaAptaujuIzvele.removeAllItems();
        for (Survey survey : editSurveys) {
            AptaujasRedigesanaAptaujuIzvele.addItem(survey.getTitle());
        }
        editComboLoading = false;

        NavigacijasLogsIntervetajs.setVisible(false);
        AptaujasRedigesana.setVisible(true);
        AptaujasRedigesana.setSize(450, 470);
        AptaujasRedigesana.setLocationRelativeTo(null);
        loadEditSurvey(0);
    }//GEN-LAST:event_IntervetajsLogsRedigetAptaujuPogaMouseClicked

    private void AptaujasRedigesanaIzietPogaMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_AptaujasRedigesanaIzietPogaMouseClicked
        AptaujasRedigesana.setVisible(false);
        NavigacijasLogsIntervetajs.setVisible(true);
        NavigacijasLogsIntervetajs.setSize(458, 390);
        NavigacijasLogsIntervetajs.setLocationRelativeTo(null);
    }//GEN-LAST:event_AptaujasRedigesanaIzietPogaMouseClicked

    private void RezultatuApkAttWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_RezultatuApkAttWindowClosing
        RezultatuApkAtt.setVisible(false);
        AptaujasIzveleLogs.setVisible(true);
        AptaujasIzveleLogs.setSize(470, 320);
        AptaujasIzveleLogs.setLocationRelativeTo(null);
    }//GEN-LAST:event_RezultatuApkAttWindowClosing

    private void AptaujasRedigesanaWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_AptaujasRedigesanaWindowClosing
        AptaujasRedigesana.setVisible(false);
        NavigacijasLogsIntervetajs.setVisible(true);
        NavigacijasLogsIntervetajs.setSize(458, 390);
        NavigacijasLogsIntervetajs.setLocationRelativeTo(null);
    }//GEN-LAST:event_AptaujasRedigesanaWindowClosing

    private void AptaujasIzveleLogsWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_AptaujasIzveleLogsWindowClosing
        backFromSurveyChooser();
    }//GEN-LAST:event_AptaujasIzveleLogsWindowClosing

    private void NavigacijasLogsIntervetajsWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_NavigacijasLogsIntervetajsWindowClosing
        NavigacijasLogsIntervetajs.setVisible(false);
        LoginLogs.setVisible(true);
        LoginLogs.setSize(400, 320);
        LoginLogs.setLocationRelativeTo(null);
        LoginLogsParole.setText("");
        LoginLogsLietotajvards.setText("");
    }//GEN-LAST:event_NavigacijasLogsIntervetajsWindowClosing

    private void LoginLogsWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_LoginLogsWindowClosing
        // Izdzēš pašreizējo lietotāju
        util.Session.endSession();
        LoginLogs.setVisible(false);
        this.setVisible(true);
        this.setSize(400, 260);
        this.setLocationRelativeTo(null);
    }//GEN-LAST:event_LoginLogsWindowClosing

    private void AptaujuIzveideNosaukumsWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_AptaujuIzveideNosaukumsWindowClosing
        AptaujuIzveideNosaukums.setVisible(false);
        NavigacijasLogsIntervetajs.setVisible(true);
        NavigacijasLogsIntervetajs.setSize(458, 390);
        NavigacijasLogsIntervetajs.setLocationRelativeTo(null);
    }//GEN-LAST:event_AptaujuIzveideNosaukumsWindowClosing

    private void LietRegistracijaHelpPogaMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_LietRegistracijaHelpPogaMouseClicked
        JOptionPane.showMessageDialog(
                LietRegistracija,
                "Ievadiet lietotājvārdu, paroli un paroles apstiprinājumu. "
                + "Izvēlieties lietotāja lomu un nospiediet Reģistrēties.",
                "Palīdzība",
                JOptionPane.INFORMATION_MESSAGE);
    }//GEN-LAST:event_LietRegistracijaHelpPogaMouseClicked

    private void LietRegistracijaRegistretiesPogaMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_LietRegistracijaRegistretiesPogaMouseClicked
        String username = jTextField3.getText().trim();
        String password = new String(jTextField4.getText());
        String confirm = new String(jTextField5.getText());

        String role = LietRegistracijaIntervetajs.isSelected() ? "Interviewer" : "Respondent";

        if (username.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Aizpildiet visus laukus.");
            return;
        }
        if (!password.equals(confirm)) {
            JOptionPane.showMessageDialog(this, "Paroles nesakrīt.");
            return;
        }

        boolean isRegistered = register(username, password, role);

        if (isRegistered) {
            JOptionPane.showMessageDialog(this, "Reģistrācija veiksmīga.");

            LietRegistracija.setVisible(false);
            this.setVisible(true);
            this.setSize(400, 260);
            this.setLocationRelativeTo(null);
        } else {
            JOptionPane.showMessageDialog(this, "Lietotājvārds jau eksistē vai radās datubāzes kļūda.");
        }
    }//GEN-LAST:event_LietRegistracijaRegistretiesPogaMouseClicked

    private void LoginLogsParoleKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_LoginLogsParoleKeyPressed
        // Pievienota funkcija kad mēģīni loginoties var uzspiest enter lai nespiest pogu "Ieiet"
        if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
            String username = LoginLogsLietotajvards.getText().trim();
            String password = LoginLogsParole.getText();

            String role = login(username, password);

            if (role != null) {
                LoginLogs.setVisible(false);

                if ("Admin".equalsIgnoreCase(role)) {
                    AdminLogsNavigacija.setSize(408, 330);
                    AdminLogsNavigacija.setLocationRelativeTo(null);
                    AdminLogsNavigacija.setVisible(true);

                } else if ("Interviewer".equalsIgnoreCase(role)) {
                    NavigacijasLogsIntervetajs.setSize(458, 390);
                    NavigacijasLogsIntervetajs.setLocationRelativeTo(null);
                    NavigacijasLogsIntervetajs.setVisible(true);

                } else if ("Respondent".equalsIgnoreCase(role)) {
                    NavigacijasLogsRespondent.setSize(458, 330);
                    NavigacijasLogsRespondent.setLocationRelativeTo(null);
                    NavigacijasLogsRespondent.setVisible(true);

                } else {
                    LoginLogs.setVisible(true);
                    JOptionPane.showMessageDialog(
                            this, "Neatpazīta lietotāja loma: " + role
                    );
                }

            } else {
                JOptionPane.showMessageDialog(
                        this, "Nepareizs lietotājvārds vai parole."
                );
            }
        }
    }//GEN-LAST:event_LoginLogsParoleKeyPressed

    private void NavigacijasLogsRespondentWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_NavigacijasLogsRespondentWindowClosing
        NavigacijasLogsRespondent.setVisible(false);
        LoginLogs.setVisible(true);
        LoginLogs.setSize(400, 320);
        LoginLogs.setLocationRelativeTo(null);
        LoginLogsParole.setText("");
        LoginLogsLietotajvards.setText("");
    }//GEN-LAST:event_NavigacijasLogsRespondentWindowClosing

    private void ParolesMainisanaMainitParoliPogaMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_ParolesMainisanaMainitParoliPogaMouseClicked
        int userId = util.Session.getUserId();

        if (ParolesMainisanaJaunaParole.getText().isEmpty() || ParolesMainisanaAtkartotiParole.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Aizpildiet visus laukus!");
            return;
        }
        if (!ParolesMainisanaJaunaParole.getText().equals(ParolesMainisanaAtkartotiParole.getText())) {
            JOptionPane.showMessageDialog(this, "Paroles nesakrīt!");
            return;
        }

        boolean izdevas = changePassword(userId, ParolesMainisanaJaunaParole.getText(), ParolesMainisanaAtkartotiParole.getText());
        if (izdevas) {
            JOptionPane.showMessageDialog(this, "Parole veiksmīgi nomainīta!");
            util.Session.endSession();
            ParolesMainisana.setVisible(false);
            LoginLogs.setVisible(true);
            LoginLogs.setSize(400, 320);
            LoginLogs.setLocationRelativeTo(null);
            LoginLogsParole.setText("");
            LoginLogsLietotajvards.setText("");
        } else {
            JOptionPane.showMessageDialog(this, "Kļūda!");
        }
    }//GEN-LAST:event_ParolesMainisanaMainitParoliPogaMouseClicked

    private void RezultatuApkAttParaditRezultatuMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_RezultatuApkAttParaditRezultatuMouseClicked
        showCurrentQuestionResults();
    }//GEN-LAST:event_RezultatuApkAttParaditRezultatuMouseClicked

    private void RezultatuApkAttIeprieksejaisJautPogaMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_RezultatuApkAttIeprieksejaisJautPogaMouseClicked
        if (currentQuestionIndex > 0) {
            currentQuestionIndex--;
            showQuestionHeader();
        }
    }//GEN-LAST:event_RezultatuApkAttIeprieksejaisJautPogaMouseClicked

    private void RezultatuApkAttNakamaisJautPogaMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_RezultatuApkAttNakamaisJautPogaMouseClicked
        if (currentQuestionIndex < currentQuestions.size() - 1) {
            currentQuestionIndex++;
            showQuestionHeader();
        }
    }//GEN-LAST:event_RezultatuApkAttNakamaisJautPogaMouseClicked

    private void AptaujuIzveidePievienotJautMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_AptaujuIzveidePievienotJautMouseClicked
        if (addCurrentQuestion()) {
            clearNewQuestionFields();
            showNewQuestionNumber();
        }
    }//GEN-LAST:event_AptaujuIzveidePievienotJautMouseClicked

    private void AptaujuIzveideDzestPogaMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_AptaujuIzveideDzestPogaMouseClicked
        if (!newQuestionFieldsEmpty()) {
            clearNewQuestionFields();
        } else if (!newQuestions.isEmpty()) {
            newQuestions.remove(newQuestions.size() - 1);
            showNewQuestionNumber();
        }
    }//GEN-LAST:event_AptaujuIzveideDzestPogaMouseClicked

    private void AptaujuIzveideIzveidotAptaujuMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_AptaujuIzveideIzveidotAptaujuMouseClicked
        if (!newQuestionFieldsEmpty() && !addCurrentQuestion()) {
            return;
        }
        if (newQuestions.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Pievienojiet vismaz vienu jautājumu!");
            return;
        }

        Survey survey = new Survey();
        survey.setTitle(newSurveyTitle);
        survey.setUserId(util.Session.getUserId());

        try {
            new SurveyDAO().createSurveyWithQuestions(survey, newQuestions);
            JOptionPane.showMessageDialog(this, "Aptauja \"" + newSurveyTitle + "\" izveidota (jautājumi: " + newQuestions.size() + ").");
            newQuestions = new ArrayList<>();
            AptaujuIzveideNosaukumsAptaujasNosaukums.setText("");
            clearNewQuestionFields();
            AptaujuIzveide.setVisible(false);
            NavigacijasLogsIntervetajs.setVisible(true);
            NavigacijasLogsIntervetajs.setSize(458, 390);
            NavigacijasLogsIntervetajs.setLocationRelativeTo(null);
        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                JOptionPane.showMessageDialog(this, "Kāds no jautājumiem jau eksistē datubāzē");
            } else {
                JOptionPane.showMessageDialog(this, "Datubāzes kļūda");
            }
            e.printStackTrace();
        }
    }//GEN-LAST:event_AptaujuIzveideIzveidotAptaujuMouseClicked

    private void AtbilzuIevadeTalakPogaMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_AtbilzuIevadeTalakPogaMouseClicked
        Question q = fillQuestions.get(fillIndex);

        String answer = null;
        if (AtbilzuIevade1AtbildeBox.isSelected()) {
            answer = q.getAnswer_1();
        } else if (AtbilzuIevade2AtbildeBox.isSelected()) {
            answer = q.getAnswer_2();
        } else if (AtbilzuIevade3AtbildeBox.isSelected()) {
            answer = q.getAnswer_3();
        }
        if (answer == null) {
            JOptionPane.showMessageDialog(this, "Lūdzu, izvēlieties vienu atbildi!");
            return;
        }

        RespondentQuestion rq = new RespondentQuestion();
        rq.setQuestionId(q.getQuestionId());
        rq.setUserId(util.Session.getUserId());
        rq.setAnswer(answer);
        givenAnswers.add(rq);

        if (fillIndex < fillQuestions.size() - 1) {
            fillIndex++;
            showFillQuestion();
            return;
        }

        try {
            new RespondentQuestionDAO().saveAnswers(givenAnswers);
            JOptionPane.showMessageDialog(this, "Paldies! Jūsu atbildes ir saglabātas.");
            finishFilling();
        } catch (SQLException e) {
            givenAnswers.remove(givenAnswers.size() - 1);
            JOptionPane.showMessageDialog(this, "Datubāzes kļūda");
            e.printStackTrace();
        }
    }//GEN-LAST:event_AtbilzuIevadeTalakPogaMouseClicked

    private void AtbilzuIevadeWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_AtbilzuIevadeWindowClosing
        int choice = JOptionPane.showConfirmDialog(AtbilzuIevade,
                "Iziet no aptaujas? Atbildes netiks saglabātas.", "Apstiprinājums", JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            finishFilling();
        }
    }//GEN-LAST:event_AtbilzuIevadeWindowClosing

    private void RespondentsLogsPilditAptaujasPogaMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_RespondentsLogsPilditAptaujasPogaMouseClicked
        try {
            java.util.List<Survey> available = new SurveyDAO().getSurveysAvailableForRespondent(util.Session.getUserId());
            if (available.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Pašlaik nav aptauju, ko Jūs varētu aizpildīt.");
                return;
            }
            openSurveyChooser(available, true);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Datubāzes kļūda, ielādējot aptaujas.");
            e.printStackTrace();
        }
    }//GEN-LAST:event_RespondentsLogsPilditAptaujasPogaMouseClicked

    private void AptaujasRedigesanaAptaujuIzveleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_AptaujasRedigesanaAptaujuIzveleActionPerformed
        if (editComboLoading) {
            return;
        }
        int index = AptaujasRedigesanaAptaujuIzvele.getSelectedIndex();
        if (index >= 0) {
            loadEditSurvey(index);
        }
    }//GEN-LAST:event_AptaujasRedigesanaAptaujuIzveleActionPerformed

    private void AptaujasRedigesanaNakamaisJautMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_AptaujasRedigesanaNakamaisJautMouseClicked
        if (editIndex < editQuestions.size() - 1) {
            editIndex++;
            showEditQuestion();
        }
    }//GEN-LAST:event_AptaujasRedigesanaNakamaisJautMouseClicked

    private void AptaujasRedigesanaPagajusaisJautMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_AptaujasRedigesanaPagajusaisJautMouseClicked
        if (editIndex > 0) {
            editIndex--;
            showEditQuestion();
        }
    }//GEN-LAST:event_AptaujasRedigesanaPagajusaisJautMouseClicked

    private void AptaujasRedigesanaSaglabatPogaMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_AptaujasRedigesanaSaglabatPogaMouseClicked
        if (editQuestions.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Šai aptaujai nav jautājumu, ko saglabāt.");
            return;
        }
        Question current = editQuestions.get(editIndex);

        String text = AptaujasRedigesanaJautLogs.getText().trim();
        String a1 = AptaujasRedigesana1AtbildeLogs.getText().trim();
        String a2 = AptaujasRedigesana2AtbildeLogs.getText().trim();
        String a3 = AptaujasRedigesana3AtbildeLogs.getText().trim();

        if (text.isEmpty() || a1.isEmpty() || a2.isEmpty() || a3.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Aizpildiet jautājumu un visas trīs atbildes!");
            return;
        }
        if (text.length() > 100) {
            JOptionPane.showMessageDialog(this, "Jautājums ir pārāk garš (maksimums 100 simboli).");
            return;
        }
        if (a1.length() > 50 || a2.length() > 50 || a3.length() > 50) {
            JOptionPane.showMessageDialog(this, "Atbilde ir pārāk gara (maksimums 50 simboli).");
            return;
        }
        for (int i = 0; i < editQuestions.size(); i++) {
            if (i != editIndex && editQuestions.get(i).getQuestion().equalsIgnoreCase(text)) {
                JOptionPane.showMessageDialog(this, "Šajā aptaujā jau ir šāds jautājums!");
                return;
            }
        }

        boolean answersChanged = !a1.equals(current.getAnswer_1())
                || !a2.equals(current.getAnswer_2())
                || !a3.equals(current.getAnswer_3());

        try {
            if (answersChanged && !new RespondentQuestionDAO().getAnswersByQuestionId(current.getQuestionId()).isEmpty()) {
                JOptionPane.showMessageDialog(this, "Uz šo jautājumu jau ir atbildēts, tāpēc atbilžu variantus mainīt nevar.\nVar mainīt tikai jautājuma tekstu.");
                showEditQuestion();
                return;
            }

            Question updated = new Question();
            updated.setQuestionId(current.getQuestionId());
            updated.setSurveyId(current.getSurveyId());
            updated.setQuestion(text);
            updated.setAnswer_1(a1);
            updated.setAnswer_2(a2);
            updated.setAnswer_3(a3);

            new SurveyDAO().updateQuestion(updated);
            editQuestions.set(editIndex, updated);
            JOptionPane.showMessageDialog(this, "Jautājums saglabāts!");
        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                JOptionPane.showMessageDialog(this, "Šāds jautājums jau eksistē datubāzē");
            } else {
                JOptionPane.showMessageDialog(this, "Datubāzes kļūda");
            }
            e.printStackTrace();
        }
    }//GEN-LAST:event_AptaujasRedigesanaSaglabatPogaMouseClicked

    private void AdminLietotajiDialogWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_AdminLietotajiDialogWindowClosing
        AdminLietotajiDialog.setVisible(false);
        AdminLogsNavigacija.setVisible(true);
        AdminLogsNavigacija.setSize(408, 330);
        AdminLogsNavigacija.setLocationRelativeTo(null);
    }//GEN-LAST:event_AdminLietotajiDialogWindowClosing

    private void AdminSkatitDatusPogaMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_AdminSkatitDatusPogaMouseClicked
        AdminLogsNavigacija.setVisible(false);
        AdminLietotajiDialog.setVisible(true);
        AdminLietotajiDialog.setSize(458, 340);
        AdminLietotajiDialog.setLocationRelativeTo(null);
        javax.swing.table.DefaultTableModel tableModel = (javax.swing.table.DefaultTableModel) lietotajiTable.getModel();
        tableModel.setRowCount(0);
        try {
            java.util.List<model.User> users = new dao.UserDAO().getAllUsers();

            for (model.User user : users) {
                tableModel.addRow(new Object[]{
                    user.getUserId(),
                    user.getUsername(),
                    user.getRole()
                });
            }
        } catch (java.sql.SQLException e) {
            javax.swing.JOptionPane.showMessageDialog(
                    this,
                    "Neizdevās ielādēt lietotājus: " + e.getMessage()
            );
        }
    }//GEN-LAST:event_AdminSkatitDatusPogaMouseClicked

    private void AdminLogsMainitParoliMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_AdminLogsMainitParoliMouseClicked
        if (!util.Session.isLoggedIn()) {
            JOptionPane.showMessageDialog(this, "Kļūda!");
            return;
        }
        passwordFromAdmin = true;
        passwordFromRespondent = false;
        ParolesMainisanaPasreizejaParole.setText("");
        ParolesMainisanaAtkartotiParole.setText("");
        ParolesMainisanaJaunaParole.setText("");
        passwordFromRespondent = false;
        AdminLogsNavigacija.setVisible(false);
        ParolesMainisana.setVisible(true);
        ParolesMainisana.setSize(455, 295);
        ParolesMainisana.setLocationRelativeTo(null);
    }//GEN-LAST:event_AdminLogsMainitParoliMouseClicked

    private void AdminLogsNavigacijaWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_AdminLogsNavigacijaWindowClosing
        AdminLogsNavigacija.setVisible(false);
        LoginLogs.setVisible(true);
        LoginLogs.setSize(400, 320);
        LoginLogs.setLocationRelativeTo(null);
        LoginLogsParole.setText("");
        LoginLogsLietotajvards.setText("");
    }//GEN-LAST:event_AdminLogsNavigacijaWindowClosing

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(SurveySystem.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(SurveySystem.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(SurveySystem.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(SurveySystem.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new SurveySystem().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JDialog AdminLietotajiDialog;
    private javax.swing.JButton AdminLogsMainitParoli;
    private javax.swing.JDialog AdminLogsNavigacija;
    private javax.swing.JButton AdminSkatitDatusPoga;
    private javax.swing.JDialog AptaujasIzveleLogs;
    private javax.swing.JLabel AptaujasNos;
    private javax.swing.JDialog AptaujasRedigesana;
    private javax.swing.JTextField AptaujasRedigesana1AtbildeLogs;
    private javax.swing.JTextField AptaujasRedigesana2AtbildeLogs;
    private javax.swing.JTextField AptaujasRedigesana3AtbildeLogs;
    private javax.swing.JComboBox<String> AptaujasRedigesanaAptaujuIzvele;
    private javax.swing.JButton AptaujasRedigesanaIzietPoga;
    private javax.swing.JLabel AptaujasRedigesanaJautKartiba;
    private javax.swing.JTextField AptaujasRedigesanaJautLogs;
    private javax.swing.JLabel AptaujasRedigesanaJautSkaits;
    private javax.swing.JButton AptaujasRedigesanaNakamaisJaut;
    private javax.swing.JButton AptaujasRedigesanaPagajusaisJaut;
    private javax.swing.JButton AptaujasRedigesanaSaglabatPoga;
    private javax.swing.JDialog AptaujuIzveide;
    private javax.swing.JButton AptaujuIzveideDzestPoga;
    private javax.swing.JButton AptaujuIzveideIzveidotAptauju;
    private javax.swing.JTextField AptaujuIzveideJautajumsLogs;
    private javax.swing.JDialog AptaujuIzveideNosaukums;
    private javax.swing.JTextField AptaujuIzveideNosaukumsAptaujasNosaukums;
    private javax.swing.JButton AptaujuIzveideNosaukumsAtpakalPoga;
    private javax.swing.JButton AptaujuIzveideNosaukumsTalaklPoga;
    private javax.swing.JButton AptaujuIzveidePievienotJaut;
    private javax.swing.JDialog AtbilzuIevade;
    private javax.swing.JLabel AtbilzuIevade1Atbilde;
    private javax.swing.JCheckBox AtbilzuIevade1AtbildeBox;
    private javax.swing.JLabel AtbilzuIevade2Atbilde;
    private javax.swing.JCheckBox AtbilzuIevade2AtbildeBox;
    private javax.swing.JLabel AtbilzuIevade3Atbilde;
    private javax.swing.JCheckBox AtbilzuIevade3AtbildeBox;
    private javax.swing.JLabel AtbilzuIevadeJautajums;
    private javax.swing.JButton AtbilzuIevadeTalakPoga;
    private javax.swing.JButton GalvenaisLogsIeietPoga;
    private javax.swing.JButton GalvenaisLogsRegistretiesPoga;
    private javax.swing.JButton IntervetajsLogsApskatitiesRezultatusPoga;
    private javax.swing.JButton IntervetajsLogsMainitParoliPoga;
    private javax.swing.JButton IntervetajsLogsRedigetAptaujuPoga;
    private javax.swing.JButton IntervetajsLogsVeidotAptaujuPoga;
    private javax.swing.JDialog LietRegistracija;
    private javax.swing.JButton LietRegistracijaAtpakalPoga;
    private javax.swing.JButton LietRegistracijaHelpPoga;
    private javax.swing.JCheckBox LietRegistracijaIntervetajs;
    private javax.swing.JButton LietRegistracijaRegistretiesPoga;
    private javax.swing.JDialog LoginLogs;
    private javax.swing.JButton LoginLogsIeietPoga;
    private javax.swing.JTextField LoginLogsLietotajvards;
    private javax.swing.JTextField LoginLogsParole;
    private javax.swing.JDialog NavigacijasLogsIntervetajs;
    private javax.swing.JDialog NavigacijasLogsRespondent;
    private javax.swing.JDialog ParolesMainisana;
    private javax.swing.JTextField ParolesMainisanaAtkartotiParole;
    private javax.swing.JTextField ParolesMainisanaJaunaParole;
    private javax.swing.JButton ParolesMainisanaMainitParoliPoga;
    private javax.swing.JTextField ParolesMainisanaPasreizejaParole;
    private javax.swing.JButton RespondentsLogsMainitParoliPoga;
    private javax.swing.JButton RespondentsLogsPilditAptaujasPoga;
    private javax.swing.JDialog RezultatuApkAtt;
    private javax.swing.JButton RezultatuApkAttIeprieksejaisJautPoga;
    private javax.swing.JLabel RezultatuApkAttJautKartiba;
    private javax.swing.JLabel RezultatuApkAttJautSkaits;
    private javax.swing.JButton RezultatuApkAttNakamaisJautPoga;
    private javax.swing.JButton RezultatuApkAttParaditRezultatu;
    private javax.swing.JScrollPane RezultatuApkAttRezultati;
    private javax.swing.JButton RezultatuIzveleLogsApskatitPoga;
    private javax.swing.JComboBox<String> RezultatuIzveleLogsAptaujasRezultati;
    private javax.swing.JButton RezultatuIzveleLogsAtpakalPoga;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel30;
    private javax.swing.JLabel jLabel31;
    private javax.swing.JLabel jLabel32;
    private javax.swing.JLabel jLabel33;
    private javax.swing.JLabel jLabel34;
    private javax.swing.JLabel jLabel35;
    private javax.swing.JLabel jLabel36;
    private javax.swing.JLabel jLabel37;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JTextField jTextField3;
    private javax.swing.JTextField jTextField4;
    private javax.swing.JTextField jTextField5;
    private javax.swing.JTextField jTextField7;
    private javax.swing.JTextField jTextField8;
    private javax.swing.JTextField jTextField9;
    private javax.swing.JTable lietotajiTable;
    private java.awt.Panel panel1;
    // End of variables declaration//GEN-END:variables
}
