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
	protected long dureeMoyenne;
	protected HashMap<Jour,Long> dureeParJour = new HashMap<Jour,Long>();
	
	private static long chrono;
	protected boolean estLanceChrono;
	protected long chronoTemp;

	public Activite(String nom, String image){
		this.nom = nom;
		this.imageAdresseFichier = image;
		this.dureeMoyenne = 0;
		this.dureeDuJour = 0;
		Calendar date = Calendar.getInstance(Locale.FRENCH);
		this.dureeParJour.put(new Jour(date), new Long(0));
		this.chronoTemp = 0;
		this.estLanceChrono = false;
	}

	public Activite (String nom, String imageAdresseFichier, long dureeDuJour, long dureeMoyenne, HashMap<Jour,Long> dureeParJour ) {
		this.nom = nom;
		this.imageAdresseFichier = imageAdresseFichier;
		this.dureeMoyenne = dureeMoyenne;
		this.dureeDuJour = dureeDuJour;
		this.dureeParJour = dureeParJour;
		this.chronoTemp = 0;
		this.estLanceChrono = false;
	}
	
	public Activite(String guestString, File file) {
		this.nom = guestString;
		this.imageAdresseFichier = file.getPath();
		this.dureeMoyenne = 0;
		this.dureeDuJour = 0;
		Calendar date = Calendar.getInstance(Locale.FRENCH);
		this.dureeParJour.put(new Jour(date), new Long(0));
		this.chronoTemp=0;
		this.estLanceChrono = false;
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
		estLanceChrono=true;
	}

	// Arret du chrono
	void stopperChronoEtSauvegardeDonnees() {
		if (estLanceChrono){
			chronoTemp=0;
			long chrono2 = System.currentTimeMillis() ;
			dureeDuJour += (chrono2-chrono)/1000;
			System.out.println("durée du jour "+ dureeDuJour);
			Calendar date = Calendar.getInstance(Locale.FRENCH);
			enregistrerDonneesDuJour(new Jour(date));
			majMoyenne();
			estLanceChrono= false;
		}
		
	}

	public void compter() {
		if (estLanceChrono){
			stopperChronoEtSauvegardeDonnees();
		}
		else {
			lancerChrono();
		}
		
	}

	public void enregistrerDonneesDuJour(Jour jour) {
		this.dureeParJour.put(jour, this.dureeDuJour);		
	}

	public void setDureeDuJour(long i) {
		this.dureeDuJour = i;
		
	}

	public void lireDonneesDuJour() {
		Calendar date = Calendar.getInstance(Locale.FRENCH);
		if (dureeParJour.containsKey(new Jour(date))){
			this.setDureeDuJour(dureeParJour.get(new Jour(date)));
		}
		
	}

	public void majDureesDuJourParChrono() {
		if (estLanceChrono){
			long chrono2=System.currentTimeMillis();
			chronoTemp=(chrono2-chrono)/1000;
			Calendar date = Calendar.getInstance(Locale.FRENCH);
			this.dureeParJour.put(new Jour(date), ( dureeDuJour+chronoTemp));
			majMoyenne();
		}
		
	}

	public void majMoyenne() {
		long somme = 0;
		Iterator<Long> i = this.dureeParJour.values().iterator();
		while(i.hasNext()){
			somme+=i.next();
		}
		dureeMoyenne = somme/this.dureeParJour.size();
		
	} 
}
