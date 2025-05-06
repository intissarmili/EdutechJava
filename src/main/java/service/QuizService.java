package service;

import service.IServicee;
import models.Quiz;
import models.Question;
import utils.MaConnexion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class QuizService implements IServicee<Quiz> {
    private final Connection cnx;

    public QuizService() {
        cnx = MaConnexion.getInstance().getConnection();
    }

    @Override
    public void create(Quiz quiz) {
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
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void update(Quiz quiz) {
        String query = "UPDATE quiz SET note = ?, reponse_utilisateur = ?, prix_piece = ?, cours_id = ? WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setObject(1, quiz.getNote(), Types.INTEGER);
            ps.setString(2, quiz.getReponseUtilisateur() != null ? String.join(",", quiz.getReponseUtilisateur()) : null);
            ps.setInt(3, quiz.getPrixPiece());
            ps.setInt(4, quiz.getCoursId());
            ps.setInt(5, quiz.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void delete(Quiz quiz) {
        String query = "DELETE FROM quiz WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setInt(1, quiz.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Quiz> readAll() {
        List<Quiz> quizzes = new ArrayList<>();
        String query = "SELECT * FROM quiz";

        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(query)) {

            while (rs.next()) {
                Quiz quiz = mapResultSetToQuiz(rs);

                // Charger les questions associées
                String questionsQuery = "SELECT question FROM question WHERE quiz_id = ?";
                try (PreparedStatement ps = cnx.prepareStatement(questionsQuery)) {
                    ps.setInt(1, quiz.getId());
                    try (ResultSet qrs = ps.executeQuery()) {
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

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return quizzes;
    }

    public List<Integer> getAllQuizIds() {
        List<Integer> ids = new ArrayList<>();
        String query = "SELECT id FROM quiz";

        try (PreparedStatement ps = cnx.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ids.add(rs.getInt("id"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return ids;
    }

    public List<Question> getQuestionsForQuiz(int quizId) {
        List<Question> questions = new ArrayList<>();
        String query = "SELECT * FROM question WHERE quiz_id = ?";

        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setInt(1, quizId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String optionsStr = rs.getString("options");
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
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return questions;
    }

    public List<Quiz> findByCoursId(int coursId) {
        List<Quiz> quizzes = new ArrayList<>();
        String query = "SELECT * FROM quiz WHERE cours_id = ?";

        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setInt(1, coursId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Quiz quiz = mapResultSetToQuiz(rs);

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
        } catch (SQLException e) {
            e.printStackTrace();
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

        String reponses = rs.getString("reponse_utilisateur");
        if (reponses != null) {
            quiz.setReponseUtilisateur(reponses.split(","));
        }

        return quiz;
    }
}
