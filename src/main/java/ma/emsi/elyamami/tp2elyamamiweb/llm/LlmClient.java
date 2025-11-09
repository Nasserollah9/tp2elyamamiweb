package ma.emsi.elyamami.tp2elyamamiweb.llm;

import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.enterprise.context.Dependent;

import java.io.Serializable;

/**
 * Gère l'interface avec l'API de Gemini en utilisant LangChain4j.
 * Cette classe abstrait la complexité de la gestion de la mémoire
 * et des appels au modèle via un "Service IA" (l'interface Assistant).
 *
 * @Dependent garantit que l'état (la mémoire de chat) est lié
 * au cycle de vie du bean qui l'injecte (par ex. un ViewScoped bean).
 */
@Dependent
public class LlmClient implements Serializable {

    // --- Variables d'instance ---

    /**
     * Le rôle système actuel (par ex. "Tu es un expert en Java").
     * Cette variable est facultative mais demandée par les instructions.
     */
    private String systemRole;

    /**
     * L'instance du service IA (générée par LangChain4j)
     * qui gère les appels au LLM.
     */
    private final Assistant assistant;

    /**
     * La mémoire de la conversation, qui stocke l'historique des messages.
     */
    private final ChatMemory chatMemory;

    /**
     * Constructeur du client LLM.
     * Initialise le modèle, la mémoire et le service assistant.
     */
    public LlmClient() {
        // 1. Récupérer la clé API
        String geminiKey = System.getenv("GEMINI_KEY");
        if (geminiKey == null || geminiKey.isEmpty()) {
            throw new RuntimeException("Erreur: La variable d'environnement GEMINI_KEY n'est pas définie.");
        }

        // 2. Créer l'instance du ChatModel (Gemini)
        ChatModel model = GoogleAiGeminiChatModel.builder()
                .apiKey(geminiKey)
                .modelName("gemini-2.5-flash") // Note: "gemini-2.5-flash" de l'énoncé n'existe pas, j'utilise le nom correct.
                .build();

        // 3. Créer la mémoire de conversation (10 derniers messages)
        this.chatMemory = MessageWindowChatMemory.withMaxMessages(10);

        // 4. Créer l'assistant (le "service IA")
        this.assistant = AiServices.builder(Assistant.class)
                .chatModel(model)
                .chatMemory(this.chatMemory) // Lie la mémoire à l'assistant
                .build();
    }

    // --- Méthodes publiques ---

    /**
     * Définit le rôle système pour l'assistant IA.
     * Cela efface l'historique de la conversation précédente pour
     * commencer une nouvelle conversation avec le nouveau contexte.
     *
     * @param systemRole Le message système (ex: "Tu es un poète").
     */
    public void setSystemRole(String systemRole) {
        this.systemRole = systemRole; // Stocke le rôle (tel que demandé)

        // Vider la mémoire car le contexte change
        this.chatMemory.clear();

        // Ajouter le nouveau rôle système au début de la mémoire
        this.chatMemory.add(SystemMessage.from(systemRole));
    }

    /**
     * Envoie une requête (prompt) au LLM et retourne sa réponse.
     * Utilise l'instance 'assistant' de LangChain4j.
     *
     * @param prompt Le prompt de l'utilisateur.
     * @return La réponse générée par le LLM.
     */
    public String envoyerPrompt(String prompt) {
        // LangChain4j s'occupe de tout :
        // 1. Il ajoute le prompt utilisateur à la 'chatMemory'.
        // 2. Il envoie l'intégralité de la mémoire (SystemMessage + messages précédents) au LLM.
        // 3. Il reçoit la réponse, l'ajoute à la mémoire.
        // 4. Il retourne la réponse.
        return this.assistant.chat(prompt);
    }

    // Pas besoin de méthode close(), LangChain4j gère le cycle de vie.
}