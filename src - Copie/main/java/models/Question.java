package models;

import java.util.Arrays;

public class Question {
    private int id;
    private String question;
    private String[] options;
    private String reponseCorrecte;
    private int quizId; // Reste comme int pour correspondre à la DB
    private int certificationId;

    // Constructeurs
    public Question() {}

    public Question(int id, String question, String[] options, String reponseCorrecte, int quizId, int certificationId) {
        this.id = id;
        this.question = question;
        this.options = options != null ? options.clone() : null;
        this.reponseCorrecte = reponseCorrecte;
        this.quizId = quizId;
        this.certificationId = certificationId;
    }

    // Getters et Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }

    public String[] getOptions() { return options != null ? options.clone() : null; }
    public void setOptions(String[] options) { this.options = options != null ? options.clone() : null; }

    public String getReponseCorrecte() { return reponseCorrecte; }
    public void setReponseCorrecte(String reponseCorrecte) { this.reponseCorrecte = reponseCorrecte; }

    public int getQuizId() { return quizId; }
    public void setQuizId(int quizId) { this.quizId = quizId; }

    public int getCertificationId() { return certificationId; }
    public void setCertificationId(int certificationId) { this.certificationId = certificationId; }

    @Override
    public String toString() {
        return question + " (Options: " + Arrays.toString(options) + ")";
    }
}