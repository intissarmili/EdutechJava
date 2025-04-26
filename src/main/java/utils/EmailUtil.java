package utils;

import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;

public class EmailUtil {
    public static boolean sendEmail(String to, String subject, String content) {
        final String from = "your.email@gmail.com"; // 👈 REMPLACE ICI
        final String password = "your-app-password"; // 👈 App Password depuis Gmail settings

        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true"); // TLS

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(from, password);
            }
        });

        try {
            Message msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(from));
            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            msg.setSubject(subject);
            msg.setText(content);

            Transport.send(msg);
            return true;
        } catch (MessagingException e) {
            System.err.println("Erreur envoi email: " + e.getMessage());
            return false;
        }
    }
}
