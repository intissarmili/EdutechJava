package utils;
import jakarta.mail.Session;
import jakarta.mail.Authenticator;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.Properties;

public class EmailUtil {

    public static boolean sendEmail(String to, String subject, String content) {
        final String from = "ahmedboutrif0@gmail.com"; // ✅ Corrected email
        final String password = "rgipwupnbjlqpryd"; // ✅ App Password (no spaces)

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(from, password);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject);
            message.setText(content);

            Transport.send(message);

            System.out.println("✅ Email envoyé avec succès !");
            return true;
        } catch (MessagingException e) {
            System.err.println("❌ Erreur envoi email: " + e.getMessage());
            return false;
        }
    }
}
