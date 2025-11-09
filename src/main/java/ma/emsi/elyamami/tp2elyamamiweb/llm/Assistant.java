package ma.emsi.elyamami.tp2elyamamiweb.llm;

/**
 * Interface de service IA qui définit les interactions avec le LLM.
 * LangChain4j fournira l'implémentation.
 */
public interface Assistant {

    /**
     * Envoie un prompt utilisateur au LLM et retourne sa réponse.
     * @param prompt Le message de l'utilisateur.
     * @return La réponse textuelle de l'assistant IA.
     */
    String chat(String prompt);
}