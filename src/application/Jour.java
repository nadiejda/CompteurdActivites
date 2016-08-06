package application;

import java.time.LocalDate;
import java.util.Calendar;

public class Jour {
	protected int jour;
	protected int mois;
	protected int annee;
	
	public Jour(Calendar date){
		jour = date.get(Calendar.DAY_OF_MONTH);
		mois = date.get(Calendar.MONTH);
		annee = date.get(Calendar.YEAR);
	}

	public Jour(int jour2, int mois2, int annee2) {
		jour = jour2;
		mois = mois2;
		annee = annee2;
	}

	public Jour(LocalDate value) {
		jour = value.getDayOfMonth();
		mois = value.getMonthValue()-1;
		annee = value.getYear();
	}

	@Override
	public String toString() {
		return annee+"/"+mois+"/"+jour;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + annee;
		result = prime * result + jour;
		result = prime * result + mois;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Jour other = (Jour) obj;
		if (annee != other.annee)
			return false;
		if (jour != other.jour)
			return false;
		if (mois != other.mois)
			return false;
		return true;
	}
	
	public Number toNumber(){
		return (jour+mois * 100+annee*10000);
	}
}
