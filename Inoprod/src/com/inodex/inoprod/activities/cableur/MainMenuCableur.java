package com.inodex.inoprod.activities.cableur;

import android.app.Activity;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import com.inodex.inoprod.R;
import com.inodex.inoprod.business.SequencementProvider;
import com.inodex.inoprod.business.TableSequencement.Operation;

/**
 * Menu principal du profil opérateur. L'ordre du jour y est affiché.
 * 
 * @author Arnaud Payet
 * @version 1.1
 * 
 */
public class MainMenuCableur extends Activity {

	/** Nom de l'opérateur */
	private String nomPrenomOperateur[] = null;

	/** Uri de la table de sequencement */
	private Uri url = SequencementProvider.CONTENT_URI;
	/** Curseur et Content Resolver à utiliser lors des requêtes */
	private Cursor cursor;
	private ContentResolver cr;

	/** Clause à utiliser lors des requêtes */
	private String clause;

	/** Colonnes utilisés pour les requêtes */
	private String columns[] = { Operation._id, Operation.RANG_1_1,
			Operation.GAMME };
	private int layouts[] = { R.id.ordreOperations, R.id.operationsRealiser };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_menu_cableur);
	}

}
