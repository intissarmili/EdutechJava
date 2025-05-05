package service;

import service.IServicee;
import models.Question;
import utils.MaConnexion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class QuestionService implements IServicee<Question> {

    private Connection cnx;

    public QuestionService() {
        this.cnx = MaConnexion.getInstance().getConnection();
    }

    @Override
    public void create(Question question) throws SQLException {
        String query = "INSERT INTO question(question, options, reponse_correcte, quiz_id, certification_id) " +
                "VALUES(?, ?, ?, ?, ?)";

        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setString(1, question.getQuestion());
            ps.setString(2, serializeOptions(question.getOptions())); // Sérialisation PHP
            ps.setString(3, question.getReponseCorrecte());
            ps.setInt(4, question.getQuizId());
            ps.setInt(5, question.getCertificationId());
            ps.executeUpdate();
        }
    }

    @Override
    public void update(Question question) throws SQLException {
        String query = "UPDATE question SET question = ?, options = ?, reponse_correcte = ?, " +
                "quiz_id = ?, certification_id = ? WHERE id = ?";

        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setString(1, question.getQuestion());
            ps.setString(2, serializeOptions(question.getOptions()));
            ps.setString(3, question.getReponseCorrecte());
            ps.setInt(4, question.getQuizId());
            ps.setInt(5, question.getCertificationId());
            ps.setInt(6, question.getId());
            ps.executeUpdate();
        }
    }

    @Override
    public void delete(Question question) throws SQLException {
        String query = "DELETE FROM question WHERE id = ?";

        try (PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setInt(1, question.getId());
            ps.executeUpdate();
        }
    }

    @Override
    public List<Question> readAll() throws SQLException {
        List<Question> questions = new ArrayList<>();
        String query = "SELECT id, quiz_id, certification_id, question, options, reponse_correcte FROM question";

        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(query)) {

            while (rs.next()) {
                Question q = new Question();
                q.setId(rs.getInt("id"));
                q.setQuizId(rs.getInt("quiz_id"));
                q.setCertificationId(rs.getInt("certification_id"));
                q.setQuestion(rs.getString("question"));
                q.setOptions(parseOptions(rs.getString("options"))); // Désérialisation PHP
                q.setReponseCorrecte(rs.getString("reponse_correcte"));
                questions.add(q);
            }
        }
        return questions;
    }

    // Sérialisation au format PHP (a:3:{i:0;s:5:"value1";...})
    private String serializeOptions(String[] options) {
        if (options == null) return "a:0:{}";

        StringBuilder sb = new StringBuilder("a:");
        sb.append(options.length).append(":{");

        for (int i = 0; i < options.length; i++) {
            sb.append("i:").append(i).append(";s:")
                    .append(options[i].length()).append(":\"")
                    .append(options[i]).append("\";");
        }
        sb.append("}");
        return sb.toString();
    }

    // Désérialisation du format PHP
    private String[] parseOptions(String serialized) {
        if (serialized == null || !serialized.startsWith("a:")) {
            return new String[0];
        }

        try {
            // Extraction des valeurs entre guillemets
            String[] parts = serialized.split("\"");
            List<String> options = new ArrayList<>();

            for (int i = 1; i < parts.length; i += 2) {
                if (!parts[i].contains(";") && !parts[i].isEmpty()) {
                    options.add(parts[i]);
                }
            }
            return options.toArray(new String[0]);
        } catch (Exception e) {
            System.err.println("Erreur de parsing des options: " + serialized);
            return new String[0];
        }
    }
}