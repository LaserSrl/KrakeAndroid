package com.krake.core;

/**
 * Created by joel on 23/07/14.
 */
public class Constants {

    /**
     * Input: String - autoroute path dell'oggetto da richiedere ad Orchard
     */
    static public final String REQUEST_DISPLAY_PATH_KEY = "displayAlias";

    /**
     * Input: String - indicazione ad orchard della lingua richiesta dall'App (formato it-IT)
     * Se non indicato viene preso il valore {@link com.krake.core.R.string#orchard_language}
     */
    static public final String REQUEST_LANGUAGE_KEY = "lang";
    /**
     * Input: intero - Indice della pagina da 1 ad N.
     * Se non indicato viene chiesta la pagina 1
     */
    static public final String REQUEST_PAGE_KEY = "page";

    /**
     * Input - int: Dimensione della pagina, di default presa dal {@link com.krake.core.R.integer#orchard_default_page_size}
     */
    static public final String REQUEST_PAGE_SIZE_KEY = "pageSize";

    /**
     * Input - int: Dimensione della pagina, di default presa dal {@link com.krake.core.R.integer#orchard_default_page_size}
     */
    static public final String REQUEST_SUB_TERMS = "SubTerms";

    /**
     * Input: String - parametro per indicare quali campi dei contenuti di orchard devono essere restituiti. I campi
     * devono essere separati da virgola ed indicare il nome del campo indicato in Orchard (case sensitive).
     * Questa opzione è utilizzabile solo caricando dati propri di Orchard tramite una projection.
     * In caso non sia indicato saranno restituiti tutti i campi degli oggetti.
     */
    static public final String REQUEST_ITEM_PART_FILTER = "mfilter";

    /**
     * Input: String - Parametro speciale utlizzato solo nell'accesso alle tassonomie.
     * Impostato al valore SubTerms vengono restituiti i sotto termini e non gli oggetti contenuti.
     */
    static public final String REQUEST_RESULT_TARGET = "resultTarget";

    /**
     * Input: float - latitudine (valido solo per le ricerche attorno ad un punto)
     */
    static public final String REQUEST_LATITUDE = "lat";

    /**
     * Input: float - longitudine (valido solo per le ricerche attorno ad un punto)
     */
    static public final String REQUEST_LONGITUDE = "lng";

    /**
     * Input: float - raggio in metri (valido solo per le ricerche attorno ad un punto)
     */
    static public final String REQUEST_RADIUS = "dist";

    /**
     * Input: int - Profondità massima dei livelli del JSON generatore da orchard.
     * Di default 10, un valore maggiore ritorna più dati ma richiede maggiore tempo di parsing/download.
     */
    static public final String REQUEST_DEEP_LEVEL = "deeplevel";

    static public final String REQUEST_SIGNAL_NAME = "Name";

    static public final String REQUEST_CONTENT_TYPE = "ContentType";

    /**
     * Input: valorizzato a true forza orchard a ritornare un cookie persistente ad una chiamata di login.
     * Default: non inviato.
     * Orchard per la login sua ritorna un cookie persistente
     * per external login: cookie non persistente
     */
    static public final String REQUEST_PERSISTENT_COOKIE = "createPersistentCookie";


    //OUTPUT
    static public final String RESPONSE_NAME_KEY = "n";
    static public final String RESPONSE_VALUE_KEY = "v";
    static public final String RESPONSE_MODEL_KEY = "m";
    static public final String RESPONSE_LIST_KEY = "l";
    static public final String RESPONSE_CONTENT_TYPE_KEY = "ContentType";

    //PUSH
    static public final String RESPONSE_PUSH_REFERENCE_DISPLAY_PATH = "Al";
    static public final String RESPONSE_PUSH_EXTERNAL_URL = "Eu";
    static public final String RESPONSE_PUSH_IDENTIFIER = "Id";
    static public final String RESPONSE_PUSH_TEXT = "Text";


    //Output_values
    static public final String CONTENT_TYPE_PROJECTION_PAGE = "ProjectionPage";
    static public final String CONTENT_TYPE_CONTENT_PART = "ContentPart";
    static public final String CONTENT_TYPE_WIDGET_LIST = "WidgetList";

    //Package
    static public final String OGL_SUB_PACKAGE = "model";

    public static final String REQUEST_REAL_FORMAT = "realFormat";

    public static final String REQUEST_COMPLEX_BEHAVIOUR = "complexbehaviour";

    //Cookies

    static public final String COOKIE_PRIVACY_ANSWERS = "PoliciesAnswers";
}
