package services;

import com.stripe.Stripe;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;

public class StripeService {

    // Constructeur : Initialisation de la clé API Stripe
    public StripeService() {
        // Remplacez par votre clé API secrète
        Stripe.apiKey = "sk_test_51QwLR6H736elQqW1UejZ1kdX7huaeHRYpxzQF5GKDxiTBEPr8xbzAFTNjSWv8S2P1ZtZwa2yrxsVuNnNZ85WkojD00TZGpXaWo";
    }

    // Méthode pour créer la session de paiement
    public String createCheckoutSession(long amount, String currency) throws Exception {
        try {
            // Création des paramètres de la session de paiement
            SessionCreateParams params = SessionCreateParams.builder()
                    .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD) // Mode de paiement : carte
                    .addLineItem(
                            SessionCreateParams.LineItem.builder()
                                    .setPriceData(
                                            SessionCreateParams.LineItem.PriceData.builder()
                                                    .setCurrency(currency) // Devise (par exemple "usd")
                                                    .setProductData(
                                                            SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                    .setName("Abonnement") // Nom du produit
                                                                    .build()
                                                    )
                                                    .setUnitAmount(amount) // Montant en cents (par exemple 2000 pour 20.00 USD)
                                                    .build()
                                    )
                                    .setQuantity(1L) // Quantité de l'article
                                    .build()
                    )
                    .setMode(SessionCreateParams.Mode.PAYMENT) // Mode : paiement
                    .setSuccessUrl("https://Stripe.com/success") // URL après succès du paiement
                    .setCancelUrl("https://Stripe.com/cancel") // URL après annulation du paiement
                    .build();

            // Crée la session de paiement
            Session session = Session.create(params);
            // Retourne l'URL de paiement Stripe
            return session.getUrl();
        } catch (Exception e) {
            // Gestion des erreurs
            e.printStackTrace();
            throw new Exception("Erreur lors de la création de la session de paiement Stripe : " + e.getMessage());
        }
    }
}
