package service;

import interfaces.IService;
import models.Quiz;
import models.Question;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import utils.MaConnexion;

public class QuizService implements IService<Quiz> {
    private final Connection cnx;

    public QuizService() {
        cnx = MaConnexion.getInstance().getConnection();
    }
    public List<Quiz> findByCoursId(int coursId) throws SQLException {
        List<Quiz> quizzes = new ArrayList<>();
        String query = "SELECT * FROM quiz WHERE cours_id = ?";

        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setInt(1, coursId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Quiz quiz = mapResultSetToQuiz(rs);

                    // Charger questions associées
                    String questionsQuery = "SELECT question FROM question WHERE quiz_id = ?";
                    try (PreparedStatement qps = cnx.prepareStatement(questionsQuery)) {
                        qps.setInt(1, quiz.getId());
                        try (ResultSet qrs = qps.executeQuery()) {
                            StringBuilder questionsText = new StringBuilder();
                            while (qrs.next()) {
                                if (questionsText.length() > 0) questionsText.append(", ");
                                questionsText.append(qrs.getString("question"));
                            }
                            quiz.setQuestionsText(questionsText.toString());
                        }
                    }

                    quizzes.add(quiz);
                }
            }
        }

        return quizzes;
    }


    @Override
    public void create(Quiz quiz) throws SQLException {
        String query = "INSERT INTO quiz(note, reponse_utilisateur, prix_piece, cours_id) VALUES(?, ?, ?, ?)";
        try (PreparedStatement ps = cnx.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            ps.setObject(1, quiz.getNote(), Types.INTEGER);
            ps.setString(2, quiz.getReponseUtilisateur() != null ? String.join(",", quiz.getReponseUtilisateur()) : null);
            ps.setInt(3, quiz.getPrixPiece());
            ps.setInt(4, quiz.getCoursId());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    quiz.setId(rs.getInt(1));
                }
            }
        }
    }

    @Override
    public void update(Quiz quiz) throws SQLException {
        String query = "UPDATE quiz SET note = ?, reponse_utilisateur = ?, prix_piece = ?, cours_id = ? WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setObject(1, quiz.getNote(), Types.INTEGER);
            ps.setString(2, quiz.getReponseUtilisateur() != null ? String.join(",", quiz.getReponseUtilisateur()) : null);
            ps.setInt(3, quiz.getPrixPiece());
            ps.setInt(4, quiz.getCoursId());
            ps.setInt(5, quiz.getId());
            ps.executeUpdate();
        }
    }

    @Override
    public void delete(Quiz quiz) throws SQLException {
        String query = "DELETE FROM quiz WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setInt(1, quiz.getId());
            ps.executeUpdate();
        }
    }
    public List<Integer> getAllQuizIds() throws SQLException {
        List<Integer> ids = new ArrayList<>();
        String query = "SELECT id FROM quiz";
        try (PreparedStatement ps = cnx.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ids.add(rs.getInt("id"));
            }
        }
        return ids;
    }
    @Override
    public List<Quiz> readAll() throws SQLException {
        List<Quiz> quizzes = new ArrayList<>();

        // 1. D'abord récupérer les quizzes
        String quizQuery = "SELECT * FROM quiz";
        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(quizQuery)) {

            while (rs.next()) {
                Quiz quiz = mapResultSetToQuiz(rs);

                // 2. Ensuite récupérer les questions pour chaque quiz
                String questionsQuery = "SELECT question FROM question WHERE quiz_id = ?";
                try (PreparedStatement ps = cnx.prepareStatement(questionsQuery)) {
                    ps.setInt(1, quiz.getId());
                    try (ResultSet qRs = ps.executeQuery()) {
                        StringBuilder questionsText = new StringBuilder();
                        while (qRs.next()) {
                            if (questionsText.length() > 0) {
                                questionsText.append(", ");
                            }
                            questionsText.append(qRs.getString("question"));
                        }
                        quiz.setQuestionsText(questionsText.toString());
                    }
                }
                quizzes.add(quiz);
            }
        }
        return quizzes;
    }

    private Quiz mapResultSetToQuiz(ResultSet rs) throws SQLException {
        Quiz quiz = new Quiz(
                rs.getInt("id"),
                rs.getObject("note", Integer.class),
                rs.getInt("prix_piece"),
                rs.getInt("cours_id")
        );

        // Gestion des réponses utilisateur si nécessaire
        String reponses = rs.getString("reponse_utilisateur");
        if (reponses != null) {
            quiz.setReponseUtilisateur(reponses.split(","));
        }

        return quiz;
    }

    public List<Question> getQuestionsForQuiz(int quizId) throws SQLException {
        List<Question> questions = new ArrayList<>();
        String query = "SELECT * FROM question WHERE quiz_id = ?";

        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setInt(1, quizId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String optionsStr = rs.getString("options");
                    // On conserve le format brut pour la désérialisation dans Question
                    String[] options = optionsStr != null ? new String[]{optionsStr} : new String[0];

                    questions.add(new Question(
                            rs.getInt("id"),
                            rs.getString("question"),
                            options,
                            rs.getString("reponse_correcte"),
                            rs.getInt("quiz_id"),
                            rs.getInt("certification_id")
                    ));
                }
            }
        }
        return questions;
    }

}