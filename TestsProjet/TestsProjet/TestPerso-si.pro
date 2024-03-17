programme sittq:

const marqueur = -1;
var ent nblu, min, max;		{adresses variables dans la pile d'ex�cution de MAPILE: 0, 1, 2}
debut
	lire(min); max := min; lire(nblu);
	si nblu > max alors max := nblu sinon min := nblu fsi;
	lire(nblu);
	ecrire(min, max);
fin
