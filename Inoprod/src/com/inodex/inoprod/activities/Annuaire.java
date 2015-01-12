package com.inodex.inoprod.activities;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.SimpleCursorAdapter;

import com.inodex.inoprod.R;
import com.inodex.inoprod.business.AnnuairePersonel.Employe;
import com.inodex.inoprod.business.AnnuaireProvider;

/**
 * Acitivit� qui affiche l'annuaire du personnel autoris�
 * 
 * @author Arnaud Payet
 * @version 1.1
 * 
 */
public class Annuaire extends Activity {

	/** Bouton qui permet de revenir au menu principal */
	private ImageButton boutonExit = null;
	
	/** Chaines de caract�res permettant de faire une requete dans la base de donn�es Annuaire */
	private String columns[] = new String[] { Employe._id, Employe.EMPLOYE_NOM,
			Employe.EMPLOYE_PRENOM, Employe.EMPLOYE_METIER };
	
	/** Layouts � utiliser */
	private int[] layouts = new int[] { R.id.idEmp, R.id.nom, R.id.prenom,
			R.id.metier };
	/** Uri de AnnuaireProvider */
	private Uri url = AnnuaireProvider.CONTENT_URI;
	/** Curseur et Content Resolver � utiliser lors des requ�tes */
	private Cursor cursor;
	private ContentResolver cr;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_annuaire);
		cr = getContentResolver();
		// Affichage de l'annuaire
		displayContentProvider();

		// Retour menu principal
		boutonExit = (ImageButton) findViewById(R.id.imageButton1);
		boutonExit.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent toMain = new Intent(Annuaire.this, MainActivity.class);
				startActivity(toMain);

			}
		});
	}

	/**
	 * Gen�re l'affichage de l'annuaire en utilisant un SimpleCursorAdapter Le
	 * layout GridView est r�cup�r� puis utiliser pour afficher chacun des
	 * �l�ments
	 */
	private void displayContentProvider() {
		// Cr�ation du SimpleCursorAdapter affili� au GridView
		SimpleCursorAdapter sca = new SimpleCursorAdapter(this,
				R.layout.grid_layout_annuaire, null, columns, layouts);
		GridView gridView = (GridView) findViewById(R.id.gridview);
		gridView.setAdapter(sca);
		// Requ�te dans la base Annuaire
		cursor = cr.query(url, columns, null, null, null);
		sca.changeCursor(cursor);

	}

}
