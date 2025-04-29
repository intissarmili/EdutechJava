package models;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Question {
    private int id;
    private String question;
    private String[] options;
    private String reponseCorrecte;
    private int quizId;
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

    // Nouvelle méthode pour désérialiser les options
    public List<String> getDeserializedOptions() {
        if (options == null || options.length == 0) {
            return List.of();
        }

        // Cas 1 : Options sérialisées Symfony (ex: "a:3;f:0;s:10:\"nharek zin\";...")
        if (options[0].startsWith("a:") && options[0].contains(";")) {
            return Arrays.stream(options[0].split(";"))
                    .filter(part -> part.contains(":\""))
                    .map(part -> {
                        int start = part.indexOf("\"") + 1;
                        int end = part.lastIndexOf("\"");
                        return part.substring(start, end);
                    })
                    .collect(Collectors.toList());
        }

        // Cas 2 : Options normales (déjà désérialisées ou simples)
        return Arrays.asList(options);
    }

    @Override
    public String toString() {
        return question + " (Options: " + Arrays.toString(options) + ")";
    }
}