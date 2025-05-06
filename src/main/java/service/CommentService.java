package service;

import models.Comment;

import utils.MaConnexion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CommentService {
    
    public void createComment(Comment comment) throws SQLException {
        String sql = "INSERT INTO commentaires (contenu, username, date_creation) VALUES (?, ?, ?)";
        
        try (Connection conn = MaConnexion.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, comment.getContent());
            pstmt.setString(2, comment.getUsername());
            pstmt.setTimestamp(3, Timestamp.valueOf(comment.getDate()));
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("La création du commentaire a échoué, aucune ligne n'a été affectée.");
            }
            
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    comment.setId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("La création du commentaire a échoué, aucun ID n'a été obtenu.");
                }
            }
        }
    }
    
    public List<Comment> getAllComments() throws SQLException {
        List<Comment> comments = new ArrayList<>();
        String sql = "SELECT * FROM commentaires ORDER BY date_creation DESC";
        
        try (Connection conn = MaConnexion.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Comment comment = new Comment();
                comment.setId(rs.getInt("id"));
                comment.setContent(rs.getString("contenu"));
                comment.setUsername(rs.getString("username"));
                comment.setDate(rs.getTimestamp("date_creation").toLocalDateTime());
                comments.add(comment);
            }
        }
        
        return comments;
    }
    
    public void deleteComment(int id) throws SQLException {
        String sql = "DELETE FROM commentaires WHERE id = ?";
        
        try (Connection conn = MaConnexion.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        }
    }
} 