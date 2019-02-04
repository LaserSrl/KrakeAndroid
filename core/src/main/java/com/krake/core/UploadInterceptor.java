package com.krake.core;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.krake.core.app.KrakeApplication;
import com.krake.core.media.MediaType;
import com.krake.core.media.UploadableMediaInfo;
import com.krake.core.network.RemoteClient;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Classe invocata per ogni lista di Uri abbinata ad una chiave durante l'upload di media.
 */
public abstract class UploadInterceptor {
    public static final int PRIORITY_LOW = 0;
    public static final int PRIORITY_MEDIUM = 1;
    public static final int PRIORITY_HIGH = 2;

    private boolean cancelled;
    private int priority;
    @MediaType
    private int availableMedias;

    /**
     * Inizializza un {@link UploadInterceptor} settando la sua priorità e i media supportati
     *
     * @param priority        priorità di questo interceptor
     * @param availableMedias tipi di media supportati da questa classe (in bitwise OR nel caso ci siano più tipi)
     */
    public UploadInterceptor(@Priority int priority, @MediaType int availableMedias) {
        this.priority = priority;
        this.availableMedias = availableMedias;
    }

    /**
     * Metodo che viene richiamato su ogni {@link UploadableMediaInfo} per specificare un upload custom.
     * <br/>
     * Questo processo è asincrono quindi non c'è bisogno di richiamare api aggiuntive per il dispatch su un altro thread.
     * <br/>
     * Per utilizzare un interceptor custom è necessario aggiungerlo nell'application con il metodo {@link KrakeApplication#addUploadInterceptor(UploadInterceptor)}
     * e specificare la sua logica nel metodo {@link KrakeApplication#getUploadInterceptor(UploadableMediaInfo, String, Bundle)}
     *
     * @param remoteClient      istanza da usare
     * @param baseServiceUrl   url di base sul quale richiamare le varie api
     * @param media            {@link UploadableMediaInfo} relativa al file da caricare
     * @param uploadParams     bundle opzionale con dei possibili parametri aggiuntivi utili per l'upload
     * @throws OrchardError se un errore è stato sollevato durante l'esecuzione del metodo
     */
    public abstract void uploadFile(@NonNull RemoteClient remoteClient,
                                    @NonNull Context context,
                                    @NonNull UploadableMediaInfo media,
                                    @Nullable Bundle uploadParams) throws OrchardError;

    /**
     * Metodo che fornisce la possibilità di cancellare un upload o una lista di upload.
     *
     * @return true se l'upload è stato effettivamente cancellato, false altrimenti
     */
    public abstract boolean cancelUpload();

    /**
     * Stabilisce se l'upload è stato cancellato o meno.
     *
     * @return true se è stato cancellato
     */
    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @MediaType
    public int getAvailableMedias() {
        return availableMedias;
    }

    @Priority
    public int getPriority() {
        return priority;
    }

    /**
     * Indica la priorità di questo interceptor sull'upload di qualsiasi tipo di media.
     * <br/>
     * Questo dato è utilizzato nel comportamento di default di {@link KrakeApplication#getUploadInterceptor(UploadableMediaInfo, String, Bundle)}
     * in cui gli interceptor sono ordinati per priorità. Può essere utile anche in caso di override
     */
    @IntDef({PRIORITY_LOW, PRIORITY_MEDIUM, PRIORITY_HIGH})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Priority {
    }
}