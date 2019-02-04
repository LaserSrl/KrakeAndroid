package com.krake.core.media;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.webkit.MimeTypeMap;

import com.krake.core.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Provider per i vari tipi di File che vengono salvati nell'app.
 * <br>
 * I vari files verranno salvati in 4 cartelle differenti all'interno dell'app con i seguenti path:
 * <ul>
 * <li><b>../image/..</b>: usata per le immagini</li>
 * <li><b>../video/..</b>: usata per i video</li>
 * <li><b>../audio/..</b>: usata per i file audio</li>
 * <li><b>../temp/..</b>: usata per tutti i tipi di file creati temporaneamente solo per evitare problemi di sicurezza o Uri criptati</li>
 * <li><b>../image_manager_disk_cache/..</b>: usata per tutti i file cachati da Glide</li>
 * </ul>
 */
public class MediaProvider extends FileProvider {
    /**
     * Ottiene l'Uri di un File attraverso il FileProvider impostato sul package espresso nella stringa app_package
     *
     * @param context Context corrente
     * @param file    file sorgente da utilizzare per ottenere l'Uri
     * @return Uri del file passato come parametro
     */
    public static Uri getUriForFile(@NonNull Context context, @NonNull File file) {
        return getUriForFile(context, context.getString(R.string.app_package), file);
    }

    /**
     * Crea un File di tipo Media nella cartella di dati interna all'applicazione
     *
     * @param context Context corrente
     * @param type    tipo di media da creare
     * @return File del media con nome TYPE_PREFIX_timeStamp.extension, creato nella relativa cartella stabilita dal FileProvider
     */
    @SuppressLint({"SwitchIntDef", "SimpleDateFormat"})
    public static File createMediaFile(@NonNull Context context, @MediaType int type) {
        String childPath;
        switch (type) {
            case MediaType.AUDIO:
                childPath = "audio";
                break;
            case MediaType.VIDEO:
                childPath = "video";
                break;
            default:
                childPath = "image";
                break;
        }
        File path = new File(context.getFilesDir(), childPath);
        if (!path.exists()) {
            //noinspection ResultOfMethodCallIgnored
            path.mkdirs();
        }

        String fileName;
        final String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        if (type == MediaType.IMAGE)
            fileName = "IMG_" + timeStamp + ".jpg";
        else if (type == MediaType.VIDEO)
            fileName = "VID_" + timeStamp + ".mp4";
        else
            fileName = "AUD_" + timeStamp + ".aac";

        return new File(path, fileName);
    }

    /**
     * Crea un File temporaneo a partire da un Uri.
     * <br>
     * Questo metodo è necessario per evitare Uri temporanei ritornati da app come Google Photo, Google Camera, ecc.. , per evitare Uri criptati
     * e per superare i problemi di sicurezza relativi all'utilizzo di un Uri legato ad un File il cui permesso di lettura è solamente temporaneo.
     *
     * @param context  Context corrente
     * @param mediaUri Uri del media
     * @return File temporaneo creato nella cartella ../temp/..
     */
    public static File createTempFile(@NonNull Context context, @NonNull Uri mediaUri) {
        final ContentResolver contentResolver = context.getContentResolver();

        String lastSegment = mediaUri.getLastPathSegment();
        // usato per rimpiazzare tutti i caratteri speciali (punto escluso) della parte finale dell'uri
        lastSegment = lastSegment.replaceAll("^\\.\\w", "");

        // cerca di risolvere il mimeType del file
        final String mimeType = contentResolver.getType(mediaUri);
        String extension = null;
        if (mimeType != null) {
            // ottiene l'istanza singleton per mappare mimeType con estensione
            final MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
            // ottiene l'estensione relativa al mimeType
            extension = mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(mediaUri));
        }

        // posizione esclusiva dell'ultimo punto
        final int dotLastIndexExclusive = lastSegment.lastIndexOf('.') + 1;
        // aggiunge l'estensione nel caso in cui non ci sia il punto oppure l'estensione dell'ultimo segmento del path sia differente da quella da aggiungere
        if (extension != null && (dotLastIndexExclusive == -1 || (lastSegment.length() - 1 >= dotLastIndexExclusive && !lastSegment.substring(dotLastIndexExclusive).equals(extension)))) {
            // aggiunge il punto e l'estensione
            lastSegment += '.' + extension;
        }

        File tempPath = new File(context.getFilesDir(), "temp");
        if (!tempPath.exists()) {
            //noinspection ResultOfMethodCallIgnored
            tempPath.mkdirs();
        }

        File tempFile = new File(tempPath, lastSegment);
        if (!tempFile.exists()) {
            InputStream inputStream = null;
            try {
                // apre l'InputStream dal ContentResolver
                inputStream = context.getContentResolver().openInputStream(mediaUri);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            if (inputStream != null) {
                // copia l'InputStream in un File
                OutputStream out = null;
                try {
                    out = new FileOutputStream(tempFile);
                    byte[] buf = new byte[1024];
                    int len;
                    while ((len = inputStream.read(buf)) > 0) {
                        out.write(buf, 0, len);
                    }
                    out.flush();
                    ((FileOutputStream) out).getFD().sync();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (out != null)
                            out.close();
                        inputStream.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

        }
        return tempFile;
    }

    /**
     * Associa il tipo del Media al relativo MimeType
     *
     * @param mediaType tipo del media
     * @return Stringa che esprime il MimeType del media
     */
    @SuppressLint("SwitchIntDef")
    public static String mimeTypeForMedia(@MediaType int mediaType) {
        String mimeType;
        switch (mediaType) {
            case MediaType.AUDIO:
                mimeType = "audio";
                break;
            case MediaType.VIDEO:
                mimeType = "video";
                break;
            default:
                mimeType = "image";
                break;
        }
        return mimeType + "/*";
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public String getType(Uri uri) {
        String type = super.getType(uri);
        Context context = getContext();
        // nel caso in cui il file sia un file generico presente nella cartella di cache di Glide, viene cambiato il suo mime-type con image/*
        if (type.equals("application/octet-stream") && context != null && uri.getAuthority().equals(context.getString(com.krake.core.R.string.app_package)) && uri.getEncodedPath().startsWith("/share/")) {
            type = "image/*";
        }
        return type;
    }
}