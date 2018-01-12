package eus.julenugalde.sudokuebatzaile;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

//TODO Lauki dokumentatu
/** Sudoku laukia */
public class Lauki {
	/** Zeldatxoaren zabalera */
	public static final int ZELDA_ZABALERA = 3;
	/** Laukiaren zabalera */
	public static final int ZABALERA = ZELDA_ZABALERA * ZELDA_ZABALERA;
	
	private Laukitxo[] laukitxoak;
	private boolean flagEbatzita = false;
	
	/** Laukia hutsik hasteko */
	public Lauki() {
		laukitxoak = new Laukitxo[ZABALERA * ZABALERA];
		for (int i=0; i<laukitxoak.length; i++) {
			laukitxoak[i] = new Laukitxo();
			laukitxoak[i].setHasierako(false);
		}
	}
	
	//TODO Lauki dokumentatu
	public Lauki(File fitxategi, char bereizle) throws IOException, NumberFormatException {
		this();

		if (!fitxategi.exists()) throw new IOException("Fitxategia ez da aukitu.");
		FileReader fr = new FileReader(fitxategi);
		try {
			int i=0;
			int irakurrita;
			while (((irakurrita=fr.read()) != -1) && (i<laukitxoak.length)) {
				if (irakurrita == ';') {
					laukitxoak[i].hustu();
					laukitxoak[i++].setHasierako(false);
					//System.out.println(i + " HUTSIK");
				}
				else if (irakurrita == '\n') {}
				else{
					//irakurrita karaktere bat da, bere balioa eskuratu behar da
					int zenbakia = irakurrita - '0';
					fr.read();	//Separadorea
					if ((zenbakia >= Laukitxo.MIN_VALUE) && (zenbakia <= Laukitxo.MAX_VALUE)) {
						laukitxoak[i].setBalio(zenbakia);
						laukitxoak[i++].setHasierako(true);
						//System.out.println(i + ": " + zenbakia);
					}
					else {
						throw new IOException("Balioa ez da zuzena: " + zenbakia);
					}
				}
			} 
		} finally {
			fr.close();
		}
		
	}
	
	//TODO Lauki dokumentatu
	public boolean setLaukitxo (int posizioa, int balioa) {
		if (posizioa < 0) return false;
		if (posizioa > laukitxoak.length) return false;
		return laukitxoak[posizioa].setBalio(balioa);
	}
	
	//TODO Lauki dokumentatu
	public Laukitxo[] getLaukitxoak() {
		return laukitxoak;
	}

	//TODO Lauki dokumentatu
	public boolean ebatzi() {
		int lehenengoHutsa = hurrengoPosizioHutsa();
		if (lehenengoHutsa == -1) {
			return true;	//Dagoeneko ebatzita
		}
		if (ebaztekoPausua(lehenengoHutsa)) {
			return true;
		}
		else {
			//Hutsik zeudenak berriro hustu
			for (Laukitxo l : laukitxoak) {
				if (!l.isHasierakoa()) {
					l.hustu();
				}
			}
			return false;
		}
	}
	
	private boolean ebaztekoPausua(int posizioa) {
		int hurrengoa;
		for (int i=Laukitxo.MIN_VALUE; i<=Laukitxo.MAX_VALUE; i++) {
			//System.out.print(" Probando " + i + " en " + posizioa + "... ");
			laukitxoak[posizioa].setBalio(i);
			if (egiaztatu()) {
				//Ematen du baliogarria dela, momentuz balioa utzi eta hurrengo posizio
				//hutsean saiatu
				//System.out.println("es valido. Proximo: " + hurrengoPosizioHutsa());
				if ((hurrengoa=hurrengoPosizioHutsa()) == -1) {
					flagEbatzita = true;
				}
				else {
					//Ez da azken laukitxo hutsa
					/*try {
						inprimatu();
						System.out.println("A probar la posicion " + hurrengoa);
						Thread.sleep(50);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}*/
					ebaztekoPausua(hurrengoa);
				}
			}
			/*else {
				System.out.println("no es valido");
			}*/
			if (flagEbatzita) return true;
		}
		//Balio guztiak probatu dira posizio honetan eta ez da baliogarria. Aurreko balioa
		//berreskuratu eta atera
		laukitxoak[posizioa].setBalio(Laukitxo.HUTSIK);
		return false;
	}
	
	private int hurrengoPosizioHutsa() {
		for (int i=0; i<laukitxoak.length; i++) {
			if (laukitxoak[i].isHutsik()) {
				return i;
			}
		}
		return -1;
	}
	
	
	/** Sudokuren arauak betetzen diren egiaztatu
	 * 
	 * @return <code>true</code> sudokua zuzena bada, <code>false</code> akatsik badago
	 */
	public boolean egiaztatu() {
		for (int i=0; i<laukitxoak.length; i++) {
			//hutsik ez badago,begiratu 
			if (!laukitxoak[i].isHutsik()) {
				int balioa = laukitxoak[i].getBalio();
				//Begiratu lerro berean balioa errepikatzen den
				int j = i + 1;
				while ((j%Lauki.ZABALERA != 0) && (j<laukitxoak.length)) {
					//System.out.println(i + "-" + j + " egiaztatzen (lerroa)");
					if (balioa == laukitxoak[j].getBalio()) {
						//System.err.println("Error filas: " + i + "-" + j);
						return false;
					}						
					j++;
				}
				//Begiratu zutabe berean balioa errepikatzen den
				j = i + Lauki.ZABALERA;
				while (j<laukitxoak.length) {
					//System.out.println(i + "-" + j + " egiaztatzen (zutabea)");
					if (balioa == laukitxoak[j].getBalio()) {
						//System.err.println("Error columnas: " + i + "-" + j);
						return false;
					}
					j+=Lauki.ZABALERA;
				}
				//Begiratu 3x3 laukian balio berdina badago
				j = i+1;
				while (j<laukitxoak.length) {
					if ( (!laukitxoak[j].isHutsik()) &&
						  (zelda(i)==zelda(j)) && 
						  (laukitxoak[i].getBalio() == laukitxoak[j].getBalio()) )
						  return false;
					j++;
				}						
			}
		}
		return true;
	}
	
	//TODO Lauki dokumentatu
	public int zelda(int kokapena) {
		int lerroa = (kokapena / (Lauki.ZELDA_ZABALERA * Lauki.ZABALERA)) % Lauki.ZELDA_ZABALERA; 
		int zutabea = (kokapena % Lauki.ZABALERA) / Lauki.ZELDA_ZABALERA; 
		return ((lerroa * Lauki.ZABALERA) + zutabea);
	}
	
	//TODO Lauki dokumentatu
	public boolean isEbatzita() {
		if (egiaztatu()) {
			for (int i=0; i<laukitxoak.length; i++) {
				if (laukitxoak[i].isHutsik()) return false;
			}
		}
		return true;
	}

	/** Probak egitek funtzio nagusia. Fitxategi bat ireki eta sudokua ebazten du
	 * 
	 * @param args Fitxategiaren izena. Hutsik badago probazko bat erabiltzen du
	 */
	public static void main(String[] args) {
		String izena = ((args.length==0) ? "v2155141.csv" : args[0]);
		File f = new File(System.getProperty("user.dir") + "//res//" + izena);
		try {
			Lauki lauki = new Lauki(f, ';');
			System.out.println(f.getAbsolutePath()  + " sudokua ebatziko da.");
			System.out.println(" * Ebatzi gabe \n" + lauki.toString());
			System.out.println("Sukodu-a ebazten. Itxaron mesedez...");
			lauki.ebatzi();
			System.out.println("\n * Ebatzita\n" + lauki.toString());
			
		} catch (IOException ioex) {
			System.err.println("IO error: " + ioex.getLocalizedMessage());
		} catch (NumberFormatException nfex) {
			System.err.println("Format error: " + nfex.getLocalizedMessage());
		}
	}
	
	@Override
	public String toString() {
		Laukitxo[] l = getLaukitxoak();
		int j=1;
		StringBuilder sb = new StringBuilder ("+---------+---------+---------+\n|");
		for (int i=0; i<l.length; i++, j++) {
			String katea = (l[i].isHutsik()) ? " . " : (" " + l[i].getBalio() + " "); 
			sb.append(katea);
			if ((j % Lauki.ZELDA_ZABALERA) == 0) {	//3x3 zeldatxoaren azkena
				sb.append("|");
			}
			if ((j % Lauki.ZABALERA) == 0) {	//Lerroaren azkena
				sb.append("\n");
				if ((j % (Lauki.ZABALERA*Lauki.ZELDA_ZABALERA)) == 0) {	//3 lerroaz behin
					sb.append("+---------+---------+---------+\n");
				}
				if (j != l.length) {
					sb.append("|");
				}
			}
		}
		return sb.toString();
	}

}
