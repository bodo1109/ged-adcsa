package cm.adcsa.auth.enums;

public enum StatutUtilisateur {
    ACTIF,
    INACTIF,
    BLOQUE;

    public boolean isActif() {
        return this == ACTIF;
    }
} 