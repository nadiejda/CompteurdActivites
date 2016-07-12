package application;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map.Entry;

public class Activite {
	protected String nom;
	protected String imageAdresseFichier;
	protected long dureeDuJour; // en sec.
	protected int dureeMoyenne;
	protected HashMap<Jour,Integer> dureeParJour = new HashMap<Jour,Integer>();
	
	private static long chrono;
	private boolean estLanceChrono;

	public Activite(String nom, String image){
		this.nom = nom;
		this.imageAdresseFichier = image;
		this.dureeMoyenne = 0;
		this.dureeDuJour = 0;
		Calendar date = Calendar.getInstance(Locale.FRENCH);
		this.dureeParJour.put(new Jour(date), new Integer(0));
	}

	public Activite (String nom, String imageAdresseFichier, long dureeDuJour, int dureeMoyenne, HashMap<Jour,Integer> dureeParJour ) {
		this.nom = nom;
		this.imageAdresseFichier = imageAdresseFichier;
		this.dureeMoyenne = dureeMoyenne;
		this.dureeDuJour = dureeDuJour;
		this.dureeParJour = dureeParJour;
	}
	
	public Activite(String guestString, File file) {
		this.nom = guestString;
		this.imageAdresseFichier = file.getPath();
		this.dureeMoyenne = 0;
		this.dureeDuJour = 0;
		Calendar date = Calendar.getInstance(Locale.FRENCH);
		this.dureeParJour.put(new Jour(date), new Integer(0));
	}

	@Override
	public String toString() {
		return "Activite [nom=" + nom + ", imageAdresseFichier=" + imageAdresseFichier + ", dureeDuJour=" + dureeDuJour
				+ ", dureeMoyenne=" + dureeMoyenne + ", dureeParJour=" + dureeParJour.toString() + ", estLanceChrono="
				+ estLanceChrono + "]";
	}

	// Lancement du chrono
	void lancerChrono() {
		chrono = System.currentTimeMillis() ;
	}

	// Arret du chrono
	void stopperChrono() {
		long chrono2 = System.currentTimeMillis() ;
		dureeDuJour += (chrono2-chrono)/1000;
		Calendar date = Calendar.getInstance(Locale.FRENCH);
		this.dureeParJour.put(new Jour(date), (int) dureeDuJour);
		int somme = 0;
		Iterator<Integer> i = this.dureeParJour.values().iterator();
		while(i.hasNext()){
			somme+=(int)i.next();
		}
		dureeMoyenne = somme/this.dureeParJour.size();
	}

	public void compter() {
		if (estLanceChrono){
			stopperChrono();
		}
		else {
			lancerChrono();
		}
		estLanceChrono= !estLanceChrono;
		
	} 
}
