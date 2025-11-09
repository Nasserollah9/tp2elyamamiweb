package ma.emsi.elyamami.tp2elyamamiweb.jsf;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.model.SelectItem;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
// Importe le client LLM
import ma.emsi.elyamami.tp2elyamamiweb.llm.LlmClient;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Named
@ViewScoped
public class Bb implements Serializable {

    // Injecte notre nouvelle classe LlmClient
    @Inject
    private LlmClient llmClient;

    // (Tous vos autres champs : roleSysteme, roleSystemeChangeable, etc.)
    private String roleSysteme;
    private boolean roleSystemeChangeable = true;
    private List<SelectItem> listeRolesSysteme;
    private String question;
    private String reponse;
    private StringBuilder conversation = new StringBuilder();

    @Inject
    private FacesContext facesContext;

    // (Constructeur vide, tous les getters/setters restent identiques)

    public Bb() {}

    // ... Getters et Setters pour tous les champs ...
    // (Copiez-les depuis votre code)
    public String getRoleSysteme() { return roleSysteme; }
    public void setRoleSysteme(String roleSysteme) { this.roleSysteme = roleSysteme; }
    public boolean isRoleSystemeChangeable() { return roleSystemeChangeable; }
    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }
    public String getReponse() { return reponse; }
    public void setReponse(String reponse) { this.reponse = reponse; }
    public String getConversation() { return conversation.toString(); }
    public void setConversation(String conversation) { this.conversation = new StringBuilder(conversation); }


    /**
     * Envoie la question au LLM via le LlmClient.
     */
    public String envoyer() {
        if (question == null || question.isBlank()) {
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Texte question vide", "Il manque le texte de la question");
            facesContext.addMessage(null, message);
            return null;
        }

        try {
            // Si c'est le début de la conversation...
            if (this.conversation.isEmpty()) {
                // 1. On dit au LlmClient quel rôle utiliser
                llmClient.setSystemRole(roleSysteme);
                // 2. On bloque la liste déroulante
                this.roleSystemeChangeable = false;
            }

            // 3. On envoie le prompt et on reçoit la réponse !
            this.reponse = llmClient.envoyerPrompt(question);

        } catch (Exception e) {
            // En cas d'erreur de l'API
            this.reponse = "ERREUR : " + e.getMessage();
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_FATAL,
                    "Erreur de l'API", "La connexion au LLM a échoué: " + e.getMessage());
            facesContext.addMessage(null, message);
        }

        // On affiche dans l'historique
        afficherConversation();
        return null;
    }

    /**
     * Pour un nouveau chat. (Inchangé)
     */
    public String nouveauChat() {
        return "index";
    }

    /**
     * Affiche la conversation (j'ai renommé "Serveur" en "Assistant")
     */
    private void afficherConversation() {
        this.conversation.append("== User:\n").append(question).append("\n== Assistant:\n").append(reponse).append("\n");
    }

    /**
     * Retourne les rôles système. (Inchangé)
     */
    public List<SelectItem> getRolesSysteme() {
        if (this.listeRolesSysteme == null) {
            this.listeRolesSysteme = new ArrayList<>();
            String role = """
                    You are a helpful assistant. You help the user to find the information they need.
                    If the user type a question, you answer it.
                    """;
            this.listeRolesSysteme.add(new SelectItem(role, "Assistant"));

            role = """
                    You are an interpreter. You translate from English to French and from French to English.
                    ...
                    """;
            this.listeRolesSysteme.add(new SelectItem(role, "Traducteur Anglais-Français"));

            role = """
                    Your are a travel guide. ...
                    """;
            this.listeRolesSysteme.add(new SelectItem(role, "Guide touristique"));
        }
        return this.listeRolesSysteme;
    }
}