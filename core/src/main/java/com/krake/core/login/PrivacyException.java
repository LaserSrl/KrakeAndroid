package com.krake.core.login;

import com.krake.core.model.PolicyText;

import java.util.List;

/**
 * Eccezione inviata quando il caricamento del contenuto di Orchard è fallito
 * perché l'utente non ha accettato alcune privacy necessarie.
 */
public class PrivacyException extends Exception {
    private static final long serialVersionUID = -5755999431801284371L;

    private List<PolicyText> privacyTexts;

    public PrivacyException(List<PolicyText> privacyTexts) {
        super("", null);
        this.privacyTexts = privacyTexts;
    }

    public List<PolicyText> getPrivacyTexts() {
        return privacyTexts;
    }
}
