package org.moire.opensudoku.gui.importing;

import android.content.ContentResolver;
import android.net.Uri;

import org.moire.opensudoku.db.SudokuInvalidFormatException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Handles import of .sdm files (see http://sudocue.net/download.php).
 *
 * @author romario
 */
public class SdmImportTask extends AbstractImportTask {

    private Uri mUri;

    public SdmImportTask(Uri uri) {
        mUri = uri;
    }

    @Override
    protected void processImport() throws SudokuInvalidFormatException {
        importFolder(mUri.getLastPathSegment());
        InputStreamReader isr;

        try {
            if (mUri.getScheme().equals("content")) {
                ContentResolver contentResolver = mContext.getContentResolver();
                isr = new InputStreamReader(contentResolver.openInputStream(mUri));
            } else {
                URL url = new URL(mUri.toString());
                isr = new InputStreamReader(url.openStream());
            }
            try (BufferedReader br = new BufferedReader(isr)) {
                String s;
                while ((s = br.readLine()) != null) {
                    if (!s.equals("")) {
                        if (s.contains(".")) {
                            s = s.replace(".", "0");
                        }
                        importGame(s);
                    }
                }
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
