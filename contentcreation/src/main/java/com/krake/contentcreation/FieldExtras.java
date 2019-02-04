package com.krake.contentcreation;

/**
 * Classe che specifica chiavi/valori per gli extra che vengono passati nel costruttore di {@link ContentCreationTabInfo.FieldInfo}
 */
public final class FieldExtras {

    private FieldExtras() {
        // costruttore vuoto per evitare di istanziare questa classe
    }

    /**
     * Classe wrapper per chiavi/valori legati al field di tipo FIELD_TYPE_TEXT
     */
    public static final class Text {

        private Text() {
            // costruttore vuoto per evitare di istanziare questa classe
        }

        /**
         * Chiave per l'extra che definisce il massimo numero di linee per il testo
         * <br/>
         * Il tipo del valore deve essere <b>Integer</b>
         * <br/>
         * Il valore di default è 1 (il testo sarà su una sola linea)
         * <br/>
         * Per non definire nessun limite, utilizzare il valore {@link #MAX_LINES_NO_LIMIT}
         */
        public static final int KEY_MAX_LINES = 1;

        /**
         * Chiave per l'extra che definisce il tipo di input per il testo editabile.
         * <br/>
         * Il tipo del valore deve essere <b>Integer</b>
         * <br/>
         * Nel caso in cui si volessero assegnare più valori a questa chiave, bisogna inserirli in bitwise OR
         * <br/>
         * Il valore di default è InputType.TYPE_CLASS_TEXT
         */
        public static final int KEY_INPUT_TYPE = 2;

        /**
         * Valore per la chiave {@link #KEY_MAX_LINES} che definisce un numero illimitato di linee massime
         */
        public static final int MAX_LINES_NO_LIMIT = Integer.MAX_VALUE;
    }

    /**
     * Classe wrapper per chiavi/valori legati al field di tipo FIELD_TYPE_DATE
     */
    public static final class Date {

        private Date() {
            // costruttore vuoto per evitare di istanziare questa classe
        }

        /**
         * Chiave per l'extra che definisce la formattazione visiva (quella che viene passata ad Orchard non verrà modificata) della data.
         * <br/>
         * Come valore bisogna passare il pattern del DateFormat, non il DateFormat stesso
         * <br/>
         * Il tipo del valore deve essere <b>String</b>
         * <br/>
         * Il valore di default è hh/MM/yyyy se il valore di {@link #KEY_ENABLE_TIME} è false, altrimenti hh/MM/yyyy - HH:mm
         */
        public static final int KEY_DATE_FORMAT = 10;

        /**
         * Chiave per l'extra che definisce se con il DatePicker si potranno scegliere anche ore e minuti oltre alla data.
         * <br/>
         * Il tipo del valore deve essere <b>Boolean</b>
         * <br/>
         * Il valore di default è false
         */
        public static final int KEY_ENABLE_TIME = 11;
    }
}