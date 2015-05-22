package org.moire.opensudoku.gui.importing;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URISyntaxException;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.content.ContentResolver;
import android.net.Uri;
import org.moire.opensudoku.R;
import org.moire.opensudoku.db.SudokuImportParams;
import org.moire.opensudoku.db.SudokuInvalidFormatException;
import org.moire.opensudoku.game.SudokuGame;

/**
 * Handles import of application/x-opensudoku or .opensudoku files.
 *
 * @author romario
 */
public class OpenSudokuImportTask extends AbstractImportTask {

	private Uri mUri;

	public OpenSudokuImportTask(Uri uri) {
		mUri = uri;
	}

	@Override
	protected void processImport() throws SudokuInvalidFormatException {
		try {
			InputStreamReader streamReader;
			if (mUri.getScheme().equals("content")) {
				ContentResolver contentResolver = mContext.getContentResolver();
				streamReader = new InputStreamReader(contentResolver.openInputStream(mUri));
			} else {
				java.net.URI juri;
				juri = new java.net.URI(mUri.getScheme(), mUri
						.getSchemeSpecificPart(), mUri.getFragment());
				streamReader = new InputStreamReader(juri.toURL().openStream());
			}

			try {
				importXml(streamReader);
			} finally {
				streamReader.close();
			}
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}

	private void importXml(Reader in) throws SudokuInvalidFormatException {
		BufferedReader inBR = new BufferedReader(in);
		/*
		 * while((s=in.readLine())!=null){ Log.i(tag, "line: "+s); }
		 */

		// parse xml
		XmlPullParserFactory factory;
		XmlPullParser xpp;
		try {
			factory = XmlPullParserFactory.newInstance();
			factory.setNamespaceAware(false);
			xpp = factory.newPullParser();
			xpp.setInput(inBR);
			int eventType = xpp.getEventType();
			String rootTag = "";
			while (eventType != XmlPullParser.END_DOCUMENT) {
				if (eventType == XmlPullParser.START_TAG) {
					rootTag = xpp.getName();
					if (rootTag.equals("opensudoku")) {
						String version = xpp.getAttributeValue(null, "version");
						if (version == null) {
							// no version provided, assume that it's version 1
							importV1(xpp);
						} else if (version.equals("2")) {
							importV2(xpp);
						} else {
							setError("Unknown version of data.");
						}
					} else {
						setError(mContext.getString(R.string.invalid_format));
						return;
					}
				}
				eventType = xpp.next();
			}
		} catch (XmlPullParserException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void importV2(XmlPullParser parser)
			throws XmlPullParserException, IOException, SudokuInvalidFormatException {
		int eventType = parser.getEventType();
		String lastTag = "";
		SudokuImportParams importParams = new SudokuImportParams();

		while (eventType != XmlPullParser.END_DOCUMENT) {
			if (eventType == XmlPullParser.START_TAG) {
				lastTag = parser.getName();
				if (lastTag.equals("folder")) {
					String name = parser.getAttributeValue(null, "name");
					long created = parseLong(parser.getAttributeValue(null, "created"), System.currentTimeMillis());
					importFolder(name, created);
				} else if (lastTag.equals("game")) {
					importParams.clear();
					importParams.created = parseLong(parser.getAttributeValue(null, "created"), System.currentTimeMillis());
					importParams.state = parseLong(parser.getAttributeValue(null, "state"), SudokuGame.GAME_STATE_NOT_STARTED);
					importParams.time = parseLong(parser.getAttributeValue(null, "time"), 0);
					importParams.lastPlayed = parseLong(parser.getAttributeValue(null, "last_played"), 0);
					importParams.data = parser.getAttributeValue(null, "data");
					importParams.note = parser.getAttributeValue(null, "note");

					importGame(importParams);
				}
			} else if (eventType == XmlPullParser.END_TAG) {
				lastTag = "";
			} else if (eventType == XmlPullParser.TEXT) {
				if (lastTag.equals("name")) {
				}

			}
			eventType = parser.next();
		}
	}

	private long parseLong(String string, long defaultValue) {
		return string != null ? Long.parseLong(string) : defaultValue;
	}

	private void importV1(XmlPullParser parser)
			throws XmlPullParserException, IOException, SudokuInvalidFormatException {
		int eventType = parser.getEventType();
		String lastTag = "";

		while (eventType != XmlPullParser.END_DOCUMENT) {
			if (eventType == XmlPullParser.START_TAG) {
				lastTag = parser.getName();
				if (lastTag.equals("game")) {
					importGame(parser.getAttributeValue(null, "data"));
				}
			} else if (eventType == XmlPullParser.END_TAG) {
				lastTag = "";
			} else if (eventType == XmlPullParser.TEXT) {
				if (lastTag.equals("name")) {
					importFolder(parser.getText());
				}

			}
			eventType = parser.next();
		}


	}

}
