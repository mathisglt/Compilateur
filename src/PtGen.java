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
    private static int nbVars;
    private static int nbVarAct;
    private static int ipoproc;
    private static int nbParamProc;
    private static int numProc;
    private static EltTabSymb eltmp;
    private static int indAffect;
    private static boolean isProc;
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
		isProc = false;
		// initialisation necessaire aux attributs lexicaux
		UtilLex.initialisation();
	
		// initialisation du type de l'expression courante
		tCour = NEUTRE;

		nbVarAct = 0;
		numProc = -1; // Ne pas laisser à 0 car la procédure 0 pourrait exister
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
    	// Constante
    	case 2:	
    		if (presentIdent(bc) == 0) {
    			if(tCour == ENT || tCour == BOOL) {
    				placeIdent(UtilLex.numIdCourant, CONSTANTE, tCour, vCour);
    			}else {
    				UtilLex.messErr("Déclaration constante de type neutre");
    			}
    		}
    		else UtilLex.messErr("Constante deja declaree.");
    			break;
    	// Variables
    	case 3:		
    		if (presentIdent(bc) == 0) {
    			if(tCour == ENT || tCour == BOOL) {
	    			if(isProc) {
	    				placeIdent(UtilLex.numIdCourant, VARLOCALE, tCour, nbVarAct); // Si nous compilons une procédure c'est une VARLOCALE
	    			}else {
						placeIdent(UtilLex.numIdCourant, VARGLOBALE, tCour, nbVarAct); // Sinon, une VARGLOBALE
					}
    			} else {
    				UtilLex.messErr("Déclaration d'une variable de type neutre");
    			}
    			nbVars++;
    			nbVarAct++;
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
    	case 25:	// Case d'ecriture
    		if(tCour==BOOL) {
    			po.produire(ECRBOOL);
    		}else if(tCour== ENT) po.produire(ECRENT);
    		else {UtilLex.messErr("Ecriture d'une variable non booléenne ou entière ");}
    		break;
    	// Cases relatif à primaire
    	case 26:	
    		po.produire(EMPILER);
    		po.produire(vCour);
    		break;
    	case 27:	
			int ind = presentIdent(1); // Vérifie si l'ident est dans la table
			if(ind != 0) { 
				eltmp = tabSymb[ind];// Ind est l'indice pù il se trouve, 0 si absent
				tCour = eltmp.type; // On récupere sa valeur et son type
				switch(eltmp.categorie) {
					case CONSTANTE: po.produire(EMPILER); po.produire(eltmp.info);break;
					case VARGLOBALE: po.produire(CONTENUG);po.produire(eltmp.info);break;
					case VARLOCALE: po.produire(CONTENUL);po.produire(eltmp.info);po.produire(0);break;
					case PARAMFIXE: po.produire(CONTENUL);po.produire(eltmp.info);po.produire(0);break;
					case PARAMMOD: po.produire(CONTENUL);po.produire(eltmp.info);po.produire(1);break;
					default: UtilLex.messErr("Type de variable non reconnue par l'écriture");
				}
			}else {
				UtilLex.messErr("Appel d'une variable non déclarée");
			}
    		break;
    		
    	// Cases relatif au affouappel 
    	case 28:
    		int ind2 = presentIdent(1);
    		if(ind2 !=0) {
    			if(tabSymb[ind2].categorie == VARGLOBALE || tabSymb[ind2].categorie == VARLOCALE || tabSymb[ind2].categorie == PARAMMOD) {
    				eltmp = tabSymb[ind2];
        			indAffect = eltmp.info;
    			}
    			if (tabSymb[ind2].categorie == CONSTANTE) {
    				UtilLex.messErr("Tentative d'affectation à une CONSTANTE  : " + eltmp.code);
    			}
    			else if (tabSymb[ind2].categorie == PROC) {
    				numProc = ind2;
    			}
    		}else {
    			UtilLex.messErr("La variable" +UtilLex.numIdCourant + " n'existe pas !" );
    		}
    		break;
    		
    	case 29:
    		// Les erreurs de typages sont gérés au précédent case
    		if(eltmp.categorie == VARGLOBALE) {
    			po.produire(AFFECTERG);
    			po.produire(indAffect);
    		}
    		else if (eltmp.categorie == VARLOCALE) {
    			po.produire(AFFECTERL);
    			po.produire(eltmp.info);
    			po.produire(0);
    		}
    		else if (eltmp.categorie == PARAMMOD) {
    			po.produire(AFFECTERL);
    			po.produire(eltmp.info);
    			po.produire(1);
    		}		
    		
    		break;
    	// Case relatif à lecture
    	case 30 : 
    		int indlec = presentIdent(1); // Récupère son indice dans la table
    		if(indlec !=0) { // Si il existe
    			eltmp = tabSymb[indlec];
    			if (eltmp.type == ENT) po.produire(LIRENT); // Si c'est un entier on lirent 
    			else po.produire(LIREBOOL); // Sinon lirebool
    			
    			switch(eltmp.categorie) {
    				case VARGLOBALE: 
    				case PARAMMOD:
    				po.produire(AFFECTERG); // Si c'est une variable globale on l'affecte
    				po.produire(eltmp.info);break;
    			}
    			
    		}else {
    			UtilLex.messErr("Lecture d'une variable inexistante : " + UtilLex.numIdCourant );
    		}
    		break;
    	// Cases relatif au inssi : Test relatif :  TestsProjet\TestsProjet\TestPerso-si
    		
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
    	// Cases relatif au ttq  : Test relatif :  TestsProjet\TestsProjet\TestPerso-ttq
    	case 34 : // Juste Empiler ipo actuel en pileRep
    		pileRep.empiler(po.getIpo()+1);
    		break;
    		
    	case 35 :
    		int indbsifaux = pileRep.depiler();
    		int inddebutexpr = pileRep.depiler();
    		po.produire(BINCOND); // Bsifaux amenant à la fin de la boucle
    		po.produire(0);
    		po.modifier(po.getIpo(),inddebutexpr ); // Modifier l'ipo du bincond pour retourner au début de l'évaluation de l'expression
    		po.modifier(indbsifaux, po.getIpo()+1); // Modifier l'ipo de sortie de while
    		break;
    		
    	// Cases relatif au inscond : Test relatif :  TestsProjet\TestsProjet\TestPerso-cond
    		
    	case 36: // Creer le bincond en le liant au précédent, 
    		     // On a empiler 0 avant pour gérer le cas du premier cond
    			 // Sinon il dépilerait quelque chose qui n'est pas voulu
    		int indbsifaux36 = pileRep.depiler();
    		int indbincond36 = pileRep.depiler();
    		po.produire(BINCOND);
    		po.produire(indbincond36); // Bincond pointe vers le bincond précédent ou 0 
    		po.modifier(indbsifaux36, po.getIpo()+1);; // L'ancien bsifaux renvoie au prochain ipo
    		pileRep.empiler(po.getIpo()); // On empile l'ipo du bincond pour le prochain
    		break;
    		
    	case 37: // Dernier cond , début du aut
    		int indbsifaux37 = pileRep.depiler();
    		po.produire(BINCOND);
    		po.produire(0);
    		po.modifier(indbsifaux37, po.getIpo()+1);
    		pileRep.empiler(po.getIpo());
    		break;
    		
    	case 38: // Cond aut
    		int autbincond = pileRep.depiler();
    		int dernierind = pileRep.depiler();
    		po.modifier(autbincond, po.getIpo()+1);
    		
    		while (dernierind !=0 ) { // Tant que l'on est pas revenu au premier bincond 
    			int tmp = po.getElt(dernierind); // On sauvegard temporairement le retour suivant
    			po.modifier(dernierind, po.getIpo()+1); // Le bincond renvoie au fcond
    			dernierind = tmp; // On continue 
    		}
    		po.modifier(dernierind, po.getIpo()+1); // Ne pas oublier le premier bincond
    		break;
    	case 39:
    		pileRep.empiler(0); // expliqué au case 36
    		break;
    		
    		// Traitement des procédures
    	case 48:
    		// Fonctionne avec une variable car il n'y a pas de possibilités de proc imbriquée dans une autre
    		po.produire(BINCOND);
			po.produire(0); // Bincond qui saute la procédure pour qu'elle ne soit pas éxécutée sans être appelée
			ipoproc = po.getIpo();
			break;
    	case 49:
    		placeIdent(UtilLex.numIdCourant, PROC, NEUTRE, po.getIpo()+1); // 1ère ligne proc
			placeIdent(-1,PRIVEE,NEUTRE,0);// 2ème ligne proc 
    	
			bc = presentIdent(-1)+2;	// On utilise la ligne d'avant pour retrouver le dernier élément puis on remonte de 2 pour englober les 2 nouvelles lignes crées
   

			isProc = true;
			nbVarAct=2; // Ajout de 2 pour compter l'adresse de retour et l'ex bp
			nbVars=0;
    		break;
    	case 50:
    		tabSymb[bc-1].info = nbVars; // Après chargement de tous les paramètres, actualisation dans la 2ème ligne de proc
    		nbVars = 0;
    		break;
    	case 51:
    		// crééer les param fixe dans tabSymb
    		placeIdent(UtilLex.numIdCourant, PARAMFIXE, tCour, nbVars);
    		nbVars++;
    		nbVarAct++;
    		break;
    	case 52:
    		// case 51 mais avec mod
    		placeIdent(UtilLex.numIdCourant, PARAMMOD, tCour, nbVars);
    		nbVars++;	
    		nbVarAct++;
    		break;
    	case 53:
    		int cat = tabSymb[it].categorie;
    		while (cat == CONSTANTE || cat == VARLOCALE){  // On supprime les constantes et Variables locales de la table des symboles
				if(cat == VARLOCALE) {nbVarAct--;};
    			tabSymb[it] = null; 
				it--;
				cat = tabSymb[it].categorie;
			}
			int it2 = it;
			while (cat == PARAMMOD || cat == PARAMFIXE){
				nbVarAct--;
				tabSymb[it2].code = -1;
				it2--;
				cat = tabSymb[it2].categorie;
			}
			po.produire(RETOUR);
			po.produire(tabSymb[bc-1].info);
			bc = 1;
			isProc = false;
			break;
    	case 54:
    		po.modifier(ipoproc,po.getIpo()+1); // Changement du bincond de début à la suite du programme
    		pileRep.empiler(po.getIpo());
    		break;
    		
    	case 60: // effixes
    		nbParamProc++;
    		break;
 
    	case 61: // effmods
    		int indproc = presentIdent(1); // On récupère l'indice de l'ident
    		switch (tabSymb[indproc].categorie) {
    			case VARLOCALE:
    			case PARAMFIXE:
    			case PARAMMOD :po.produire(EMPILERADL);
				   		       po.produire(tabSymb[indproc].info); // Si c'est un de ces 3 on récupère le contenu localement
				   		       break;
    			case VARGLOBALE:
    				po.produire(EMPILERADG); 
    				po.produire(tabSymb[indproc].info); // Sinon globalement
    				break;
    			default:UtilLex.messErr("Type Incorrect entré dans les paramètre modifiable de la procédures : " + tabSymb[indproc].categorie );break;
    		}
    		if(tabSymb[indproc].categorie == PARAMMOD)po.produire(1);
			else if (tabSymb[indproc].categorie != VARGLOBALE) po.produire(0);
    		nbParamProc++;
    		break;
    		
    	// Cases relatifs à l'appel d'une procédure
    	case 70:
    		nbParamProc =0; // Reset au cas ou nous avons déjà compilé une procédure
    		break;
    	case 71:
    		//Vérification du nombre de paramètres
    		if(nbParamProc == tabSymb[numProc+1].info) {
    			if(numProc >=0){
    				po.produire(APPEL);
    				po.produire(tabSymb[numProc].info); // ipo de la proc
    				po.produire(tabSymb[numProc+1].info); // nombre de paramètres
    				numProc = -1; // Reintialise le numéro de Procédure
    			}
    		}else {
    			UtilLex.messErr("Nombre de paramètres incorrect pour l'appel de procédure " + numProc );
    		}
    		break;
    		
    	case 200: 
    		if(nbVars > 0) {
    			po.produire(RESERVER);
        		po.produire(nbVars);
        		nbVars = 0;
    		}
    		break;
		case 255 : 
			afftabSymb(); // affichage de la table des symboles en fin de compilation
			po.produire(ARRET);
			po.constGen(); 
			po.constObj();
			
			break;
			
// test : TestsProjet\TestsProjet\TDexo3-sittq
			// TestsProjet\TestsProjet\TestPerso-
			// TestsProjet\TestsProjet\polyP36-exo8
		
		default:
			System.out.println("Point de generation non prevu dans votre liste");
			break;

		}
	}
}
    
    
    
    
    
    
    
    
    
    
    
    
    
 
