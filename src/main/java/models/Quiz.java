package models;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class Quiz {
    private int id;
    private Integer note;
    private String[] reponseUtilisateur;
    private Integer prixPiece;
    private Integer coursId;
    private List<Question> questions = new ArrayList<>();
    private String questionsText; // Pour stocker le texte concaténé des questions

    // Constructeurs
    public Quiz() {
        this.prixPiece = 0;
        this.coursId = 0;
    }

    public Quiz(Integer prixPiece, Integer coursId) {
        setPrixPiece(prixPiece);
        setCoursId(coursId);
    }

    public Quiz(int id, Integer note, Integer prixPiece, Integer coursId) {
        this.id = id;
        setNote(note);
        setPrixPiece(prixPiece);
        setCoursId(coursId);
    }

    // Gestion des questions
    public List<Question> getQuestions() {
        return new ArrayList<>(questions);
    }

    public void setQuestions(List<Question> questions) {
        this.questions = questions != null ? new ArrayList<>(questions) : new ArrayList<>();
    }

    public String getQuestionsText() {
        return questionsText;
    }

    public void setQuestionsText(String questionsText) {
        this.questionsText = questionsText;
    }

    // Getters/Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public Integer getNote() { return note; }
    public void setNote(Integer note) { this.note = note; }
    public String[] getReponseUtilisateur() { return reponseUtilisateur != null ? reponseUtilisateur.clone() : null; }
    public void setReponseUtilisateur(String[] reponseUtilisateur) { this.reponseUtilisateur = reponseUtilisateur != null ? reponseUtilisateur.clone() : null; }
    public Integer getPrixPiece() { return prixPiece; }
    public void setPrixPiece(Integer prixPiece) {
        if (prixPiece == null) throw new IllegalArgumentException("Prix pièce requis");
        if (prixPiece < 0 || prixPiece > 700) throw new IllegalArgumentException("Prix entre 0 et 700");
        this.prixPiece = prixPiece;
    }
    public Integer getCoursId() { return coursId; }
    public void setCoursId(Integer coursId) {
        if (coursId == null) throw new IllegalArgumentException("ID cours requis");
        if (coursId <= 0) throw new IllegalArgumentException("ID cours doit être positif");
        this.coursId = coursId;
    }

    // Méthodes métier
    public String getQuestionsConcatenated() {
        if (questionsText != null) return questionsText;
        return questions.stream().map(Question::getQuestion).collect(Collectors.joining(", "));
    }

    public int calculerNote() {
        if (reponseUtilisateur == null || questions.isEmpty()) return 0;
        int score = 0;
        for (int i = 0; i < Math.min(questions.size(), reponseUtilisateur.length); i++) {
            Question q = questions.get(i);
            if (q != null && reponseUtilisateur[i] != null && reponseUtilisateur[i].equals(q.getReponseCorrecte())) {
                score++;
            }
        }
        return score;
    }

    public boolean estReussi() {
        return getNote() != null && getNote() > (getNoteMaximale() / 2);
    }

    public int getNoteMaximale() {
        return questions.size();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Quiz quiz = (Quiz) o;
        return id == quiz.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("Quiz{id=%d, note=%d/%d}", id, note != null ? note : 0, getNoteMaximale());
    }
}