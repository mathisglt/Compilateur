/*********************************************************************************
 * VARIABLES ET METHODES FOURNIES PAR LA CLASSE UtilLex (cf libClass_Projet)     *
 *       complement à l'ANALYSEUR LEXICAL produit par ANTLR                      *
 *                                                                               *
 *                                                                               *
 *   nom du programme compile, sans suffixe : String UtilLex.nomSource           *
 *   ------------------------                                                    *
 *                                                                               *
 *   attributs lexicaux (selon items figurant dans la grammaire):                *
 *   ------------------                                                          *
 *     int UtilLex.valEnt = valeur du dernier nombre entier lu (item nbentier)   *
 *     int UtilLex.numIdCourant = code du dernier identificateur lu (item ident) *
 *                                                                               *
 *                                                                               *
 *   methodes utiles :                                                           *
 *   ---------------                                                             *
 *     void UtilLex.messErr(String m)  affichage de m et arret compilation       *
 *     String UtilLex.chaineIdent(int numId) delivre l'ident de codage numId     *
 *     void afftabSymb()  affiche la table des symboles                          *
 *********************************************************************************/


import java.io.*;

/**
 * classe de mise en oeuvre du compilateur
 * =======================================
 * (verifications semantiques + production du code objet)
 * 
 * @author Girard, Masson, Perraudeau
 *
 */

public class PtGen {
    

    // constantes manipulees par le compilateur
    // ----------------------------------------

	private static final int 
	
	// taille max de la table des symboles
	MAXSYMB=300,

	// codes MAPILE :
	RESERVER=1,EMPILER=2,CONTENUG=3,AFFECTERG=4,OU=5,ET=6,NON=7,INF=8,
	INFEG=9,SUP=10,SUPEG=11,EG=12,DIFF=13,ADD=14,SOUS=15,MUL=16,DIV=17,
	BSIFAUX=18,BINCOND=19,LIRENT=20,LIREBOOL=21,ECRENT=22,ECRBOOL=23,
	ARRET=24,EMPILERADG=25,EMPILERADL=26,CONTENUL=27,AFFECTERL=28,
	APPEL=29,RETOUR=30,

	// codes des valeurs vrai/faux
	VRAI=1, FAUX=0,

    // types permis :
	ENT=1,BOOL=2,NEUTRE=3,

	// categories possibles des identificateurs :
	CONSTANTE=1,VARGLOBALE=2,VARLOCALE=3,PARAMFIXE=4,PARAMMOD=5,PROC=6,
	DEF=7,REF=8,PRIVEE=9,

    //valeurs possible du vecteur de translation 
    TRANSDON=1,TRANSCODE=2,REFEXT=3;


    // utilitaires de controle de type
    // -------------------------------
    /**
     * verification du type entier de l'expression en cours de compilation 
     * (arret de la compilation sinon)
     */
	private static void verifEnt() {
		if (tCour != ENT)
			UtilLex.messErr("expression entiere attendue");
	}
	/**
	 * verification du type booleen de l'expression en cours de compilation 
	 * (arret de la compilation sinon)
	 */
	private static void verifBool() {
		if (tCour != BOOL)
			UtilLex.messErr("expression booleenne attendue");
	}

    // pile pour gerer les chaines de reprise et les branchements en avant
    // -------------------------------------------------------------------

    private static TPileRep pileRep;  


    // production du code objet en memoire
    // -----------------------------------

    private static ProgObjet po;
    
    
    // COMPILATION SEPAREE 
    // -------------------
    //
    /** 
     * modification du vecteur de translation associe au code produit 
     * + incrementation attribut nbTransExt du descripteur
     *  NB: effectue uniquement si c'est une reference externe ou si on compile un module
     * @param valeur : TRANSDON, TRANSCODE ou REFEXT
     */
    private static void modifVecteurTrans(int valeur) {
		if (valeur == REFEXT || desc.getUnite().equals("module")) {
			po.vecteurTrans(valeur);
			desc.incrNbTansExt();
		}
	}    
    // descripteur associe a un programme objet (compilation separee)
    private static Descripteur desc;

     
    // autres variables fournies
    // -------------------------
    
 // MERCI de renseigner ici un nom pour le trinome, constitue EXCLUSIVEMENT DE LETTRES
    public static String trinome="NolMatVinc";
    
    private static int tCour; // type de l'expression compilee
    private static int vCour; // sert uniquement lors de la compilation d'une valeur (entiere ou boolenne)
    private static int code;
    private static int nbVars;
    private static int inttmp;
    private static int booltmp;
    private static EltTabSymb eltmp;
    private static int indAffect;
    // TABLE DES SYMBOLES
    // ------------------
    //
    private static EltTabSymb[] tabSymb = new EltTabSymb[MAXSYMB + 1];
    
    // it = indice de remplissage de tabSymb
    // bc = bloc courant (=1 si le bloc courant est le programme principal)
	private static int it, bc;
	
	/** 
	 * utilitaire de recherche de l'ident courant (ayant pour code UtilLex.numIdCourant) dans tabSymb
	 * 
	 * @param borneInf : recherche de l'indice it vers borneInf (=1 si recherche dans tout tabSymb)
	 * @return : indice de l'ident courant (de code UtilLex.numIdCourant) dans tabSymb (O si absence)
	 */
	private static int presentIdent(int borneInf) {
		int i = it;
		while (i >= borneInf && tabSymb[i].code != UtilLex.numIdCourant)
			i--;
		if (i >= borneInf)
			return i;
		else
			return 0;
	}

	/**
	 * utilitaire de placement des caracteristiques d'un nouvel ident dans tabSymb
	 * 
	 * @param code : UtilLex.numIdCourant de l'ident
	 * @param cat : categorie de l'ident parmi CONSTANTE, VARGLOBALE, PROC, etc.
	 * @param type : ENT, BOOL ou NEUTRE
	 * @param info : valeur pour une constante, ad d'exécution pour une variable, etc.
	 */
	private static void placeIdent(int code, int cat, int type, int info) {
		if (it == MAXSYMB)
			UtilLex.messErr("debordement de la table des symboles");
		it = it + 1;
		tabSymb[it] = new EltTabSymb(code, cat, type, info);
	}

	/**
	 *  utilitaire d'affichage de la table des symboles
	 */
	private static void afftabSymb() { 
		System.out.println("       code           categorie      type    info");
		System.out.println("      |--------------|--------------|-------|----");
		for (int i = 1; i <= it; i++) {
			if (i == bc) {
				System.out.print("bc=");
				Ecriture.ecrireInt(i, 3);
			} else if (i == it) {
				System.out.print("it=");
				Ecriture.ecrireInt(i, 3);
			} else
				Ecriture.ecrireInt(i, 6);
			if (tabSymb[i] == null)
				System.out.println(" reference NULL");
			else
				System.out.println(" " + tabSymb[i]);
		}
		System.out.println();
	}
    

	/**
	 *  initialisations A COMPLETER SI BESOIN
	 *  -------------------------------------
	 */
	public static void initialisations() {
	
		// indices de gestion de la table des symboles
		it = 0;
		bc = 1;
		
		// pile des reprises pour compilation des branchements en avant
		pileRep = new TPileRep(); 
		// programme objet = code Mapile de l'unite en cours de compilation
		po = new ProgObjet();
		// COMPILATION SEPAREE: desripteur de l'unite en cours de compilation
		desc = new Descripteur();
		
		// initialisation necessaire aux attributs lexicaux
		UtilLex.initialisation();
	
		// initialisation du type de l'expression courante
		tCour = NEUTRE;

		//TODO si necessaire

	} // initialisations

	/**
	 *  code des points de generation A COMPLETER
	 *  -----------------------------------------
	 * @param numGen : numero du point de generation a executer
	 */
	public static void pt(int numGen) {
	
		switch (numGen) {
		case 0:
			initialisations();
			break;
		// Ident
    	case 1:	code = UtilLex.numIdCourant;
    	
    			break;

    	// Constante
    	case 2:	if (presentIdent(1) == 0) placeIdent(code, CONSTANTE, tCour, vCour);
    			else UtilLex.messErr("Constante deja declaree.");
    			break;
    	// Variables
    	case 3:		
    		if (presentIdent(1) == 0) {
				placeIdent(UtilLex.numIdCourant, VARGLOBALE, tCour, nbVars);
			nbVars++;
    		}
    		else UtilLex.messErr("Variable deja declaree.");
		break;
		// Entier positif
    	case 4:	tCour = ENT;
    			vCour = UtilLex.valEnt;
    			break;

    	// Entier negatif
    	case 5:	tCour = ENT;
    			vCour = - UtilLex.valEnt ;
				break;

		// Bool true
    	case 6:	tCour = BOOL;
    			vCour = VRAI;
    			break;

    	// Bool false
    	case 7:	tCour = BOOL;
    			vCour = FAUX;
				break;

    	case 8:	tCour = ENT;
				break;
				
    	case 9:	tCour = BOOL;
				break;
    	case 10:	
    		verifBool();
    		break;
    	case 11:
    		po.produire(OU);
			break;
    	case 12: 
    		po.produire(ET);
    		break;
    	case 13:	
    		po.produire(NON);
    		break;
    	case 14:	
    		verifEnt();
    		break;
    	case 15:	
    		po.produire(EG);tCour=BOOL;
    		break;
    	case 16:	
    		po.produire(DIFF);tCour=BOOL;
    		break;
    	case 17:	
    		po.produire(SUP);
    		tCour=BOOL;
    		break;
    	case 18:	
    		po.produire(SUPEG);tCour=BOOL;
    		break;
    	case 19:	
    		po.produire(INF);tCour=BOOL;
    		break;
    	case 20:	
    		po.produire(INFEG);tCour=BOOL;
    		break;
    	case 21:	
    		po.produire(ADD);
    		break;
    	case 22:	
    		po.produire(SOUS);
    		break;
    	case 23:	
    		po.produire(MUL);
    		break;
    	case 24:	
    		po.produire(DIV);
    		break;
    	case 25:	
    		if(tCour==BOOL) {
    			po.produire(ECRBOOL);
    		}else po.produire(ECRENT);
    		break;
    	case 26:	
    		po.produire(EMPILER);
    		po.produire(UtilLex.valEnt);
    		break;
    	case 27:	
    		int ind = presentIdent(1);
    		if(ind !=0) {
    			eltmp = tabSymb[ind];
    			tCour = eltmp.type;
    			switch(eltmp.categorie) {
    				case CONSTANTE: po.produire(EMPILER); po.produire(eltmp.info);break;
    				case VARGLOBALE: po.produire(CONTENUG);po.produire(eltmp.info);break;
    				default: UtilLex.messErr("tf is that bro");
    			}
    		}
    		break;
    	case 28:
    		int ind2 = presentIdent(1);
    		if(ind2 !=0) {
    			eltmp = tabSymb[ind2];
    			indAffect = eltmp.info;
    		}
    		break;
    		
    	case 29:
    		po.produire(AFFECTERG);
    		po.produire(indAffect);
    		break;
    		
    	case 30 :
    		int indlec = presentIdent(1);
    		if(indlec !=0) {
    			eltmp = tabSymb[indlec];
    			if (eltmp.type == ENT) po.produire(LIRENT);
    			else po.produire(LIREBOOL);
    			
    			switch(eltmp.categorie) {
    			case VARGLOBALE: 
    				po.produire(AFFECTERG);
    				po.produire(eltmp.info);break;
    			default: UtilLex.messErr("Mauvaise inscription");
    			}
    			
    		}
    		break;
    		
    	case 31 : // Premier if du si
    		verifBool(); // L'expression doit être booléenne 
    		po.produire(BSIFAUX); // Bsifaux , aller au else si expression évaluée à faux
    		po.produire(0); // Valeur de base mise à 0 arbitrairement 
    		pileRep.empiler(po.getIpo());
    		break;
    	case 32 : // Sinon du if ( non obligatoire d'exister ) 
    		int indicecond = pileRep.depiler();
    		po.produire(BINCOND); // Bincond pour sauter le sinon si le si était vrai
    		po.produire(0);
    		pileRep.empiler(po.getIpo()); // Empiler l'ipo actuel
    		po.modifier(indicecond, po.getIpo()+1); // Modifier l'ipo du bsifaux pour aller à la ligne suivante
    		break;
    	case 33 :
    		int indicecond2 = pileRep.depiler();
    		po.modifier(indicecond2, po.getIpo()+1); // Modifier l'ipo du bincond pour aller à la ligne suivante
    		break;
    	
		case 255 : 
			afftabSymb(); // affichage de la table des symboles en fin de compilation
			po.constGen();
			po.constObj();
			break;
// test : TestsProjet\TestsProjet\TDexo3-sittq
			// TestsProjet\TestsProjet\TestPerso-si
		
		default:
			System.out.println("Point de generation non prevu dans votre liste");
			break;

		}
	}
}
    
    
    
    
    
    
    
    
    
    
    
    
    
 
